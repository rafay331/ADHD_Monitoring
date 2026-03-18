package com.example.adhd_monitor.notificationManagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Broadcast received: Triggering notification");

        // Fetch priority and notification type from shared preferences or pass hardcoded values
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        // You can adjust these default values to whatever fits the logic of your app
        String prioritySetting = sharedPreferences.getString("notification_priority", "Medium");
        String notificationType = sharedPreferences.getString("notification_type", "Sound");

        // Send notification with the correct priority and notification type
        NotificationHelper.sendNotification(context, "Focus Reminder", "Time to focus and avoid distractions!", prioritySetting, notificationType);
    }
}
