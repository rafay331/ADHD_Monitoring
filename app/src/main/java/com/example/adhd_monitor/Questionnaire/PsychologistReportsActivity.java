package com.example.adhd_monitor.Questionnaire;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.Questionnaire.database.AdhdReportEntity;
import com.example.adhd_monitor.R;

import java.util.List;

public class PsychologistReportsActivity extends AppCompatActivity {

    private RecyclerView reportsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psychologist_reports);

        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<AdhdReportEntity> reports = AppDatabase.getInstance(this)
                .adhdReportDao()
                .getAllReports();

        ReportAdapter adapter = new ReportAdapter(this, reports);
        reportsRecyclerView.setAdapter(adapter);
    }
}

