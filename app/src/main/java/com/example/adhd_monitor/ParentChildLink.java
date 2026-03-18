package com.example.adhd_monitor;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "parent_child_link",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "childId",
                onDelete = CASCADE
        )
)
public class ParentChildLink {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int parentId; // link to Parent.id (no FK needed unless used in join)

    @ColumnInfo(index = true)
    public int childId;  // foreign key to User.id

    @NonNull
    public String status;

    public ParentChildLink(int parentId, int childId, @NonNull String status) {
        this.parentId = parentId;
        this.childId = childId;
        this.status = status;
    }

    public int getId() { return id; }
    public int getParentId() { return parentId; }
    public int getChildId() { return childId; }
    public String getStatus() { return status; }
}
