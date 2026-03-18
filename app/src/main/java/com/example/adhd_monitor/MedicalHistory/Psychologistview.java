package com.example.adhd_monitor.MedicalHistory;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.R;

import java.util.List;

public class Psychologistview extends AppCompatActivity {
    private RecyclerView pdfRecyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_pdf_reports);

        pdfRecyclerView = findViewById(R.id.pdfRecyclerView);
        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<MedicalHistoryPdfEntity> reports = AppDatabase.getInstance(this)
                .medicalHistoryDao()
                .getAllPdfs();

        MedicalPdfReportAdapter adapter = new MedicalPdfReportAdapter(this, reports);

        pdfRecyclerView.setAdapter(adapter);
    }
}
