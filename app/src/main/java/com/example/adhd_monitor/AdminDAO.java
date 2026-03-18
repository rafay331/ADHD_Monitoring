package com.example.adhd_monitor;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface AdminDAO {
    @Query("SELECT * FROM admin_table WHERE username = :username LIMIT 1")
    Admin getAdminByUsername(String username);

    @Insert
    void insert(Admin admin);
}
