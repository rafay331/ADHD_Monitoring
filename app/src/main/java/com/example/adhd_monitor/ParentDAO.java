package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ParentDAO {

    @Query("SELECT * FROM parent_table WHERE username = :username LIMIT 1")
    Parent getParentByUsername(String username);

    @Query("SELECT * FROM parent_table WHERE email = :email LIMIT 1")
    Parent getParentByEmail(String email);

    @Query("SELECT * FROM parent_table WHERE id = :parentId LIMIT 1")
    Parent getParentById(int parentId);

    @Query("UPDATE parent_table SET username = :newUsername, password = :newPassword WHERE id = :parentId")
    void updateParent(int parentId, String newUsername, String newPassword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParent(Parent parent);

    @Update
    void updateParent(Parent parent);
}
