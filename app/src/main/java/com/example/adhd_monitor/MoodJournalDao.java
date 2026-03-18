package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.adhd_monitor.MoodJournalEntity;

import java.util.List;

@Dao
public interface MoodJournalDao {

    @Insert
    long insert(MoodJournalEntity entry);

    @Query("SELECT * FROM mood_journal WHERE userId = :userId ORDER BY createdAt DESC")
    List<MoodJournalEntity> getAllByUser(int userId);

    @Query("DELETE FROM mood_journal WHERE userId = :userId")
    void deleteAllByUser(int userId);
}
