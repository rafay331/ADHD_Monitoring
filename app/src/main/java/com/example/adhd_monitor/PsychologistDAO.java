package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PsychologistDAO {

    @Query("SELECT * FROM psychologist WHERE username = :username LIMIT 1")
    Psychologist getPsychologistByUsername(String username);

    @Query("SELECT * FROM psychologist WHERE email = :email LIMIT 1")
    Psychologist getPsychologistByEmail(String email);

    @Query("SELECT * FROM psychologist WHERE id = :psychologistId LIMIT 1")
    Psychologist getPsychologistById(int psychologistId);

    @Query("UPDATE psychologist SET username = :newUsername, password = :newPassword WHERE id = :psychologistId")
    void updatePsychologist(int psychologistId, String newUsername, String newPassword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPsychologist(Psychologist psychologist);

    @Update
    void updatePsychologist(Psychologist psychologist);
}
