package com.example.adhd_monitor.Questionnaire.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "adhd_reports")
public class AdhdReportEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "spectrum")
    public String spectrum;

    @ColumnInfo(name = "comments")
    public String comments; // For psychologist's notes
}
