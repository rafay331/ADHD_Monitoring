package com.example.adhd_monitor.MedicalHistory;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.adhd_monitor.MedicalHistory.MedicalHistoryPdfEntity;
import androidx.core.content.FileProvider;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.BaseActivity;
import com.example.adhd_monitor.R;
import com.example.adhd_monitor.ReportGenerator.PdfReportGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalHistoryInputActivity extends BaseActivity {
    private MedicationHistoryEntity latestMedEntry;


    private EditText editDate, editField1, editField2, editNotes;
    private Spinner spinnerType;
    private Button btnSave, btnGeneratePdf;
    private AppDatabase db;
    private int userId; // Set this after login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history_input);

        editDate = findViewById(R.id.editDate);
        editField1 = findViewById(R.id.editField1);
        editField2 = findViewById(R.id.editField2);
        editNotes = findViewById(R.id.editNotes);
        spinnerType = findViewById(R.id.spinnerHistoryType);
        btnSave = findViewById(R.id.btnSave);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        db = AppDatabase.getInstance(this);

        editDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveEntry());
        btnGeneratePdf.setOnClickListener(v -> generatePdf());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selected = dayOfMonth + "/" + (month + 1) + "/" + year;
            editDate.setText(selected);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEntry() {
        String type = spinnerType.getSelectedItem().toString();
        String date = editDate.getText().toString();
        String field1 = editField1.getText().toString();
        String field2 = editField2.getText().toString();
        String notes = editNotes.getText().toString();

        if (type.equals("Medication History")) {
            latestMedEntry = new MedicationHistoryEntity();

            latestMedEntry.userId = userId;
            latestMedEntry.date = date;
            latestMedEntry.medicineName = field1;
            latestMedEntry.dosage = field2;
            latestMedEntry.notes = notes;
            new Thread(() -> db.medicalHistoryDao().insertMedication(latestMedEntry)).start();

        } else {
            BehavioralHistoryEntity beh = new BehavioralHistoryEntity();
            beh.userId = userId;
            beh.date = date;
            beh.symptoms = field1;
            beh.notes = field2 + " | " + notes;
            new Thread(() -> db.medicalHistoryDao().insertBehavior(beh)).start();
        }

        Toast.makeText(this, "Entry Saved", Toast.LENGTH_SHORT).show();
    }

    private void generatePdf() {
        String type = spinnerType.getSelectedItem().toString();
        new Thread(() -> {
            File pdf = null;

            if (type.equals("Medication History")) {
                if (latestMedEntry != null) {
                    List<MedicationHistoryEntity> oneEntry = new ArrayList<>();
                    oneEntry.add(latestMedEntry);
                    pdf = PdfReportGenerator.generateMedicationHistoryPdf(this, oneEntry);
                }
            } else {
                List<BehavioralHistoryEntity> behs = db.medicalHistoryDao().getBehavioralHistory(userId);
                pdf = PdfReportGenerator.generateBehavioralHistoryPdf(this, behs);
            }

            if (pdf != null) {
                // Save metadata to Room
                MedicalHistoryPdfEntity report = new MedicalHistoryPdfEntity();
                report.userId = userId;
                report.type = type.equals("Medication History") ? "Medication" : "Behavioral";
                report.filePath = pdf.getAbsolutePath();
                report.dateGenerated = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                db.medicalHistoryDao().insertPdf(report);

                // ✅ Share PDF
                File finalPdf = pdf;
                runOnUiThread(() -> {
                    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", finalPdf);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share Medical Report"));
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }



    private void openPdf(File file) {
        runOnUiThread(() -> {
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // ✅ Explicitly grant permission to all PDF viewers
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
