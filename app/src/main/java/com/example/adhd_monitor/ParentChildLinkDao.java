package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParentChildLinkDao {

    @Insert
    void insert(ParentChildLink link);

    @Query("SELECT * FROM parent_child_link WHERE childId = :childId AND status = 'pending'")
    List<ParentChildLink> getPendingRequestsForChild(int childId);

    @Query("UPDATE parent_child_link SET status = :status WHERE id = :id")
    void updateLinkStatus(int id, String status);

    @Query("SELECT * FROM parent_child_link WHERE parentId = :parentId AND childId = :childId AND status = 'pending'")
    ParentChildLink getPendingRequestBetween(int parentId, int childId);

    @Query("SELECT * FROM parent_child_link WHERE parentId = :parentId AND status = 'accepted' LIMIT 1")
    ParentChildLink getLinkByParentId(int parentId);

    // ✅ NEW: Get linked child username from the User table
    @Query("SELECT u.username FROM users u " +
            "INNER JOIN parent_child_link pcl ON u.id = pcl.childId " +
            "WHERE pcl.parentId = :parentId AND pcl.status = 'accepted' LIMIT 1")
    String getLinkedChildUsername(int parentId);

}



