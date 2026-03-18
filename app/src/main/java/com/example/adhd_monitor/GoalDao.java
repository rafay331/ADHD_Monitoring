package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY achieved ASC, deadline ASC")
    List<GoalEntity> getGoals(long userId);
}

