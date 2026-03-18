package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnSignUp;
    private MaterialButton googleLoginBtn;
    private CheckBox checkBoxRememberMe;
    private TextView txtForgotPassword;
    private AppDatabase db;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    private static final String PREFS_NAME = "login_prefs";

    Parent parent = new Parent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            int userId = prefs.getInt("user_id", -1);
            Intent intent;
            switch (role) {
                case "user":
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("userId", userId);
                    break;
                case "psychologist":
                    intent = new Intent(MainActivity.this, PsychologistDashboardActivity.class);
                    intent.putExtra("userId", userId);
                    break;
                case "parent":
                    intent = new Intent(MainActivity.this, ParentDashboardActivity.class);
                    intent.putExtra("userId", parent.id);
                    break;
                default:
                    intent = new Intent(MainActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        db = AppDatabase.getInstance(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLoginBtn.setOnClickListener(view -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        btnLogin.setOnClickListener(view -> handleLogin());

        btnSignUp.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        txtForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, RecoverPasswordActivity.class));
        });

        if (prefs.getBoolean("remember_me", false)) {
            edtUsername.setText(prefs.getString("username", ""));
            checkBoxRememberMe.setChecked(true);
        }
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals("admin") && password.equals("admin123")) {
            saveSession(username, "admin", -1);
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
            finish();
            return;
        }

        new Thread(() -> {
            User user = db.userDao().getUserByUsername(username);
            Psychologist psychologist = db.psychologistDao().getPsychologistByUsername(username);
            Parent parent = db.parentDao().getParentByUsername(username);

            if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
                int userId = user.getId();
                saveSession(user.username, "user", userId);
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                });
                return;
            }

            if (psychologist != null && PasswordUtils.verifyPassword(password, psychologist.getPassword())) {
                int userId = psychologist.getId();
                saveSession(psychologist.getUsername(), "psychologist", userId);
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, PsychologistDashboardActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                });
                return;
            }

            if (parent != null && PasswordUtils.verifyPassword(password, parent.password)) {
                int userId = parent.getId();
                saveSession(parent.username, "parent", userId);
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, ParentDashboardActivity.class);
                    intent.putExtra("userId", String.valueOf(parent.getId()));  // Correctly pass it here
                    startActivity(intent);

                    finish();
                });
                return;
            }

            runOnUiThread(() -> Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void saveSession(String username, String role, int userId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        if (checkBoxRememberMe.isChecked()) {
            editor.putBoolean("is_logged_in", true);
            editor.putString("username", username);
            editor.putString("role", role);
            editor.putInt("user_id", userId);
            editor.putBoolean("remember_me", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    handleGoogleLogin(account.getEmail());
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleLogin(String email) {
        new Thread(() -> {
            User user = db.userDao().getUserByEmail(email);
            if (user != null) {
                saveSession(user.username, "user", user.getId());
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("userId", user.getId());
                    startActivity(intent);
                });
                return;
            }

            Psychologist psy = db.psychologistDao().getPsychologistByEmail(email);
            if (psy != null) {
                saveSession(psy.getUsername(), "psychologist", psy.getId());
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, PsychologistDashboardActivity.class);
                    intent.putExtra("userId", psy.getId());
                    startActivity(intent);
                });
                return;
            }

            Parent parent = db.parentDao().getParentByEmail(email);
            if (parent != null) {
                saveSession(parent.username, "parent", parent.getId());
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, ParentDashboardActivity.class);
                    intent.putExtra("userId", parent.getId());
                    startActivity(intent);
                });
                return;
            }

            showRoleSelectionDialog(email);
        }).start();
    }

    private void showRoleSelectionDialog(String email) {
        runOnUiThread(() -> {
            String[] roles = {"User", "Psychologist", "Parent"};
            new AlertDialog.Builder(this)
                    .setTitle("Choose Role")
                    .setItems(roles, (dialog, which) -> {
                        new Thread(() -> {
                            switch (which) {
                                case 0:
                                    User user = new User(email, email, "");
                                    db.userDao().insertUser(user);
                                    User insertedUser = db.userDao().getUserByEmail(email);
                                    saveSession(insertedUser.username, "user", insertedUser.getId());
                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        intent.putExtra("userId", insertedUser.getId());
                                        startActivity(intent);
                                    });
                                    break;
                                case 1:
                                    Psychologist psy = new Psychologist(email, email, "");
                                    db.psychologistDao().insertPsychologist(psy);
                                    Psychologist insertedPsy = db.psychologistDao().getPsychologistByEmail(email);
                                    saveSession(insertedPsy.getUsername(), "psychologist", insertedPsy.getId());
                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(MainActivity.this, PsychologistDashboardActivity.class);
                                        intent.putExtra("userId", insertedPsy.getId());
                                        startActivity(intent);
                                    });
                                    break;
                                case 2:
                                    Parent parent = new Parent(email, email, "");
                                    db.parentDao().insertParent(parent);
                                    Parent insertedParent = db.parentDao().getParentByEmail(email);
                                    saveSession(insertedParent.username, "parent", insertedParent.getId());
                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(MainActivity.this, ParentDashboardActivity.class);
                                        intent.putExtra("userId", insertedParent.getId());
                                        startActivity(intent);
                                    });
                                    break;
                            }
                        }).start();
                    })
                    .setCancelable(false)
                    .show();
        });
    }
}
