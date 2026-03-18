package com.example.adhd_monitor;

import com.example.adhd_monitor.notificationManagement.NotificationHelper;
import com.example.adhd_monitor.notificationManagement.NotificationReceiver;
import com.example.adhd_monitor.notificationManagement.PriorityAlertsActivity;
import com.example.adhd_monitor.notificationManagement.ScheduledNotificationsActivity;
import com.example.adhd_monitor.notificationManagement.SmartReminderActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

public class SettingsActivity extends BaseActivity {

    private Spinner themeSpinner, spinnerNotificationType;
    private Button btnSave, btnSecurity, btnBackToHome;
    private Button btnScheduledNotifications, btnPriorityAlerts, btnSmartReminder;
    private Switch switchNotifications, switchAlerts;
    private String selectedTheme, selectedNotificationType;
    private SharedPreferences sharedPreferences;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        themeSpinner = findViewById(R.id.themeSpinner);
        spinnerNotificationType = findViewById(R.id.spinnerNotificationType);
        btnSave = findViewById(R.id.btnSave);
        btnSecurity = findViewById(R.id.btnSecurity);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchAlerts = findViewById(R.id.switchAlerts);

        // New feature buttons
        btnScheduledNotifications = findViewById(R.id.btnScheduledNotifications);
        btnPriorityAlerts = findViewById(R.id.btnPriorityAlerts);
        btnSmartReminder = findViewById(R.id.btnSmartReminder);

        sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
        NotificationHelper.createNotificationChannel(this);
        loadPreferences();

        // Populate Theme Spinner
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                this, R.array.theme_options, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        themeSpinner.setSelection(getThemePosition(sharedPreferences.getString("theme", "System Default")));
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTheme = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTheme = "System Default";
            }
        });

        // Populate Notification Type Spinner
        ArrayAdapter<CharSequence> notificationAdapter = ArrayAdapter.createFromResource(
                this, R.array.notification_types, android.R.layout.simple_spinner_item);
        notificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotificationType.setAdapter(notificationAdapter);

        spinnerNotificationType.setSelection(getNotificationTypePosition(sharedPreferences.getString("notification_type", "Sound")));
        spinnerNotificationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNotificationType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedNotificationType = "Sound";
            }
        });

        // Button click listeners
        btnSave.setOnClickListener(v -> savePreferences());
        btnSecurity.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ProfileActivity.class)));
//        btnBackToHome.setOnClickListener(v -> {
//            startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
//            finish();
//        });

        // New feature buttons click listeners
        btnScheduledNotifications.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ScheduledNotificationsActivity.class));
        });

        btnPriorityAlerts.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, PriorityAlertsActivity.class));
        });

        btnSmartReminder.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, SmartReminderActivity.class));
        });

        // Handle Notification Switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                scheduleNotification();
            } else {
                cancelNotification();
            }
        });
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("theme", selectedTheme);
        editor.putString("notification_type", selectedNotificationType);
        editor.putBoolean("notificationsEnabled", switchNotifications.isChecked());
        editor.putBoolean("alertsEnabled", switchAlerts.isChecked());
        editor.apply();

        Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadPreferences() {
        switchNotifications.setChecked(sharedPreferences.getBoolean("notificationsEnabled", true));
        switchAlerts.setChecked(sharedPreferences.getBoolean("alertsEnabled", true));
    }

    private void scheduleNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Notification scheduled!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification() {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Notification canceled!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getThemePosition(String theme) {
        switch (theme) {
            case "Dark Mode": return 1;
            case "Light Mode": return 2;
            default: return 0;
        }
    }

    private int getNotificationTypePosition(String type) {
        switch (type) {
            case "Pop-Up": return 1;
            case "Silent": return 2;
            default: return 0;
        }
    }
}
