package com.example.adhd_monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class CommunityChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    private CommunityChatAdapter adapter;
    private AppDatabase db;

    // Logged-in user
    private long currentUserId;

    // We will show anonymous alias instead of real username
    private String anonymousAlias;
    private EditText etBuddyUsername;
    private ImageButton btnSaveBuddy;
    private android.widget.Button btnCheckIn;

    private String buddyUsername = "";

    private int currentBudgetRating = 3; // computed live
    private int currentParticipationPoints = 120; // simple points logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        db = AppDatabase.getInstance(this);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        etBuddyUsername = findViewById(R.id.etBuddyUsername);
        btnSaveBuddy = findViewById(R.id.btnSaveBuddy);
        btnCheckIn = findViewById(R.id.btnCheckIn);

        // load saved buddy
        buddyUsername = loadBuddyUsername();
        etBuddyUsername.setText(buddyUsername);

        // save buddy click
        btnSaveBuddy.setOnClickListener(v -> {
            String buddy = etBuddyUsername.getText().toString().trim();
            if (TextUtils.isEmpty(buddy)) {
                Toast.makeText(this, "Enter buddy username", Toast.LENGTH_SHORT).show();
                return;
            }
            buddyUsername = buddy;
            saveBuddyUsername(buddyUsername);
            Toast.makeText(this, "Buddy saved: " + buddyUsername, Toast.LENGTH_SHORT).show();
        });

        // check-in click
        btnCheckIn.setOnClickListener(v -> sendBuddyCheckIn());

        adapter = new CommunityChatAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        adapter.setOnEncourageClickListener(this::encourageMessage);

        // ✅ Load userId (Intent -> SharedPreferences fallback)
        currentUserId = getUserIdFromIntentOrPrefs();
        if (currentUserId == -1) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // ✅ Create/reuse anonymous alias (NOW UNIQUE PER USER)
        anonymousAlias = getAnonymousAlias(currentUserId);

        loadLastPoints();   // ✅ points now persist across app restarts
        loadMessages();

        // ✅ refresh stars when screen opens
        currentBudgetRating = computeBudgetStarsLive(currentUserId);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private long getUserIdFromIntentOrPrefs() {

        // Read INT extra first (because HomeActivity sends int)
        int idFromIntentInt = getIntent().getIntExtra("userId", -1);
        if (idFromIntentInt != -1) return idFromIntentInt;

        // Optional: if somewhere else you sent a long, support it too
        long idFromIntentLong = getIntent().getLongExtra("userId", -1L);
        if (idFromIntentLong != -1L) return idFromIntentLong;

        // SharedPreferences fallback (your app uses login_prefs, user_id)
        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        return loginPrefs.getInt("user_id", -1);
    }

    // ✅ Anonymous alias generator (UNIQUE PER USER ID)
    private String getAnonymousAlias(long userId) {
        SharedPreferences prefs = getSharedPreferences("anon_prefs", MODE_PRIVATE);

        // unique key per user
        String key = "anon_alias_user_" + userId;

        String alias = prefs.getString(key, null);

        if (alias == null) {
            int rand = 1000 + new Random().nextInt(9000);
            alias = "Anonymous#" + rand;
            prefs.edit().putString(key, alias).apply();
        }
        return alias;
    }

    private void loadMessages() {
        List<CommunityMessageEntity> all = db.communityMessageDao().getAllMessages();
        adapter.setData(all);
        scrollToBottom();
    }

    private void loadLastPoints() {
        CommunityMessageEntity last = db.communityMessageDao().getLastMessageForUser((int) currentUserId);
        if (last != null) {
            currentParticipationPoints = last.participationPoints;
        }
    }

    private void encourageMessage(CommunityMessageEntity targetMsg) {
        final int BONUS = 2;

        db.communityMessageDao().incrementEncouragement(targetMsg.id);

        CommunityMessageEntity last = db.communityMessageDao().getLastMessageForUser(targetMsg.userId);
        if (last != null) {
            db.communityMessageDao().addBonusPointsToMessage(last.id, BONUS);
        }

        // ✅ refresh local points value for current user too
        loadLastPoints();

        loadMessages();
    }

    private void saveBuddyUsername(String buddy) {
        SharedPreferences prefs = getSharedPreferences("buddy_prefs", MODE_PRIVATE);
        prefs.edit().putString("buddy_username", buddy).apply();
    }

    private String loadBuddyUsername() {
        SharedPreferences prefs = getSharedPreferences("buddy_prefs", MODE_PRIVATE);
        return prefs.getString("buddy_username", "");
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // ✅ compute stars at send time (latest budget behavior)
        currentBudgetRating = computeBudgetStarsLive(currentUserId);

        // points logic
        currentParticipationPoints += 5;

        // ✅ Always store/display anonymous alias
        CommunityMessageEntity msg = new CommunityMessageEntity(
                (int) currentUserId,     // entity uses int
                anonymousAlias,
                text,
                currentBudgetRating,
                currentParticipationPoints,
                System.currentTimeMillis()
        );

        db.communityMessageDao().insert(msg);

        adapter.addMessage(msg);
        etMessage.setText("");
        scrollToBottom();
    }

    private void sendBuddyCheckIn() {

        if (TextUtils.isEmpty(buddyUsername)) {
            Toast.makeText(this, "Set buddy username first", Toast.LENGTH_SHORT).show();
            return;
        }

        // latest stars at send time
        currentBudgetRating = computeBudgetStarsLive(currentUserId);

        // points logic
        currentParticipationPoints += 3;

        String msgText = "@" + buddyUsername + " ✅ Daily Check-in: DONE | Points: "
                + currentParticipationPoints + " | Stars: " + currentBudgetRating;

        CommunityMessageEntity msg = new CommunityMessageEntity(
                (int) currentUserId,
                anonymousAlias,
                msgText,
                currentBudgetRating,
                currentParticipationPoints,
                System.currentTimeMillis()
        );

        db.communityMessageDao().insert(msg);

        adapter.addMessage(msg);
        scrollToBottom();

        Toast.makeText(this, "Check-in sent to @" + buddyUsername, Toast.LENGTH_SHORT).show();
    }

    // ✅ REAL-TIME stars from expenses + monthly limit
    private int computeBudgetStarsLive(long userId) {

        long[] range = getCurrentMonthRangeMillis();
        long start = range[0];
        long end = range[1];

        int spent = db.expenseDao().sumForMonth(userId, start, end);

        double limit = 0;
        MonthlyBudgetLimitEntity limitEntity =
                db.monthlyBudgetLimitDao().getLimit((int) userId, getMonthKey());

        if (limitEntity != null) {
            limit = limitEntity.limitAmount;
        }

        if (limit <= 0) return 3; // neutral if no limit set

        double ratio = spent / limit;

        if (ratio <= 0.50) return 5;
        if (ratio <= 0.70) return 4;
        if (ratio <= 0.90) return 3;
        if (ratio <= 1.00) return 2;
        return 1;
    }

    private long[] getCurrentMonthRangeMillis() {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    private String getMonthKey() {
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        return String.format("%04d-%02d", y, m);
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        }
    }
}
