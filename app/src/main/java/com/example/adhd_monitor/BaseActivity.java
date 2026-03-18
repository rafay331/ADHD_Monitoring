package com.example.adhd_monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme(); // Apply theme BEFORE calling super.onCreate()
        super.onCreate(savedInstanceState);
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences("UserSettings", MODE_PRIVATE);

        String savedTheme = sharedPreferences.getString("theme", "System Default");

        if (savedTheme.equals("Dark Mode")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (savedTheme.equals("Light Mode")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
