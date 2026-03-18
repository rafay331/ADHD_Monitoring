package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface MonthlyBudgetLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(com.example.adhd_monitor.MonthlyBudgetLimitEntity entity);

    @Query("SELECT * FROM monthly_budget_limit WHERE childUserId = :childId AND monthKey = :monthKey LIMIT 1")
    com.example.adhd_monitor.MonthlyBudgetLimitEntity getLimit(long childId, String monthKey);

    @Query("UPDATE monthly_budget_limit SET lockEnabled = :enabled, updatedAt = :updatedAt WHERE childUserId = :childId AND monthKey = :monthKey")
    void setLockEnabled(long childId, String monthKey, boolean enabled, long updatedAt);
}
