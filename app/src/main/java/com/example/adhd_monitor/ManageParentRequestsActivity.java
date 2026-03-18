package com.example.adhd_monitor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;
import android.view.View;
import android.content.res.Resources;

import java.util.List;

public class ManageParentRequestsActivity extends BaseActivity {

    private LinearLayout requestContainer;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        requestContainer = findViewById(R.id.requestContainer);
        db = AppDatabase.getInstance(this);
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showPendingRequests();
    }

    private void showPendingRequests() {
        List<ParentChildLink> requests = db.parentChildLinkDao().getPendingRequestsForChild(userId);

        requestContainer.removeAllViews(); // Clear any old views

        if (requests.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No pending requests.");
            tv.setTextSize(16f);
            requestContainer.addView(tv);
            return;
        }

        for (ParentChildLink request : requests) {
            Parent parent = db.parentDao().getParentById(request.getParentId());

            if (parent == null) {
                TextView errorText = new TextView(this);
                errorText.setText("Request from unknown/deleted parent (ID: " + request.getParentId() + ")");
                errorText.setTextSize(16f);
                requestContainer.addView(errorText);
                continue;
            }

            // Container for each request
            LinearLayout requestLayout = new LinearLayout(this);
            requestLayout.setOrientation(LinearLayout.VERTICAL);
            requestLayout.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            requestLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Message Text
            TextView message = new TextView(this);
            message.setText("Do you want to accept request from " + parent.getUsername() + "?");
            message.setTextSize(16f);
            message.setPadding(0, 0, 0, dpToPx(6));
            requestLayout.addView(message);

            // Button Layout
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setPadding(0, 0, 0, dpToPx(10));

            // Accept Button
            Button btnAccept = new Button(this);
            btnAccept.setText("Accept");
            btnAccept.setOnClickListener(v -> {
                db.parentChildLinkDao().updateLinkStatus(request.getId(), "accepted");
                Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                recreate();
            });

            // Reject Button
            Button btnReject = new Button(this);
            btnReject.setText("Reject");
            btnReject.setOnClickListener(v -> {
                db.parentChildLinkDao().updateLinkStatus(request.getId(), "rejected");
                Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                recreate();
            });

            // Add buttons to layout
            buttonLayout.addView(btnAccept);
            buttonLayout.addView(btnReject);

            // Add both to outer layout
            requestLayout.addView(buttonLayout);
            requestContainer.addView(requestLayout);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
