package com.example.adhd_monitor.MedicalHistory;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "behavioral_history")
public class BehavioralHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String date;
    public String symptoms;
    public String notes;
}

