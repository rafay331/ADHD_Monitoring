package com.example.adhd_monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PatientBudgetActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etCategory;
    private MaterialButton btnConfirmPurchase;

    private LinearProgressIndicator progressBudget;
    private TextView tvSpentLimit, tvStatusText, tvStatusHint;

    private MaterialCardView cardMonthlyLock;
    private TextView tvMonthlyRemaining;

    // ⭐ Savings rating stars
    private TextView tvSavingsStars;

    // Cool-off UI
    private MaterialCardView cardNoSpendLock;
    private TextView tvNoSpendRemaining;

    private final Handler ui = new Handler(Looper.getMainLooper());

    private AppDatabase db;
    private long userId = 0;

    // ✅ Cool-off constants/prefs
    private static final long COOL_OFF_MS = 24L * 60L * 60L * 1000L; // 24 hours
    private static final String COOL_PREFS = "cooloff_prefs";

    // ✅ timer runnable to keep remaining time updating
    private final Runnable coolTick = new Runnable() {
        @Override
        public void run() {
            refreshCoolOffUI();
            if (isCoolOffActive()) {
                ui.postDelayed(this, 1000); // update every second
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_budget);

        db = AppDatabase.getInstance(this);

        int uid = getIntent().getIntExtra("userId", -1);

        if (uid == -1) {
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            uid = loginPrefs.getInt("user_id", -1);
        }

        if (uid == -1) {
            toast("User ID missing - monthly limit can't load");
            finish();
            return;
        }

        userId = uid;

        bind();
        wire();

        refreshMonthlyUI();
        refreshCoolOffUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMonthlyUI();
        refreshCoolOffUI();

        // ✅ start ticking if active
        ui.removeCallbacks(coolTick);
        if (isCoolOffActive()) {
            ui.post(coolTick);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.removeCallbacks(coolTick); // ✅ avoid leaks
    }

    private void bind() {
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        btnConfirmPurchase = findViewById(R.id.btnConfirmPurchase);

        progressBudget = findViewById(R.id.progressBudget);
        tvSpentLimit = findViewById(R.id.tvSpentLimit);

        tvStatusText = findViewById(R.id.tvStatusText);
        tvStatusHint = findViewById(R.id.tvStatusHint);

        cardMonthlyLock = findViewById(R.id.cardMonthlyLock);
        tvMonthlyRemaining = findViewById(R.id.tvMonthlyRemaining);

        tvSavingsStars = findViewById(R.id.tvSavingsStars);

        cardNoSpendLock = findViewById(R.id.cardNoSpendLock);
        tvNoSpendRemaining = findViewById(R.id.tvNoSpendRemaining);
    }

    private void wire() {
        btnConfirmPurchase.setOnClickListener(v -> {

            // 1) cool-off block
            if (isCoolOffActive()) {
                toast("Cool-Off active. You cannot spend right now.");
                refreshCoolOffUI();
                return;
            }

            Integer amount = parseAmount();
            String category = safeText(etCategory);

            if (amount == null || amount <= 0) { toast("Enter valid amount"); return; }
            if (category.isEmpty()) { toast("Enter category"); return; }

            Executors.newSingleThreadExecutor().execute(() -> {
                String mk = monthKey();
                long[] range = monthRangeMillis();
                long start = range[0], end = range[1];

                MonthlyBudgetLimitEntity limit = db.monthlyBudgetLimitDao().getLimit(userId, mk);
                int spent = db.expenseDao().sumForMonth(userId, start, end);

                boolean lockEnabled = (limit != null && limit.lockEnabled);
                double limitAmount = (limit != null ? limit.limitAmount : 0);

                if (lockEnabled && limitAmount > 0 && (spent + amount) > limitAmount) {
                    ui.post(() -> {
                        toast("Monthly limit reached. Can't add more.");
                        refreshMonthlyUI();
                    });
                    return;
                }

                db.expenseDao().insert(new ExpenseEntity(userId, amount, category, System.currentTimeMillis()));

                ui.post(() -> {
                    toast("Expense saved");
                    etAmount.setText("");
                    etCategory.setText("");

                    // ✅ Trigger cool-off on high purchase
                    if (amount >= 3500) {
                        startCoolOff24h();
                    }

                    refreshMonthlyUI();
                    refreshCoolOffUI();
                });
            });
        });
    }

    private void refreshMonthlyUI() {
        if (userId <= 0) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            String mk = monthKey();
            long[] range = monthRangeMillis();
            int spent = db.expenseDao().sumForMonth(userId, range[0], range[1]);
            MonthlyBudgetLimitEntity limit = db.monthlyBudgetLimitDao().getLimit(userId, mk);

            double limitAmount = (limit != null ? limit.limitAmount : 0);
            boolean lockEnabled = (limit != null && limit.lockEnabled);

            double savedAmount = limitAmount - spent;
            int stars = computeStars(limitAmount, spent);

            try {
                db.savingsRatingDao().upsert(new SavingsRatingEntity(
                        userId,
                        mk,
                        limitAmount,
                        spent,
                        savedAmount,
                        stars,
                        System.currentTimeMillis()
                ));
            } catch (Exception ignored) {}

            ui.post(() -> {
                boolean coolOff = isCoolOffActive();

                if (limitAmount <= 0) {
                    tvSpentLimit.setText(String.format(Locale.getDefault(), "%d / No limit", spent));
                    progressBudget.setProgress(0);
                    cardMonthlyLock.setVisibility(View.GONE);

                    btnConfirmPurchase.setEnabled(!coolOff);

                    tvStatusText.setText("On Track");
                    tvStatusHint.setText("No monthly limit set");

                    if (tvSavingsStars != null) {
                        tvSavingsStars.setText("Savings Rating: " + starsToString(1));
                    }
                    return;
                }

                int pct = (int) Math.min(100, Math.round((spent / limitAmount) * 100.0));
                progressBudget.setProgress(Math.max(0, pct));
                tvSpentLimit.setText(String.format(Locale.getDefault(), "%d / %.0f", spent, limitAmount));

                double remaining = Math.max(0, limitAmount - spent);
                tvMonthlyRemaining.setText(String.format(Locale.getDefault(), "Remaining: %.0f PKR", remaining));

                boolean monthlyLocked = lockEnabled && spent >= limitAmount;
                cardMonthlyLock.setVisibility(monthlyLocked ? View.VISIBLE : View.GONE);

                // ✅ disable button if either lock is active
                boolean enableSave = !coolOff && !monthlyLocked;
                btnConfirmPurchase.setEnabled(enableSave);

                if (tvSavingsStars != null) {
                    tvSavingsStars.setText("Savings Rating: " + starsToString(stars));
                }

                if (coolOff) {
                    tvStatusText.setText("Locked");
                    tvStatusHint.setText("Cool-Off active (24 hours)");
                } else if (!lockEnabled) {
                    tvStatusText.setText("On Track");
                    tvStatusHint.setText("Monthly lock disabled");
                } else if (monthlyLocked) {
                    tvStatusText.setText("Locked");
                    tvStatusHint.setText("Monthly limit reached");
                } else if (pct >= 90) {
                    tvStatusText.setText("Near Limit");
                    tvStatusHint.setText("Spend carefully");
                } else {
                    tvStatusText.setText("On Track");
                    tvStatusHint.setText("Good job");
                }
            });
        });
    }

    // ⭐ Star logic
    private int computeStars(double limitAmount, int spent) {
        if (limitAmount <= 0) return 1;
        double saved = limitAmount - spent;

        if (saved >= 10000) return 5;
        if (saved >= 5000) return 4;
        if (saved >= 2000) return 3;
        if (saved > 0) return 2;
        return 1;
    }

    private String starsToString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= stars ? "★" : "☆");
        return sb.toString();
    }

    // ---------- helpers ----------
    private String monthKey() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }

    private long[] monthRangeMillis() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    private Integer parseAmount() {
        try {
            String s = safeText(etAmount);
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) { return null; }
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // =========================
    // ✅ WORKING COOL-OFF METHODS
    // =========================

    private String coolKey() {
        return "cooloff_end_user_" + userId;
    }

    private boolean isCoolOffActive() {
        SharedPreferences prefs = getSharedPreferences(COOL_PREFS, MODE_PRIVATE);
        long end = prefs.getLong(coolKey(), 0L);
        return System.currentTimeMillis() < end;
    }

    private void startCoolOff24h() {
        long now = System.currentTimeMillis();

        // If already active, keep the later end time (don’t shorten it)
        SharedPreferences prefs = getSharedPreferences(COOL_PREFS, MODE_PRIVATE);
        long currentEnd = prefs.getLong(coolKey(), 0L);

        long newEnd = Math.max(currentEnd, now + COOL_OFF_MS);
        prefs.edit().putLong(coolKey(), newEnd).apply();

        toast("High purchase detected. Cool-Off started for 24 hours.");

        refreshCoolOffUI();

        // start ticking
        ui.removeCallbacks(coolTick);
        ui.post(coolTick);
    }

    private void refreshCoolOffUI() {
        if (cardNoSpendLock == null || tvNoSpendRemaining == null || btnConfirmPurchase == null) return;

        SharedPreferences prefs = getSharedPreferences(COOL_PREFS, MODE_PRIVATE);
        long end = prefs.getLong(coolKey(), 0L);
        long now = System.currentTimeMillis();

        boolean active = now < end;

        if (!active) {
            cardNoSpendLock.setVisibility(View.GONE);
            tvNoSpendRemaining.setText("");
            // don’t force-enable here because monthly lock might be active; refreshMonthlyUI decides final state
            return;
        }

        long remaining = end - now;
        cardNoSpendLock.setVisibility(View.VISIBLE);
        tvNoSpendRemaining.setText("No-spend remaining: " + formatDuration(remaining));

        // disable immediately at UI level too
        btnConfirmPurchase.setEnabled(false);
    }

    private String formatDuration(long ms) {
        long totalSec = Math.max(0, ms / 1000);
        long hrs = totalSec / 3600;
        long min = (totalSec % 3600) / 60;
        long sec = totalSec % 60;

        if (hrs > 0) {
            return String.format(Locale.getDefault(), "%dh %dm %ds", hrs, min, sec);
        } else {
            return String.format(Locale.getDefault(), "%dm %ds", min, sec);
        }
    }
}
