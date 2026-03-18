package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FocusModeActivity
 * - Uses SharedPreferences to track an ACTIVE session (start time, duration, childId, activity type)
 * - On STOP, persists the session into Room (focus_sessions) so the Report module can read it
 * - Assumes FocusSessionEntity has: userId (long), startTime (long), endTime (long), distractions (int)
 */
public class FocusModeActivity extends AppCompatActivity {

    // UI
    private Spinner childSelector, durationSpinner, activityTypeSpinner;
    private TimePicker startTimePicker; // optional visual control (not used for persistence)
    private Switch blockDistractionsSwitch;
    private Button startFocusButton, stopSessionButton, btnOpenAcc;
    private TextView focusStatus, remainingTime, serviceStatus;

    // Persistence (temp state for the running session)
    private static final String PREFS = "focus_mode";
    private static final String K_ACTIVE  = "active";
    private static final String K_START   = "start_at_ms";
    private static final String K_CHILDID = "child_id";
    private static final String K_DURATION_MIN = "duration_min";
    private static final String K_ACTIVITY = "activity_type";
    private static final String K_DISTRACTIONS = "distractions"; // if you count them live

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focusmode);

        // Init UI
        childSelector = findViewById(R.id.childSelector);
        durationSpinner = findViewById(R.id.durationSpinner);
        activityTypeSpinner = findViewById(R.id.activityTypeSpinner);
        startTimePicker = findViewById(R.id.startTimePicker);
        blockDistractionsSwitch = findViewById(R.id.blockDistractionsSwitch);
        startFocusButton = findViewById(R.id.startFocusButton);
        stopSessionButton = findViewById(R.id.stopSessionButton);
        focusStatus = findViewById(R.id.focusStatus);
        remainingTime = findViewById(R.id.remainingTime);
        btnOpenAcc = findViewById(R.id.btnOpenAcc);
        serviceStatus = findViewById(R.id.serviceStatus);

        renderStatus();

        // Open Accessibility settings
        btnOpenAcc.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(i);
            Toast.makeText(this,
                    "Scroll to 'ADHD Monitor - Focus Service' and enable it.",
                    Toast.LENGTH_LONG).show();
        });

        // Start focus mode
        startFocusButton.setOnClickListener(v -> {
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Enable the Focus Accessibility Service first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!blockDistractionsSwitch.isChecked()) {
                Toast.makeText(this, "Please enable 'Block Distractions' first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isActive()) {
                Toast.makeText(this, "Focus Mode is already active.", Toast.LENGTH_SHORT).show();
                return;
            }

            long childId = getSelectedChildId();         // << fill from your spinner model
            if (childId == 0L) {
                Toast.makeText(this, "Select a child first.", Toast.LENGTH_SHORT).show();
                return;
            }

            int durationMin = getSelectedDurationMinutes();
            String activityType = getSelectedActivityType();

            long now = System.currentTimeMillis();
            saveActiveSession(now, childId, durationMin, activityType);

            FocusState.setActive(this, true); // your existing flag (optional but kept)

            Toast.makeText(this, "Focus Mode started. Blocking enabled.", Toast.LENGTH_SHORT).show();
            renderStatus();
        });

        // Stop focus mode (persists session to Room)
        stopSessionButton.setOnClickListener(v -> stopAndPersistSession());
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderStatus();
    }

    private void renderStatus() {
        boolean active = isActive();
        focusStatus.setText(active ? "Status: Active" : "Status: Not Active");

        if (active) {
            long start = getPrefs().getLong(K_START, 0);
            int durMin = getPrefs().getInt(K_DURATION_MIN, 0);
            long endPlanned = start + durMin * 60_000L;
            long remainMs = Math.max(0, endPlanned - System.currentTimeMillis());
            int remainMin = (int) Math.ceil(remainMs / 60000.0);
            remainingTime.setText("Remaining: " + remainMin + " min");
        } else {
            remainingTime.setText("Remaining: --");
        }

        boolean accEnabled = isAccessibilityServiceEnabled();
        serviceStatus.setText(accEnabled ? "Accessibility Service: Enabled"
                : "Accessibility Service: Disabled");
    }

    // Persist finished session into Room
    private void stopAndPersistSession() {
        if (!isActive()) {
            Toast.makeText(this, "No active session.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences p = getPrefs();
        final long startAt = p.getLong(K_START, 0);
        final long childId = p.getLong(K_CHILDID, 0);
        final int distractions = p.getInt(K_DISTRACTIONS, 0);
        final long endAt = System.currentTimeMillis();

        // Clear active state first (so UI updates immediately)
        clearActiveSession();
        FocusState.setActive(this, false);

        // Persist to DB (background)
        if (startAt > 0 && childId > 0 && endAt > startAt) {
            io.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                FocusSessionDao dao = db.focusSessionDao();

                FocusSessionEntity s = new FocusSessionEntity();
                s.userId = childId;            // NOTE: Report expects userId=childId
                s.startTime = startAt;         // stored in millis
                s.endTime = endAt;             // stored in millis
                s.distractions = distractions; // if you tracked any

                dao.insert(s);
            });
        }

        Toast.makeText(this, "Focus Mode stopped.", Toast.LENGTH_SHORT).show();
        renderStatus();
    }

    /* -------------------- Helpers -------------------- */

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private boolean isActive() {
        return getPrefs().getBoolean(K_ACTIVE, false);
    }

    private void saveActiveSession(long startAtMs, long childId, int durationMin, String activityType) {
        getPrefs().edit()
                .putBoolean(K_ACTIVE, true)
                .putLong(K_START, startAtMs)
                .putLong(K_CHILDID, childId)
                .putInt(K_DURATION_MIN, durationMin)
                .putString(K_ACTIVITY, activityType)
                .putInt(K_DISTRACTIONS, 0)
                .apply();
    }

    private void clearActiveSession() {
        getPrefs().edit()
                .remove(K_ACTIVE)
                .remove(K_START)
                .remove(K_CHILDID)
                .remove(K_DURATION_MIN)
                .remove(K_ACTIVITY)
                .remove(K_DISTRACTIONS)
                .apply();
    }

    private int getSelectedDurationMinutes() {
        // Example: spinner items "15 min", "30 min", "45 min", "60 min"
        Object item = durationSpinner.getSelectedItem();
        if (item == null) return 30; // default
        String s = String.valueOf(item).replaceAll("[^0-9]", "");
        try { return Integer.parseInt(s); } catch (Exception e) { return 30; }
    }

    private String getSelectedActivityType() {
        Object item = activityTypeSpinner.getSelectedItem();
        return item == null ? "General" : item.toString();
    }

    private long getSelectedChildId() {
        // TODO: wire with your adapter/model for childSelector.
        // For now, if you store id in view tag:
        Object tag = childSelector.getTag();
        if (tag instanceof Long) return (Long) tag;
        // Or map by position from your list
        return 1L; // fallback for testing
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            int enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            if (enabled == 1) {
                String services = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                return services != null && services.contains(getPackageName());
            }
        } catch (Settings.SettingNotFoundException ignored) {}
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
