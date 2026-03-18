package com.example.adhd_monitor.TreatmentReport;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "treatment_reports")
public class TreatmentReportEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String psychologistName;
    public String note;
    public String copingPlan;
    public String dateGenerated;
    public String filePath;
}
