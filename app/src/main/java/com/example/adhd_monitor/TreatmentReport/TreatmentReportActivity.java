package com.example.adhd_monitor.TreatmentReport;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TreatmentReportActivity extends AppCompatActivity {

    private EditText editNote;
    private EditText editCopingPlan; // NEW
    private Button btnSave;

    private int userId = 1; // Replace with actual user ID
    private String psychologistName = ""; // Replace with actual psychologist name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_report);

        editNote = findViewById(R.id.editNote);
        editCopingPlan = findViewById(R.id.editCopingPlan); // NEW
        btnSave = findViewById(R.id.btnSaveAndShare);

        btnSave.setOnClickListener(v -> {
            String note = editNote.getText().toString().trim();
            String copingPlan = editCopingPlan.getText().toString().trim(); // NEW

            if (note.isEmpty()) {
                Toast.makeText(this, "Treatment note is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (copingPlan.isEmpty()) {
                Toast.makeText(this, "ADHD coping plan is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            TreatmentReportEntity report = new TreatmentReportEntity();
            report.userId = userId;
            report.psychologistName = psychologistName;
            report.note = note;
            report.copingPlan = copingPlan; // NEW
            report.dateGenerated = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

            new Thread(() -> {
                File pdfFile = TreatmentReportPdfGenerator.generate(this, report);
                if (pdfFile != null && pdfFile.exists()) {
                    report.filePath = pdfFile.getAbsolutePath();
                    AppDatabase.getInstance(this).treatmentReportDao().insert(report);

                    runOnUiThread(() -> sharePdf(pdfFile));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
    }

    private void sharePdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Send Treatment Report"));
    }
}
