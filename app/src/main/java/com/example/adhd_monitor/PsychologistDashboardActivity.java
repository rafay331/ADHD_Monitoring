package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.adhd_monitor.MedicalHistory.Psychologistview;
import com.example.adhd_monitor.Questionnaire.PsychologistReportsActivity;
import com.example.adhd_monitor.TreatmentReport.TreatmentReportActivity;

public class PsychologistDashboardActivity extends BaseActivity {

    private Button btnViewAppointments, btnViewUserReports, btnLogout, btnSettings,btnTreatmentReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psychologist_dashboard);

        btnViewAppointments = findViewById(R.id.btnViewAppointments);
        btnViewUserReports = findViewById(R.id.btnViewUserReports);
        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);
        Button btnTreatmentReport = findViewById(R.id.btnTreatmentReport);

        btnViewAppointments.setOnClickListener(view ->{
            Intent intent = new Intent(PsychologistDashboardActivity.this, Psychologistview.class);
                    startActivity(intent);

                }

        );

        btnTreatmentReport.setOnClickListener(v -> {
            Intent intent = new Intent(PsychologistDashboardActivity.this, TreatmentReportActivity.class);
            startActivity(intent);
        });

        btnViewUserReports.setOnClickListener(view -> {
            Intent intent = new Intent(PsychologistDashboardActivity.this, PsychologistReportsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> logout());

        btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(PsychologistDashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(PsychologistDashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

