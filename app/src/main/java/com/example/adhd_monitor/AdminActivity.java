package com.example.adhd_monitor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class AdminActivity extends AppCompatActivity {

    private Button btnManageUsers, btnManagePsychologists, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManagePsychologists = findViewById(R.id.btnManagePsychologists);
        btnLogout = findViewById(R.id.btnLogout);

        btnManageUsers.setOnClickListener(view ->
                Toast.makeText(AdminActivity.this, "User Management Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        btnManagePsychologists.setOnClickListener(view ->
                Toast.makeText(AdminActivity.this, "Psychologist Management Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(view -> {
            startActivity(new Intent(AdminActivity.this, MainActivity.class));
            finish();
        });
    }
}
