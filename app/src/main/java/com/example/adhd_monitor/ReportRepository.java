package com.example.adhd_monitor;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

// Room DAOs and Entities
import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.FocusSessionDao;
import com.example.adhd_monitor.TaskDao;
import com.example.adhd_monitor.GoalDao;
import com.example.adhd_monitor.FocusSessionEntity;
import com.example.adhd_monitor.TaskEntity;
import com.example.adhd_monitor.GoalEntity;
import com.example.adhd_monitor.AppDatabase;
public class ReportRepository {
    private final FocusSessionDao sessionDao;
    private final TaskDao taskDao;
    private final GoalDao goalDao;

    public ReportRepository(AppDatabase db) {
        this.sessionDao = db.focusSessionDao();
        this.taskDao = db.taskDao();
        this.goalDao = db.goalDao();
    }

    public static class Range {
        public final long start, end;
        public Range(long s, long e){ start=s; end=e; }
    }

    public static Range rangeFor(String period) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);

        if ("daily".equals(period)) {
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
            long start = cal.getTimeInMillis();
            return new Range(start, start + 24L*60*60*1000);
        } else if ("weekly".equals(period)) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY,0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
            long start = cal.getTimeInMillis();
            return new Range(start, start + 7L*24*60*60*1000);
        } else { // monthly
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
            long start = cal.getTimeInMillis();
            cal.add(Calendar.MONTH,1);
            return new Range(start, cal.getTimeInMillis());
        }
    }

    public int totalFocusMins(long userId, String period){
        Range r = rangeFor(period);
        Integer v = sessionDao.getTotalFocusMinutes(userId, r.start, r.end);
        return v == null ? 0 : v;
    }

    public int totalDistractions(long userId, String period){
        Range r = rangeFor(period);
        Integer v = sessionDao.getTotalDistractions(userId, r.start, r.end);
        return v == null ? 0 : v;
    }

    public int completedTasks(long userId, String period){
        Range r = rangeFor(period);
        return taskDao.getCompletedCount(userId, r.start, r.end);
    }

    public List<FocusSessionEntity> sessionsForChart(long userId, String period){
        Range r = rangeFor(period);
        return sessionDao.getSessionsInRange(userId, r.start, r.end);
    }

    public List<GoalEntity> goals(long userId){
        return goalDao != null ? goalDao.getGoals(userId) : Collections.emptyList();
    }

    public List<TaskEntity> latestCompletions(long userId){
        return taskDao.latestCompletions(userId);
    }

    /** Distraction Score: lower is better. Normalize to 0..100. */
    public int distractionScore(long userId, String period){
        int mins = totalFocusMins(userId, period);
        int d = totalDistractions(userId, period);
        if (mins <= 0) return 100; // worst (no focus time)
        double perHour = (d * 60.0) / mins;         // distractions per hour
        double score = Math.min(100, Math.max(0, perHour * 20)); // 0..100 scale (tune factor 20 as needed)
        return (int)Math.round(score);
    }
}

