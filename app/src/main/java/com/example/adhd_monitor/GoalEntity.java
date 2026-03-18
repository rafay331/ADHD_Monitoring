package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class GoalEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long userId;
    public String title;
    public Integer targetMinutesPerDay; // nullable
    public Long deadline;               // epoch millis nullable
    public boolean achieved;
}
