package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions")
public class FocusSessionEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long userId;

    // epoch millis
    public long startTime;
    public long endTime;

    public int distractions; // number of distraction events captured during session
}
