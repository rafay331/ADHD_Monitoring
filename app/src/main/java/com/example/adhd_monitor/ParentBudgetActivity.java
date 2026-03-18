package com.example.adhd_monitor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ParentBudgetActivity extends AppCompatActivity {

    private MaterialToolbar toolbarParentBudget;

    // Budget overview
    private TextView tvBudgetProgress, tvBudgetStatus, tvBudgetMonth;

    private RecyclerView rvParentRecent;

    private TextInputEditText etMonthlyLimit;
    private MaterialButton btnSaveLimit;
    private SwitchMaterial switchBudgetLock;

    // ✅ ONLY remaining action button
    private MaterialButton btnParentReports;

    private AppDatabase db;

    private long childId = 0; // linked child id
    private int parentId = -1;

    private double currentLimit = 0;
    private int currentSpent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_budget);

        db = AppDatabase.getInstance(this);

        // Get parentId from intent
        String receivedId = getIntent().getStringExtra("userId");
        if (receivedId != null) {
            try {
                parentId = Integer.parseInt(receivedId);
            } catch (Exception e) {
                parentId = -1;
            }
        }

        if (parentId == -1) {
            toast("Parent ID missing");
            finish();
            return;
        }

        // Bind views
        toolbarParentBudget = findViewById(R.id.toolbarParentBudget);

        // ⚠️ Make sure these IDs exist in your activity_parent_budget.xml
        tvBudgetProgress = findViewById(R.id.tvParentSpentLimit);


        tvBudgetStatus = findViewById(R.id.tvParentAlert);
        tvBudgetMonth = findViewById(R.id.tvBudgetMonth);

        etMonthlyLimit = findViewById(R.id.etMonthlyLimit);
        btnSaveLimit = findViewById(R.id.btnSaveLimit);
        switchBudgetLock = findViewById(R.id.switchBudgetLock);
        btnParentReports = findViewById(R.id.btnParentReports);


        rvParentRecent = findViewById(R.id.rvParentRecent);
        LinearProgressIndicator progressParentBudget =
                findViewById(R.id.progressParentBudget);
        progressParentBudget.setMax((int) currentLimit);
        progressParentBudget.setProgress(currentSpent);

        rvParentRecent.setLayoutManager(new LinearLayoutManager(this));

        toolbarParentBudget.setNavigationOnClickListener(v -> finish());

        // Load linked childId
        loadLinkedChildIdFromRoom();

        // Save monthly limit
        btnSaveLimit.setOnClickListener(v -> {
            if (childId == 0) {
                toast("Child not linked");
                return;
            }

            Double limit = parseDouble(etMonthlyLimit);
            if (limit == null || limit < 0) {
                toast("Enter a valid limit");
                return;
            }

            String mk = monthKey();
            boolean enabled = switchBudgetLock.isChecked();

            Executors.newSingleThreadExecutor().execute(() -> {
                MonthlyBudgetLimitEntity e = new MonthlyBudgetLimitEntity(
                        childId, mk, limit, enabled, System.currentTimeMillis()
                );
                db.monthlyBudgetLimitDao().upsert(e);

                runOnUiThread(() -> {
                    toast("Monthly limit saved");
                    loadBudgetOverview(); // ✅ refresh overview
                });
            });
        });

        // Toggle auto-lock
        switchBudgetLock.setOnCheckedChangeListener((btn, isChecked) -> {
            if (childId == 0) return;

            Executors.newSingleThreadExecutor().execute(() -> {
                MonthlyBudgetLimitEntity existing =
                        db.monthlyBudgetLimitDao().getLimit(childId, monthKey());

                double limit = (existing == null) ? 0 : existing.limitAmount;

                db.monthlyBudgetLimitDao().upsert(
                        new MonthlyBudgetLimitEntity(
                                childId, monthKey(), limit, isChecked, System.currentTimeMillis()
                        )
                );

                runOnUiThread(this::loadBudgetOverview); // ✅ refresh overview
            });
        });

        // Generate report (TXT + Share)
        btnParentReports.setOnClickListener(v -> generateAndShareReport());
    }

    private void loadLinkedChildIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ParentChildLink link =
                    db.parentChildLinkDao().getLinkByParentId(parentId);

            runOnUiThread(() -> {
                if (link == null) {
                    childId = 0;
                    toast("No child linked to this parent");
                    renderOverview(0, 0, monthKey()); // optional: show empty state
                } else {
                    childId = link.getChildId();
                    loadRecentExpenses();   // ✅ list
                    loadBudgetOverview();   // ✅ overview
                }
            });
        });
    }

    private void loadRecentExpenses() {
        if (childId == 0) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExpenseEntity> recent = db.expenseDao().getRecentExpenses(childId, 10);

            runOnUiThread(() -> rvParentRecent.setAdapter(new ParentRecentExpenseAdapter(recent)));
        });
    }

    // ---------------- Budget Overview ----------------
    private void loadBudgetOverview() {
        if (childId == 0) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            String mk = monthKey();
            long[] range = monthRangeMillis();

            MonthlyBudgetLimitEntity limitEntity =
                    db.monthlyBudgetLimitDao().getLimit(childId, mk);

            double limit = (limitEntity == null) ? 0 : limitEntity.limitAmount;

            int spent = db.expenseDao().sumForMonth(childId, range[0], range[1]);

            runOnUiThread(() -> {
                currentLimit = limit;
                currentSpent = spent;
                renderOverview(currentSpent, currentLimit, mk);
            });
        });
    }

    private void renderOverview(int spent, double limit, String mk) {
        if (tvBudgetProgress == null || tvBudgetStatus == null || tvBudgetMonth == null) return;

        DecimalFormat df = new DecimalFormat("#,##0.##");
        tvBudgetProgress.setText(df.format(spent) + " / " + df.format(limit));

        String status;
        if (limit <= 0) status = "No limit set";
        else if (spent <= limit) status = "On Track";
        else status = "Limit Exceeded";

        tvBudgetStatus.setText("Status: " + status);
        tvBudgetMonth.setText("Month: " + displayMonth(mk));
    }

    private String displayMonth(String mk) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            return out.format(in.parse(mk));
        } catch (Exception e) {
            return mk;
        }
    }

    // ---------------- Report Generation ----------------
    private void generateAndShareReport() {
        if (childId == 0) {
            toast("No linked child found");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            String mk = monthKey();
            long[] range = monthRangeMillis();

            int totalSpent =
                    db.expenseDao().sumForMonth(childId, range[0], range[1]);

            List<ExpenseEntity> list =
                    db.expenseDao().getExpensesForMonth(childId, range[0], range[1]);

            StringBuilder sb = new StringBuilder();
            sb.append("Child Expense Report\n\n");
            sb.append("Month: ").append(mk).append("\n");
            sb.append("Child ID: ").append(childId).append("\n");
            sb.append("Total Spent: ").append(totalSpent).append(" PKR\n");
            sb.append("Generated: ")
                    .append(new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a", Locale.getDefault()
                    ).format(new Date()))
                    .append("\n\n");

            if (list == null || list.isEmpty()) {
                sb.append("No expenses recorded.\n");
            } else {
                SimpleDateFormat sdf =
                        new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

                for (ExpenseEntity e : list) {
                    sb.append("- ")
                            .append(e.category)
                            .append(" | ")
                            .append(e.amount).append(" PKR")
                            .append(" | ")
                            .append(sdf.format(new Date(e.createdAt)))
                            .append("\n");
                }
            }

            try {
                File dir = new File(getExternalFilesDir(null), "reports");
                if (!dir.exists()) dir.mkdirs();

                File reportFile =
                        new File(dir, "expense_report_" + mk + "_child_" + childId + ".txt");

                try (FileOutputStream fos = new FileOutputStream(reportFile)) {
                    fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                }

                Uri uri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        reportFile
                );

                runOnUiThread(() -> shareFile(uri));

            } catch (Exception e) {
                runOnUiThread(() -> toast("Report failed"));
            }
        });
    }

    private void shareFile(Uri uri) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share Expense Report"));
    }

    // ---------------- Helpers ----------------
    private String monthKey() {
        Calendar c = Calendar.getInstance();
        return String.format(
                Locale.US, "%04d-%02d",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1
        );
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

    private Double parseDouble(TextInputEditText et) {
        try {
            String s = et.getText() == null ? "" : et.getText().toString().trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentExpenses();
        loadBudgetOverview(); // ✅ refresh overview on return
    }
}
