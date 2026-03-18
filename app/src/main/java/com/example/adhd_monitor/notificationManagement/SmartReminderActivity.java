package com.example.adhd_monitor.notificationManagement;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.adhd_monitor.R;

import java.util.concurrent.TimeUnit;

public class SmartReminderActivity extends AppCompatActivity {

    private Switch switchRecurringReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_reminder);

        switchRecurringReminder = findViewById(R.id.switchRecurringReminder);

        switchRecurringReminder.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                scheduleRecurringReminder();
            } else {
                cancelRecurringReminder();
            }
        });
    }

    private void scheduleRecurringReminder() {
        PeriodicWorkRequest reminderWork =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecurringReminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWork
        );

        Toast.makeText(this, "Recurring reminders enabled!", Toast.LENGTH_SHORT).show();
    }

    private void cancelRecurringReminder() {
        WorkManager.getInstance(this).cancelUniqueWork("RecurringReminder");
        Toast.makeText(this, "Recurring reminders disabled!", Toast.LENGTH_SHORT).show();
    }
}
