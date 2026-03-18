package com.example.adhd_monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        RecyclerView rv = findViewById(R.id.rvMoodHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Use your app convention:
        // 1) intent extra: "userId"
        // 2) fallback: login_prefs -> "user_id"
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            userId = loginPrefs.getInt("user_id", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            MoodJournalDao dao = AppDatabase.getInstance(getApplicationContext()).moodJournalDao();
            List<MoodJournalEntity> list = dao.getAllByUser(userId);

            runOnUiThread(() -> rv.setAdapter(new MoodJournalAdapter(list)));
        }).start();
    }
}
