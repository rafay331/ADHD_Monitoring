package com.example.adhd_monitor.Questionnaire.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdhdReportDAO {
    @Insert
    void insertReport(AdhdReportEntity report);

    @Query("SELECT * FROM adhd_reports WHERE user_id = :userId")
    List<AdhdReportEntity> getReportsByUser(String userId);

    @Query("SELECT * FROM adhd_reports")
    List<AdhdReportEntity> getAllReports();

    @Update
    void updateReport(AdhdReportEntity report);
}

