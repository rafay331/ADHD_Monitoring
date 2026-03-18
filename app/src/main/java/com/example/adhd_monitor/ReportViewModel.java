package com.example.adhd_monitor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ReportViewModel (split IDs)
 * - focusUserId is used for focus_sessions.userId
 * - taskChildId  is used for tasks.childId
 * Period (daily/weekly/monthly) only affects focus metrics.
 * Task metrics shown are all-time by chil
 */
public class ReportViewModel extends AndroidViewModel {

    private final FocusSessionDao focusDao;
    private final TaskDao taskDao;
    private final GoalDao goalDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // ---- Inputs ----
    private final MutableLiveData<Long> focusUserId = new MutableLiveData<>(1L);
    private final MutableLiveData<Long> taskChildId  = new MutableLiveData<>(2L);
    private final MutableLiveData<String> period     = new MutableLiveData<>("weekly"); // daily|weekly|monthly

    // ---- Outputs ----
    // Focus (period-bound)
    public final MediatorLiveData<Integer> focusMinutes = new MediatorLiveData<>();
    public final MediatorLiveData<Integer> distractionScore = new MediatorLiveData<>();
    public final MediatorLiveData<List<FocusSessionEntity>> sessions = new MediatorLiveData<>();

    // Tasks (all-time by child)
    public final MediatorLiveData<Integer> completed = new MediatorLiveData<>();
    public final MediatorLiveData<Integer> totalPoints = new MediatorLiveData<>();
    public final MediatorLiveData<List<TaskEntity>> recentCompletions = new MediatorLiveData<>();

    // Goals (by child)
    public final MediatorLiveData<List<GoalEntity>> goals = new MediatorLiveData<>();

    public ReportViewModel(@NonNull Application app) {
        super(app);
        AppDatabase db = AppDatabase.getInstance(app);
        focusDao = db.focusSessionDao();
        taskDao  = db.taskDao();
        goalDao  = db.goalDao();

        // Safe defaults
        focusMinutes.setValue(0);
        distractionScore.setValue(0);
        sessions.setValue(Collections.emptyList());
        completed.setValue(0);
        totalPoints.setValue(0);
        recentCompletions.setValue(Collections.emptyList());
        goals.setValue(Collections.emptyList());

        // Recompute whenever any input changes
        period.observeForever(p -> recompute());
        focusUserId.observeForever(id -> recompute());
        taskChildId.observeForever(id -> { recompute(); reloadStaticLists(); });
    }

    // ---- Public setters ----
    public void setPeriod(String p) {
        if (p != null) period.setValue(p.toLowerCase());
    }

    /** Set BOTH ids: focus sessions owner + task child id */
    public void setIds(long focusUid, long childId) {
        this.focusUserId.setValue(focusUid);
        this.taskChildId.setValue(childId);
    }

    // ---- Core loader ----
    private void recompute() {
        final Long fUid = focusUserId.getValue();
        final Long cId  = taskChildId.getValue();
        if (fUid == null || cId == null) return;

        final Range r = computeRange(period.getValue());

        io.execute(() -> {
            // Focus (period-based)
            Integer fm = focusDao.getTotalFocusMinutes(fUid, r.from, r.to);
            if (fm == null) fm = 0;

            Integer dis = focusDao.getTotalDistractions(fUid, r.from, r.to);
            if (dis == null) dis = 0;

            List<FocusSessionEntity> sess = focusDao.getSessionsInRange(fUid, r.from, r.to);
            if (sess == null) sess = new ArrayList<>();

            // Tasks (all-time for child)
            int comp = taskDao.getCompletedCount(cId);
            int pts  = taskDao.getTotalPoints(cId);
            List<TaskEntity> latest = taskDao.latestCompletions(cId);
            if (latest == null) latest = new ArrayList<>();

            // Publish
            focusMinutes.postValue(fm);
            int base = Math.min(100, (int) (fm * 1.5));   // reward focus time
            int penalty = dis * 15;                       // mild penalty per distraction

            int score = Math.max(0, base - penalty);
            distractionScore.postValue(score);

            sessions.postValue(sess);

            completed.postValue(comp);
            totalPoints.postValue(pts);
            recentCompletions.postValue(latest);
        });
    }

    private void reloadStaticLists() {
        final Long cId = taskChildId.getValue();
        if (cId == null) return;

        io.execute(() -> {
            List<GoalEntity> g = goalDao.getGoals(cId);
            if (g == null) g = new ArrayList<>();
            goals.postValue(g);
        });
    }

    // ---- Helpers ----
    private static class Range { long from, to; }
    private Range computeRange(String per) {
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;
        Range r = new Range();
        r.to = now;
        if ("daily".equalsIgnoreCase(per))        r.from = now - day;
        else if ("monthly".equalsIgnoreCase(per)) r.from = now - 30 * day;
        else                                      r.from = now - 7 * day; // weekly default
        return r;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdown();
    }
}
