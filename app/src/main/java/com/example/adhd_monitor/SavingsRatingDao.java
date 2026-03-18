package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SavingsRatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SavingsRatingEntity e);

    @Query("SELECT * FROM savings_rating WHERE userId = :userId AND monthKey = :monthKey LIMIT 1")
    SavingsRatingEntity get(long userId, String monthKey);
}
