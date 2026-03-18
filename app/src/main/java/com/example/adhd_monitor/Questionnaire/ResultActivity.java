package com.example.adhd_monitor.Questionnaire;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.BaseActivity;
import com.example.adhd_monitor.Questionnaire.database.AdhdReportEntity;
import com.example.adhd_monitor.R;
import com.example.adhd_monitor.ReportGenerator.PdfReportGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends BaseActivity {

    private TextView txtResult, txtScoreDetails;
    private int totalScore;
    private String userId;
    private List<Question> allQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Retrieve passed data
        totalScore = getIntent().getIntExtra("totalScore", 0);
        userId = getIntent().getStringExtra("userId");

        // Retrieve answered questions from intent
        allQuestions = (List<Question>) getIntent().getSerializableExtra("answeredQuestions");

        // UI Components
        txtResult = findViewById(R.id.txtResult);
        txtScoreDetails = findViewById(R.id.txtScoreDetails);
        Button btnGeneratePDF = findViewById(R.id.btnGeneratePDF);

        // Display result summary
        String spectrum = getSpectrumCategory(totalScore);
        txtResult.setText("ADHD Category: " + spectrum);
        txtScoreDetails.setText("Total Score: " + totalScore);

        // Handle PDF generation and sharing
        btnGeneratePDF.setOnClickListener(v -> {
            File pdfFile = PdfReportGenerator.generateReport(
                    ResultActivity.this,
                    totalScore,
                    spectrum,
                    allQuestions // ✅ use actual answered list
            );

            if (pdfFile != null) {
                // Save report to Room
                AdhdReportEntity report = new AdhdReportEntity();
                report.userId = userId;
                report.filePath = pdfFile.getAbsolutePath();
                report.date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                report.score = totalScore;
                report.spectrum = spectrum;
                report.comments = "";

                AppDatabase.getInstance(this).adhdReportDao().insertReport(report);

                // Share PDF
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Report"));
            } else {
                Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSpectrumCategory(int score) {
        if (score <= 10) return "No ADHD";
        else if (score <= 20) return "Mild ADHD";
        else if (score <= 30) return "Moderate ADHD";
        else return "Severe ADHD";
    }
}

