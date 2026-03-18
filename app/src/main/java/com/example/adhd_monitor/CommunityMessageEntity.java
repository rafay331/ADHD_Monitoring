package com.example.adhd_monitor;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "community_messages",
        indices = {@Index(value = "userId")}
)
public class CommunityMessageEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    @NonNull
    public String username;

    @NonNull
    public String message;

    // budget rating (1–5)
    public int budgetRating;

    // points shown in chat
    public int participationPoints;
    public int encouragementCount;


    public long createdAt; // System.currentTimeMillis()

    public CommunityMessageEntity(int userId, @NonNull String username, @NonNull String message,
                                  int budgetRating, int participationPoints, long createdAt) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.budgetRating = budgetRating;
        this.participationPoints = participationPoints;
        this.createdAt = createdAt;
        this.encouragementCount = 0; // ✅ default
    }

}
