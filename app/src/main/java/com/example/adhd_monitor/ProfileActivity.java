package com.example.adhd_monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private EditText edtCurrentUsername, edtCurrentPassword, edtNewUsername, edtNewPassword;
    private Button btnUpdate;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtCurrentUsername = findViewById(R.id.edtCurrentUsername);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewUsername = findViewById(R.id.edtNewUsername);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnUpdate = findViewById(R.id.btnUpdate);

        db = AppDatabase.getInstance(this);

        btnUpdate.setOnClickListener(v -> handleUpdate());
    }

    private void handleUpdate() {
        String currentUsername = edtCurrentUsername.getText().toString().trim();
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newUsername = edtNewUsername.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();

        if (currentUsername.isEmpty() || currentPassword.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Check User
            User user = db.userDao().getUserByUsername(currentUsername);
            if (user != null && PasswordUtils.verifyPassword(currentPassword, user.password)) {
                db.userDao().updateUser(user.id, newUsername, PasswordUtils.hashPassword(newPassword));
                runOnUiThread(() -> {
                    Toast.makeText(this, "User credentials updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Check Parent
            Parent parent = db.parentDao().getParentByUsername(currentUsername);
            if (parent != null && PasswordUtils.verifyPassword(currentPassword, parent.password)) {
                db.parentDao().updateParent(parent.id, newUsername, PasswordUtils.hashPassword(newPassword));
                runOnUiThread(() -> {
                    Toast.makeText(this, "Parent credentials updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Check Psychologist
            Psychologist psychologist = db.psychologistDao().getPsychologistByUsername(currentUsername);
            if (psychologist != null && PasswordUtils.verifyPassword(currentPassword, psychologist.getPassword())) {
                db.psychologistDao().updatePsychologist(psychologist.getId(), newUsername, PasswordUtils.hashPassword(newPassword));
                runOnUiThread(() -> {
                    Toast.makeText(this, "Psychologist credentials updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show());
        }).start();
    }
}

