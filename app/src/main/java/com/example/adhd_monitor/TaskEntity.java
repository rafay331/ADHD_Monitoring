package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "tasks")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    public long taskId;

    // Who is supposed to do this task (the child / user)
    @ColumnInfo(index = true)
    public long childId;

    // Which parent assigned it
    @ColumnInfo(index = true)
    public long parentId;

    // Main title / description of the task
    @ColumnInfo
    public String title;

    // Deadline/time to finish (millis since epoch)
    @ColumnInfo
    public long dueAtMillis;

    // e.g. "High", "Medium", "Low"
    @ColumnInfo
    public String priority;

    // e.g. "Study", "Chores", "Health", etc.
    @ColumnInfo
    public String category;

    // Reminder settings
    @ColumnInfo
    public boolean remindersEnabled;

    // How many minutes before due time we should remind
    @ColumnInfo
    public int reminderLeadMinutes;

    // Task completion state
    @ColumnInfo
    public boolean done;

    // When the task row was created (for sorting/history)
    @ColumnInfo
    public long createdAtMillis;

    // When child actually completed it (0 if not completed yet)
    @ColumnInfo
    public long completedAtMillis;

    // Optional: total points earned for completing this task
    @ColumnInfo
    public int pointsEarned;
}
