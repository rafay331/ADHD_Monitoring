package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



public class MoodJournalActivity extends AppCompatActivity {

    private String selectedMood = null;
    private int intensity = 3;

    private int userId = -1;

    // ✅ Cooldown settings (30 minutes)
    private static final String ALERT_PREFS = "anger_alert_prefs";
    private static final String KEY_LAST_ALERT_TIME = "last_anger_alert_time";
    private static final long ALERT_COOLDOWN_MS = 30L * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_journal);

        Button btnSad = findViewById(R.id.btnSad);
        Button btnNeutral = findViewById(R.id.btnNeutral);
        Button btnHappy = findViewById(R.id.btnHappy);
        Button btnExcited = findViewById(R.id.btnExcited);
        Button btnAngry = findViewById(R.id.btnAngry);

        TextView tvSelectedMood = findViewById(R.id.tvSelectedMood);
        TextView tvIntensity = findViewById(R.id.tvIntensity);
        SeekBar seekIntensity = findViewById(R.id.seekIntensity);
        EditText etNote = findViewById(R.id.etNote);
        Button btnSave = findViewById(R.id.btnSave);

        // ✅ Get userId (same convention as your app)
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            userId = loginPrefs.getInt("user_id", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // SeekBar 0..4 => intensity 1..5
        seekIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intensity = progress + 1;
                tvIntensity.setText("Intensity: " + intensity);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Mood selection
        btnSad.setOnClickListener(v -> setMood("Sad", tvSelectedMood));
        btnNeutral.setOnClickListener(v -> setMood("Neutral", tvSelectedMood));
        btnHappy.setOnClickListener(v -> setMood("Happy", tvSelectedMood));
        btnExcited.setOnClickListener(v -> setMood("Excited", tvSelectedMood));
        btnAngry.setOnClickListener(v -> setMood("Angry", tvSelectedMood));

        btnSave.setOnClickListener(v -> {
            if (selectedMood == null) {
                Toast.makeText(this, "Please select a mood.", Toast.LENGTH_SHORT).show();
                return;
            }

            String note = etNote.getText().toString().trim();
            long createdAt = System.currentTimeMillis();

            MoodJournalEntity entry = new MoodJournalEntity(userId, selectedMood, intensity, note, createdAt);

            new Thread(() -> {
                MoodJournalDao dao = AppDatabase.getInstance(getApplicationContext()).moodJournalDao();
                dao.insert(entry);

                runOnUiThread(() -> {
                    Toast.makeText(MoodJournalActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

                    // ✅ Rule A: Angry + intensity >= 4 => show alert (with cooldown)
                    if (shouldTriggerAngerAlert(selectedMood, intensity)) {
                        showAngerAlertDialog();
                    } else {
                        // Normal flow: go to history
                        Intent i = new Intent(MoodJournalActivity.this, MoodHistoryActivity.class);
                        i.putExtra("userId", userId);
                        startActivity(i);
                        finish();
                    }
                });
            }).start();
        });
    }

    private void setMood(String mood, TextView tvSelectedMood) {
        selectedMood = mood;
        tvSelectedMood.setText("Selected: " + mood);
    }

    // ✅ Rule A + cooldown
    private boolean shouldTriggerAngerAlert(String mood, int intensity) {
        if (!"Angry".equalsIgnoreCase(mood)) return false;
        if (intensity < 4) return false;

        SharedPreferences prefs = getSharedPreferences(ALERT_PREFS, MODE_PRIVATE);
        long last = prefs.getLong(KEY_LAST_ALERT_TIME, 0L);
        long now = System.currentTimeMillis();

        // cooldown check
        return (now - last) >= ALERT_COOLDOWN_MS;
    }

    private void showAngerAlertDialog() {
        // save last shown time immediately to enforce cooldown
        SharedPreferences prefs = getSharedPreferences(ALERT_PREFS, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_ALERT_TIME, System.currentTimeMillis()).apply();

        new AlertDialog.Builder(this)
                .setTitle("Feeling overwhelmed?")
                .setMessage("We noticed a high anger level. Would you like to try a quick calming exercise?")
                .setCancelable(false)
                .setPositiveButton("Calm Now", (dialog, which) -> {
                    // ✅ Open calming screen (change activity name if yours differs)
                    Intent i = new Intent(MoodJournalActivity.this, CalmNowActivity.class);
                    i.putExtra("userId", userId);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Dismiss", (dialog, which) -> {
                    // Go to history after dismiss
                    Intent i = new Intent(MoodJournalActivity.this, MoodHistoryActivity.class);
                    i.putExtra("userId", userId);
                    startActivity(i);
                    finish();
                })
                .show();
    }
}
