package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.room.Room;

import com.example.adhd_monitor.MedicalHistory.MedicalHistoryInputActivity;
import com.example.adhd_monitor.Questionnaire.QuestionnaireActivity;
import com.example.adhd_monitor.TreatmentReport.ParentViewTreatmentReportsActivity;

import java.util.concurrent.Executors;

public class ParentDashboardActivity extends BaseActivity {
    private Button btnSettings, btnLogout, btnMoodHistory,btnTaskManagement, btnSendChildRequest,btnQuestionnaire, btnMedicalHistory, btnViewTreatmentReports, btnFocusMode, btnReportProgress, btnBudgetManagement;
    private int parentId;
    private TextView childName; // For displaying linked child's name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // DEBUG: Log intent data
        String receivedId = getIntent().getStringExtra("userId");
        Log.d("DEBUG_PARENT_DASH", "Received userId from intent: " + receivedId);

        // Validate parent ID
        if (receivedId != null) {
            try {
                parentId = Integer.parseInt(receivedId);
            } catch (NumberFormatException e) {
                parentId = -1;
            }
        } else {
            parentId = -1;
        }

        if (parentId == -1) {
            Toast.makeText(this, "Parent ID is missing. Returning to login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize UI
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
        btnSendChildRequest = findViewById(R.id.btnSendChildRequest);
        btnMedicalHistory = findViewById(R.id.btnMedicalHistory);
        btnViewTreatmentReports = findViewById(R.id.btnViewTreatmentReports);
        btnFocusMode = findViewById(R.id.btnFocusMode);
        btnQuestionnaire = findViewById(R.id.btnQuestionnaire);
        btnTaskManagement = findViewById(R.id.btnTaskManagement);
        btnMoodHistory = findViewById(R.id.btnMoodHistory);

        btnBudgetManagement = findViewById(R.id.btnBudgetManagement);
        btnReportProgress = findViewById(R.id.btnReportProgress);

        childName = findViewById(R.id.childName); // Link to TextView in XML

        // Load and display linked child username
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "adhd_monitor_db").allowMainThreadQueries().build();

        ParentChildLinkDao linkDao = db.parentChildLinkDao();
        String childUsername = linkDao.getLinkedChildUsername(parentId);
//        int childId = db.parentChildLinkDao().getLinkByParentId(parentId).getChildId();
//        ParentChildLink link = db.parentChildLinkDao().getLinkByParentId(parentId);

//        if (link != null && "accepted".equals(link.getStatus())) {
//            int childId = link.getChildId();
//            // Proceed to use childId (e.g., open questionnaire)
//        } else {
//            Toast.makeText(this, "No linked child found", Toast.LENGTH_SHORT).show();
//            // Optionally disable child-related features
//        }


        if (childUsername != null) {
            childName.setText("Linked Child: " + childUsername);
        } else {
            childName.setText("No child linked");
        }

        // Button listeners
        btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(ParentDashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnMedicalHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ParentDashboardActivity.this, MedicalHistoryInputActivity.class);
            intent.putExtra("userId", String.valueOf(parentId));
            startActivity(intent);
        });

        btnSendChildRequest.setOnClickListener(v -> {
            Intent intent = new Intent(ParentDashboardActivity.this, SendLinkRequestToChildActivity.class);
            intent.putExtra("userId", String.valueOf(parentId));
            startActivity(intent);
        });

        btnMoodHistory.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {

                // ✅ Get accepted link for this parent
                ParentChildLink link = db.parentChildLinkDao().getLinkByParentId(parentId);

                runOnUiThread(() -> {
                    if (link == null || !"accepted".equalsIgnoreCase(link.getStatus())) {
                        Toast.makeText(this, "No linked child found (Accepted).", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int childId = link.getChildId(); // ✅ child user id

                    Intent intent = new Intent(ParentDashboardActivity.this, MoodHistoryActivity.class);
                    intent.putExtra("userId", childId);  // ✅ MoodHistory will load entries of this child
                    startActivity(intent);
                });
            });
        });

        btnViewTreatmentReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentViewTreatmentReportsActivity.class);
            startActivity(intent);
        });

        btnTaskManagement.setOnClickListener(v -> {

                Intent intent = new Intent(ParentDashboardActivity.this, TaskManagementActivity.class);
                startActivity(intent);

        });
        btnReportProgress.setOnClickListener(v -> {
            Intent i = new Intent(ParentDashboardActivity.this, ReportProgressActivity.class);
            startActivity(i);
        });


        btnBudgetManagement.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentBudgetActivity.class);
            intent.putExtra("userId", String.valueOf(parentId));   // ✅ PASS parentId
            startActivity(intent);
        });

        btnFocusMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, FocusModeActivity.class);
            startActivity(intent);
        });
        btnQuestionnaire.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionnaireActivity.class);
            
            startActivity(intent);
        });


        btnLogout.setOnClickListener(view -> logout());
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(ParentDashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
