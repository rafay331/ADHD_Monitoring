package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;           // child user
    public int amount;            // PKR
    public String category;
    public long createdAt;        // millis (System.currentTimeMillis)

    public ExpenseEntity(long userId, int amount, String category, long createdAt) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
    }
}
