package com.example.adhd_monitor;

import static com.example.adhd_monitor.PasswordUtils.hashPassword;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.regex.Pattern;

public class SignUpActivity extends BaseActivity {

    private EditText etFirstName, etLastName, etUsername, etEmail, etPassword;
    private EditText etSecurityQuestion, etSecurityAnswer;
    private RadioButton radioUser, radioPsychologist, radioParent;
    private Button btnSignUp;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize input fields
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etSecurityQuestion = findViewById(R.id.etSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);

        // Initialize role selection and button
        radioUser = findViewById(R.id.radioUser);
        radioPsychologist = findViewById(R.id.radioPsychologist);
        radioParent = findViewById(R.id.radioParent);
        btnSignUp = findViewById(R.id.btnSignUp);

        db = AppDatabase.getInstance(this);

        btnSignUp.setOnClickListener(view -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String question = etSecurityQuestion.getText().toString().trim();
            String answer = etSecurityAnswer.getText().toString().trim().toLowerCase();

            // Validate empty fields
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate name pattern
            if (!isNameValid(firstName) || !isNameValid(lastName)) {
                Toast.makeText(SignUpActivity.this, "Name must contain only letters (A-Z or a-z)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password
            if (!isStrongPassword(password)) {
                Toast.makeText(SignUpActivity.this, "Password must be 8+ chars, include upper, lower, digit, and symbol", Toast.LENGTH_LONG).show();
                return;
            }

            new Thread(() -> {
                boolean usernameExists = db.userDao().getUserByUsername(username) != null ||
                        db.psychologistDao().getPsychologistByUsername(username) != null ||
                        db.parentDao().getParentByUsername(username) != null;

                if (usernameExists) {
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Username already taken!", Toast.LENGTH_SHORT).show());
                    return;
                }

                boolean emailExists = db.userDao().getUserByEmail(email) != null ||
                        db.psychologistDao().getPsychologistByEmail(email) != null ||
                        db.parentDao().getParentByEmail(email) != null;

                if (emailExists) {
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Email already used!", Toast.LENGTH_SHORT).show());
                    return;
                }

                String hashedPassword = hashPassword(password);

                if (radioUser.isChecked()) {
                    User user = new User(username, email, hashedPassword);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setSecurityQuestion(question);
                    user.setSecurityAnswer(answer);
                    db.userDao().insertUser(user);
                } else if (radioPsychologist.isChecked()) {
                    Psychologist psy = new Psychologist(username, email, hashedPassword);
                    psy.setFirstName(firstName);
                    psy.setLastName(lastName);
                    psy.setSecurityQuestion(question);
                    psy.setSecurityAnswer(answer);
                    db.psychologistDao().insertPsychologist(psy);
                } else if (radioParent.isChecked()) {
                    Parent parent = new Parent(username, email, hashedPassword);
                    parent.setFirstName(firstName);
                    parent.setLastName(lastName);
                    parent.setSecurityQuestion(question);
                    parent.setSecurityAnswer(answer);
                    db.parentDao().insertParent(parent);
                }

                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    private boolean isStrongPassword(String password) {
        Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean isNameValid(String name) {
        return name.matches("^[a-zA-Z]+$");
    }
}
