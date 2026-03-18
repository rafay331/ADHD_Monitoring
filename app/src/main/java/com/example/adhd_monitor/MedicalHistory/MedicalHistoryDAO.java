package com.example.adhd_monitor.MedicalHistory;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicalHistoryDAO {

    @Insert
    void insertMedication(MedicationHistoryEntity med);

    @Insert
    void insertBehavior(BehavioralHistoryEntity beh);

    @Insert
    void insertPdf(MedicalHistoryPdfEntity pdf);

    @Query("SELECT * FROM medical_history_pdf" )
    List<MedicalHistoryPdfEntity> getAllPdfs();

    @Query("SELECT * FROM medical_history_pdf WHERE userId = :userId")
    List<MedicalHistoryPdfEntity> getPdfsByUserId(int userId);
    @Query("SELECT * FROM medication_history WHERE userId = :userId")
    List<MedicationHistoryEntity> getMedicationHistory(int userId);

    @Query("SELECT * FROM behavioral_history WHERE userId = :userId")
    List<BehavioralHistoryEntity> getBehavioralHistory(int userId);


}
