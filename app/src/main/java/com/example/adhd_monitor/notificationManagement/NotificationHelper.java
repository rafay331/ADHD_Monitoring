package com.example.adhd_monitor.notificationManagement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.adhd_monitor.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "adhd_notifications_channel";

    // Create Notification Channel (only required for Android O and above)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ADHD Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This channel is used for ADHD reminders and alerts.");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Updated sendNotification method to handle priority and notification type
    public static void sendNotification(Context context, String title, String message, String prioritySetting, String notificationType) {
        // Ensure the channel is created before sending notifications
        createNotificationChannel(context);

        // Determine priority level
        int priority;
        int importance;
        boolean playSound = false;

        switch (prioritySetting) {
            case "High":
                priority = NotificationCompat.PRIORITY_HIGH;
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            case "Low":
                priority = NotificationCompat.PRIORITY_LOW;
                importance = NotificationManager.IMPORTANCE_LOW;
                break;
            default:
                priority = NotificationCompat.PRIORITY_DEFAULT;
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
        }

        // Adjust sound and pop-up behavior based on Notification Type
        switch (notificationType) {
            case "Pop-Up":
                priority = NotificationCompat.PRIORITY_HIGH;
                playSound = true;
                break;
            case "Silent":
                priority = NotificationCompat.PRIORITY_LOW;
                break;
            default: // "Sound"
                playSound = true;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "ADHD Notifications", importance);
            channel.setDescription("This channel is used for ADHD reminders and alerts.");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)

                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setAutoCancel(true);

        // Apply sound if enabled
        if (playSound) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }

        // Show the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}
