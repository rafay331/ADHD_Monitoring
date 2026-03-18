package com.example.adhd_monitor.MedicalHistory;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medication_history")
public class MedicationHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;


    public int userId;
    public String date;
    public String medicineName;
    public String dosage;
    public String notes;
}
