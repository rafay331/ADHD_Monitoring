package com.example.adhd_monitor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CalmNowActivity extends AppCompatActivity implements ExerciseAdapter.OnExerciseClickListener {

    private TextView tvTimer, tvStep;
    private Button btnStart, btnStop, btnBackToHistory;
    private RecyclerView rvExercises;

    // ✅ Sound UI
    private Switch switchSound;
    private Spinner spinnerSound;
    private SeekBar seekVolume;

    private MediaPlayer mediaPlayer;
    private float volume = 0.35f; // default

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;

    private long totalMs = 60_000L;
    private long remainingMs = totalMs;

    private final String[] STEPS_BOX = new String[]{
            "Inhale (4s)", "Hold (4s)", "Exhale (4s)", "Hold (4s)"
    };

    private final String[] STEPS_478 = new String[]{
            "Inhale (4s)", "Hold (7s)", "Exhale (8s)"
    };

    private final String[] STEPS_GROUNDING = new String[]{
            "Name 5 things you can SEE",
            "Name 4 things you can FEEL",
            "Name 3 things you can HEAR",
            "Name 2 things you can SMELL",
            "Name 1 thing you can TASTE"
    };

    private final String[] STEPS_MUSCLE = new String[]{
            "Tense shoulders (5s)",
            "Relax shoulders (5s)",
            "Tense hands (5s)",
            "Relax hands (5s)",
            "Tense jaw (5s)",
            "Relax jaw (5s)",
            "Slow breathing (10s)"
    };

    private String selectedExerciseName = "Box Breathing";
    private String[] selectedSteps = STEPS_BOX;
    private int stepIntervalMs = 4000;
    private int stepIndex = 0;

    private final ExerciseItem[] exerciseItems = new ExerciseItem[]{
            new ExerciseItem("Box Breathing", "60s"),
            new ExerciseItem("4-7-8 Breathing", "≈57s"),
            new ExerciseItem("5-4-3-2-1 Grounding", "60s"),
            new ExerciseItem("Muscle Relaxation", "60s")
    };

    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calm_now);

        tvTimer = findViewById(R.id.tvTimer);
        tvStep = findViewById(R.id.tvStep);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnBackToHistory = findViewById(R.id.btnBackToHistory);

        rvExercises = findViewById(R.id.rvExercises);

        // ✅ Sound UI init
        switchSound = findViewById(R.id.switchSound);
        spinnerSound = findViewById(R.id.spinnerSound);
        seekVolume = findViewById(R.id.seekVolume);

        // userId for returning
        userId = getIntent().getIntExtra("userId", -1);

        // Setup spinner with sound names from strings.xml
        ArrayAdapter<CharSequence> soundAdapter = ArrayAdapter.createFromResource(
                this, R.array.nature_sounds, android.R.layout.simple_spinner_item
        );
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSound.setAdapter(soundAdapter);

        // Volume control
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = Math.max(0f, Math.min(1f, progress / 100f));
                applyVolume();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup slider
        rvExercises.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ExerciseAdapter adapter = new ExerciseAdapter(exerciseItems, this);
        rvExercises.setAdapter(adapter);

        updateTimerUI(remainingMs);
        tvStep.setText("Select an exercise");

        btnStart.setOnClickListener(v -> {
            if (selectedSteps == null || selectedSteps.length == 0) {
                Toast.makeText(this, "Please select an exercise.", Toast.LENGTH_SHORT).show();
                return;
            }
            startExercise();
        });

        btnStop.setOnClickListener(v -> stopExercise());

        btnBackToHistory.setOnClickListener(v -> {
            stopExercise(); // also stops sound
            Intent i = new Intent(CalmNowActivity.this, HomeActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
            finish();
        });
    }

    @Override
    public void onExerciseClicked(int position) {
        stopExercise();

        if (position == 0) {
            selectedExerciseName = "Box Breathing";
            selectedSteps = STEPS_BOX;
            stepIntervalMs = 4000;
            totalMs = 60_000L;
        } else if (position == 1) {
            selectedExerciseName = "4-7-8 Breathing";
            selectedSteps = STEPS_478;
            totalMs = 57_000L; // 3 cycles of 19 seconds
        } else if (position == 2) {
            selectedExerciseName = "5-4-3-2-1 Grounding";
            selectedSteps = STEPS_GROUNDING;
            stepIntervalMs = 12_000;
            totalMs = 60_000L;
        } else {
            selectedExerciseName = "Muscle Relaxation";
            selectedSteps = STEPS_MUSCLE;
            stepIntervalMs = 8000;
            totalMs = 60_000L;
        }

        remainingMs = totalMs;
        stepIndex = 0;
        updateTimerUI(remainingMs);
        tvStep.setText("Selected: " + selectedExerciseName);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    private void startExercise() {
        if (isRunning) return;

        isRunning = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        // ✅ Start nature sound if enabled
        if (switchSound.isChecked()) {
            startNatureSound();
        }

        stepIndex = 0;
        tvStep.setText(selectedSteps[stepIndex]);

        if ("4-7-8 Breathing".equals(selectedExerciseName)) {
            start478Timer();
            return;
        }

        countDownTimer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerUI(remainingMs);

                long elapsed = totalMs - remainingMs;
                int newStepIndex = (int) ((elapsed / stepIntervalMs) % selectedSteps.length);
                if (newStepIndex != stepIndex) {
                    stepIndex = newStepIndex;
                    tvStep.setText(selectedSteps[stepIndex]);
                }
            }

            @Override
            public void onFinish() {
                finishExercise();
            }
        }.start();
    }

    private void start478Timer() {
        final int[] phaseSeconds = new int[]{4, 7, 8};
        final String[] phaseText = new String[]{"Inhale (4s)", "Hold (7s)", "Exhale (8s)"};

        countDownTimer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerUI(remainingMs);

                long elapsedSec = (totalMs - remainingMs) / 1000;
                long cyclePos = elapsedSec % 19;

                if (cyclePos < phaseSeconds[0]) tvStep.setText(phaseText[0]);
                else if (cyclePos < phaseSeconds[0] + phaseSeconds[1]) tvStep.setText(phaseText[1]);
                else tvStep.setText(phaseText[2]);
            }

            @Override
            public void onFinish() {
                finishExercise();
            }
        }.start();
    }

    private void stopExercise() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        stopNatureSound();

        isRunning = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        remainingMs = totalMs;
        stepIndex = 0;
        updateTimerUI(remainingMs);

        if (selectedExerciseName != null) tvStep.setText("Selected: " + selectedExerciseName);
        else tvStep.setText("Select an exercise");
    }

    private void finishExercise() {
        stopNatureSound();

        remainingMs = 0;
        updateTimerUI(remainingMs);
        tvStep.setText("Well done. ✅");

        isRunning = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    // ✅ Nature sound engine
    private void startNatureSound() {
        stopNatureSound();

        int selected = spinnerSound.getSelectedItemPosition();
        int resId;

        if (selected == 0) resId = R.raw.birds;
        else if (selected == 1) resId = R.raw.rain;
//        else if (selected == 2) resId = R.raw.river;
        else resId = R.raw.river;

        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            applyVolume();
            mediaPlayer.start();
        }
    }

    private void applyVolume() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    private void stopNatureSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateTimerUI(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNatureSound();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
