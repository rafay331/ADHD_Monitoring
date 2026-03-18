package com.example.adhd_monitor.TreatmentReport;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TreatmentReportDAO {
    @Insert
    void insert(TreatmentReportEntity report);

    @Query("SELECT * FROM treatment_reports WHERE userId = :userId ORDER BY id DESC")
    List<TreatmentReportEntity> getReportsByUser(int userId);

    @Query("SELECT * FROM treatment_reports ORDER BY id DESC")
    List<TreatmentReportEntity> getAllTreatmentReports();
}
