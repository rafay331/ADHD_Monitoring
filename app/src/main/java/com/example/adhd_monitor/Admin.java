package com.example.adhd_monitor;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "admin_table")
public class Admin {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }
}