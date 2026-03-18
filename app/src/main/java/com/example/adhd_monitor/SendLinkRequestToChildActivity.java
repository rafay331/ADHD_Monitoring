package com.example.adhd_monitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

import android.view.View;





import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.ParentChildLink;
import com.example.adhd_monitor.ParentChildLinkDao;
import com.example.adhd_monitor.User;
import com.example.adhd_monitor.UserDAO;

public class SendLinkRequestToChildActivity extends BaseActivity {

    private EditText etChildUsername;
    private Button btnSendRequest;
    private int parentId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_link_request_child);

        // Initialize DB
        db = AppDatabase.getInstance(this);

        // Retrieve parent ID from Intent
        String receivedId = getIntent().getStringExtra("userId");
        if (receivedId != null) {
            try {
                parentId = Integer.parseInt(receivedId);
            } catch (NumberFormatException e) {
                parentId = -1;
            }
        } else {
            parentId = -1;
        }

        if (parentId == -1) {
            Toast.makeText(this, "Parent ID is missing. Returning to login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        etChildUsername = findViewById(R.id.etChildUsername);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String childUsername = etChildUsername.getText().toString().trim();

                if (childUsername.isEmpty()) {
                    Toast.makeText(SendLinkRequestToChildActivity.this, "Please enter the child's username.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Run DB operations in background thread
                new Thread(() -> {
                    UserDAO userDao = db.userDao();
                    ParentChildLinkDao linkDao = db.parentChildLinkDao();

                    User child = userDao.getUserByUsername(childUsername);
                    if (child != null) {
                        // Check if request already exists
                        ParentChildLink existing = linkDao.getPendingRequestBetween(parentId, child.getId());
                        if (existing != null) {
                            runOnUiThread(() -> Toast.makeText(SendLinkRequestToChildActivity.this, "Request already sent.", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        ParentChildLink link = new ParentChildLink(parentId, child.getId(), "pending");
                        linkDao.insert(link);

                        runOnUiThread(() -> {
                            Toast.makeText(SendLinkRequestToChildActivity.this, "Request sent to " + childUsername, Toast.LENGTH_SHORT).show();
                            Intent backIntent = new Intent(SendLinkRequestToChildActivity.this, ParentDashboardActivity.class);
                            backIntent.putExtra("userId", String.valueOf(parentId));
                            startActivity(backIntent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(SendLinkRequestToChildActivity.this, "Child not found.", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });
    }
}


