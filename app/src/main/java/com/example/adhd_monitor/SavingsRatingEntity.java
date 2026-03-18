package com.example.adhd_monitor;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "savings_rating",
        primaryKeys = {"userId", "monthKey"},
        indices = {@Index("userId")}
)
public class SavingsRatingEntity {

    public long userId;              // child/user id

    @NonNull
    public String monthKey;          // "YYYY-MM"

    public double limitAmount;
    public int spentAmount;
    public double savedAmount;

    public int stars;                // 1..5
    public long updatedAt;

    public SavingsRatingEntity(long userId,
                               @NonNull String monthKey,
                               double limitAmount,
                               int spentAmount,
                               double savedAmount,
                               int stars,
                               long updatedAt) {
        this.userId = userId;
        this.monthKey = monthKey;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.savedAmount = savedAmount;
        this.stars = stars;
        this.updatedAt = updatedAt;
    }
}
