package com.example.adhd_monitor.notificationManagement;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Trigger notification
        NotificationHelper.sendNotification(
                getApplicationContext(),
                "Recurring Reminder",
                "Time to refocus and stay on task!",
                "Medium",
                "Sound"
        );
        return Result.success();
    }
}
