package com.example.adhd_monitor.notificationManagement;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.adhd_monitor.BaseActivity;
import com.example.adhd_monitor.R;

public class PriorityAlertsActivity extends BaseActivity {

    private RadioGroup radioGroupPriority;
    private Button btnSendPriorityNotification;

    private static final String CRITICAL_CHANNEL_ID = "critical_alerts";
    private static final String REGULAR_CHANNEL_ID = "regular_alerts";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority_alerts);

        radioGroupPriority = findViewById(R.id.radioGroupPriority);
        btnSendPriorityNotification = findViewById(R.id.btnSendPriorityNotification);

        createNotificationChannels();

        btnSendPriorityNotification.setOnClickListener(v -> sendPriorityNotification());
    }

    private void sendPriorityNotification() {
        int selectedId = radioGroupPriority.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Please select a priority!", Toast.LENGTH_SHORT).show();
            return;
        }

        String channelId;
        String title;
        String message;

        if (selectedId == R.id.radioCritical) {
            channelId = CRITICAL_CHANNEL_ID;
            title = "\u26A0\uFE0F Critical Alert!";
            message = "This is a high-priority notification!";
        } else {
            channelId = REGULAR_CHANNEL_ID;
            title = "\uD83D\uDD14 Regular Alert";
            message = "This is a standard notification.";
        }

        // Android 13+ notification permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                Toast.makeText(this, "Notification permission required!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        sendNotification(channelId, title, message);
    }

    private void sendNotification(String channelId, String title, String message) {
        // Check for permission at call site (required for API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(channelId.equals(CRITICAL_CHANNEL_ID) ?
                        NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = (int) (System.currentTimeMillis() & 0xfffffff);
        notificationManager.notify(notificationId, builder.build());
        Toast.makeText(this, "Notification Sent!", Toast.LENGTH_SHORT).show();
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            NotificationChannel criticalChannel = new NotificationChannel(
                    CRITICAL_CHANNEL_ID, "Critical Alerts", NotificationManager.IMPORTANCE_HIGH);
            criticalChannel.setDescription("High priority alerts for important messages");

            NotificationChannel regularChannel = new NotificationChannel(
                    REGULAR_CHANNEL_ID, "Regular Alerts", NotificationManager.IMPORTANCE_DEFAULT);
            regularChannel.setDescription("Standard notifications");

            if (manager != null) {
                manager.createNotificationChannel(criticalChannel);
                manager.createNotificationChannel(regularChannel);
            }
        }
    }
}
