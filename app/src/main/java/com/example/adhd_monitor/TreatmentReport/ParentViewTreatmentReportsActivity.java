package com.example.adhd_monitor.TreatmentReport;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.BaseActivity;
import com.example.adhd_monitor.R;

import java.util.List;

public class ParentViewTreatmentReportsActivity extends BaseActivity {

    RecyclerView recyclerView;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_view_treatment_reports);

        recyclerView = findViewById(R.id.treatmentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        new Thread(() -> {
            List<TreatmentReportEntity> reports = db.treatmentReportDao().getAllTreatmentReports();
            runOnUiThread(() -> recyclerView.setAdapter(new TreatmentReportAdapter(this, reports)));
        }).start();
    }
}
