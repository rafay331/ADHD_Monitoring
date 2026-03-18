package com.example.adhd_monitor;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "monthly_budget_limit",
        primaryKeys = {"childUserId", "monthKey"},
        indices = {@Index(value = {"childUserId", "monthKey"}, unique = true)}
)
public class MonthlyBudgetLimitEntity {
    @NonNull
    public long childUserId;

    @NonNull
    public String monthKey; // "YYYY-MM"

    public double limitAmount; // 0 = no limit
    public boolean lockEnabled; // true = enforce limit

    public long updatedAt; // millis

    public MonthlyBudgetLimitEntity(long childUserId, @NonNull String monthKey,
                                    double limitAmount, boolean lockEnabled, long updatedAt) {
        this.childUserId = childUserId;
        this.monthKey = monthKey;
        this.limitAmount = limitAmount;
        this.lockEnabled = lockEnabled;
        this.updatedAt = updatedAt;
    }
}
