package com.example.adhd_monitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.adhd_monitor.MedicalHistory.MedicalHistoryInputActivity;
import com.example.adhd_monitor.Questionnaire.QuestionnaireActivity;

import java.util.List;

public class HomeActivity extends BaseActivity {

    private Button btnSettings, btnLogout, btnQuestionnaire,btnCalmNow, btnMoodJournal, btnMedicalHistory,  btnCommunitySupport,btnViewTasks, btnBudgetManagement;
    private SharedPreferences sharedPreferences;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = AppDatabase.getInstance(this);

        // Get user ID from intent or SharedPreferences
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            userId = loginPrefs.getInt("user_id", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "User ID missing. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Log.d("HOME_USER_ID", "Logged-in child user ID: " + userId);

        // UI Initialization
        btnSettings = findViewById(R.id.btnSettings);
        btnCommunitySupport = findViewById(R.id.btnCommunitySupport);
        btnLogout = findViewById(R.id.logoutButton);
        btnQuestionnaire = findViewById(R.id.btnQuestionnaire);
        btnCalmNow = findViewById(R.id.btnCalmNow);
        btnMedicalHistory = findViewById(R.id.btnMedicalHistory);
        btnViewTasks = findViewById(R.id.btnViewTasks);
        btnBudgetManagement = findViewById(R.id.btnBudgetManagement);
        btnMoodJournal = findViewById(R.id.btnMoodJournal);

        sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE);

        checkNotificationPermission();
        showPendingParentRequestsPopup(userId);

        btnQuestionnaire.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QuestionnaireActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnBudgetManagement.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PatientBudgetActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnCommunitySupport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CommunityChatActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnMoodJournal.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodJournalActivity.class);
            intent.putExtra("userId", userId); // ✅ same key you already use
            startActivity(intent);
        });

        btnCalmNow.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalmNowActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });



        btnMedicalHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MedicalHistoryInputActivity.class);
            intent.putExtra("userId",  userId); // Pass the child/user ID if you have it
            startActivity(intent);
        });
        long loggedInChildId = getIntent().getLongExtra("user_id", -1L);
        btnViewTasks.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserTasksActivity.class);
            intent.putExtra("userID", userId);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkNotificationPermission() {
        boolean isFirstTime = sharedPreferences.getBoolean("firstTime", true);
        if (isFirstTime) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Notifications")
                    .setMessage("Would you like to receive notifications?")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        sharedPreferences.edit()
                                .putBoolean("notificationsEnabled", true)
                                .putBoolean("firstTime", false)
                                .apply();
                        Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Deny", (dialog, which) -> {
                        sharedPreferences.edit()
                                .putBoolean("notificationsEnabled", false)
                                .putBoolean("firstTime", false)
                                .apply();
                        Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void showPendingParentRequestsPopup(int childId) {
        new Thread(() -> {
            List<ParentChildLink> pendingLinks = db.parentChildLinkDao().getPendingRequestsForChild(childId);
            if (pendingLinks == null || pendingLinks.isEmpty()) return;

            ParentChildLink firstRequest = pendingLinks.get(0);
            Parent parent = db.parentDao().getParentById(firstRequest.getParentId());
            if (parent == null) return;

            runOnUiThread(() -> {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Parent Link Request")
                        .setMessage("Do you want to accept request from " + parent.getUsername() + "?")
                        .setPositiveButton("Accept", (dialog, which) -> {
                            db.parentChildLinkDao().updateLinkStatus(firstRequest.getId(), "accepted");
                            Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Reject", (dialog, which) -> {
                            db.parentChildLinkDao().updateLinkStatus(firstRequest.getId(), "rejected");
                            Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                        })
                        .setCancelable(false)
                        .show();
            });
        }).start();
    }
}
