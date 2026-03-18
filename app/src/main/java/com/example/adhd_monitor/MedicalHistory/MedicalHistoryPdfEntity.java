package com.example.adhd_monitor.MedicalHistory;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medical_history_pdf")
public class MedicalHistoryPdfEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String type;  // "Medication" or "Behavioral"
    public String filePath;
    public String dateGenerated;
}

