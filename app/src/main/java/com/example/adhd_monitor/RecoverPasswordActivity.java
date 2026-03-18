package com.example.adhd_monitor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RecoverPasswordActivity extends BaseActivity {

    private EditText etUsernameOrEmail, etSecurityAnswer, etNewPassword;
    private TextView txtSecurityQuestion;
    private Button btnFetchQuestion, btnResetPassword;
    private AppDatabase db;

    private String correctAnswer = null;
    private Object currentUser = null; // can be User, Parent, or Psychologist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        etUsernameOrEmail = findViewById(R.id.etUsernameOrEmail);
        txtSecurityQuestion = findViewById(R.id.txtSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnFetchQuestion = findViewById(R.id.btnFetchQuestion);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        db = AppDatabase.getInstance(this);

        btnFetchQuestion.setOnClickListener(v -> fetchSecurityQuestion());

        btnResetPassword.setOnClickListener(v -> {
            String answer = etSecurityAnswer.getText().toString().trim().toLowerCase();
            String newPassword = etNewPassword.getText().toString().trim();

            if (correctAnswer == null || currentUser == null) {
                Toast.makeText(this, "Please fetch a user first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!answer.equals(correctAnswer)) {
                Toast.makeText(this, "Incorrect security answer", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isStrongPassword(newPassword)) {
                Toast.makeText(this, "Weak password. Use upper, lower, digit, and special char.", Toast.LENGTH_LONG).show();
                return;
            }

            String hashed = PasswordUtils.hashPassword(newPassword);

            new Thread(() -> {
                if (currentUser instanceof User) {
                    ((User) currentUser).setPassword(hashed);
                    db.userDao().updateUser((User) currentUser);
                } else if (currentUser instanceof Psychologist) {
                    ((Psychologist) currentUser).setPassword(hashed);
                    db.psychologistDao().updatePsychologist((Psychologist) currentUser);
                } else if (currentUser instanceof Parent) {
                    ((Parent) currentUser).setPassword(hashed);
                    db.parentDao().updateParent((Parent) currentUser);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    private void fetchSecurityQuestion() {
        String input = etUsernameOrEmail.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Enter username or email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDao().getUserByUsername(input);
            if (user == null) user = db.userDao().getUserByEmail(input);

            if (user != null) {
                currentUser = user;
                correctAnswer = user.securityAnswer.toLowerCase();
                User finalUser = user;
                runOnUiThread(() -> txtSecurityQuestion.setText(finalUser.securityQuestion));
                return;
            }

            Psychologist psy = db.psychologistDao().getPsychologistByUsername(input);
            if (psy == null) psy = db.psychologistDao().getPsychologistByEmail(input);

            if (psy != null) {
                currentUser = psy;
                correctAnswer = psy.getSecurityAnswer().toLowerCase();
                Psychologist finalPsy = psy;
                runOnUiThread(() -> txtSecurityQuestion.setText(finalPsy.getSecurityQuestion()));
                return;
            }

            Parent parent = db.parentDao().getParentByUsername(input);
            if (parent == null) parent = db.parentDao().getParentByEmail(input);

            if (parent != null) {
                currentUser = parent;
                correctAnswer = parent.securityAnswer.toLowerCase();
                Parent finalParent = parent;
                runOnUiThread(() -> txtSecurityQuestion.setText(finalParent.securityQuestion));
                return;
            }

            runOnUiThread(() -> {
                currentUser = null;
                correctAnswer = null;
                txtSecurityQuestion.setText("No user found");
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[@#$%^&+=!].*");
    }
}
