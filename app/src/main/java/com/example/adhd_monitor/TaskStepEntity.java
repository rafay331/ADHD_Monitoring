package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.adhd_monitor.TaskEntity;

@Entity(
        tableName = "task_steps",
        foreignKeys = @ForeignKey(
                entity = TaskEntity.class,
                parentColumns = "taskId",
                childColumns = "taskOwnerId",
                onDelete = ForeignKey.CASCADE
        )
)
public class TaskStepEntity {

    @PrimaryKey(autoGenerate = true)
    public long stepId;

    @ColumnInfo(index = true) // optional for query speed
    public long taskOwnerId;

    public String stepText;
    public boolean isDone;
}
