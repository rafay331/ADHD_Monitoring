package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {

    // ---------------- INSERTS ----------------
    @Insert
    long insertTask(TaskEntity task); // returns new taskId

    @Insert
    void insertSteps(List<TaskStepEntity> steps);

    // ---------------- QUERIES (Parent view) ----------------
    @Query("SELECT * FROM tasks WHERE parentId = :parentId ORDER BY dueAtMillis ASC")
    List<TaskEntity> getTasksAssignedByParent(long parentId);

    @Query("SELECT * FROM tasks WHERE parentId = :parentId AND childId = :childId ORDER BY dueAtMillis ASC")
    List<TaskEntity> getTasksForParentAndChild(long parentId, long childId);

    // ---------------- QUERIES (Child view) ----------------
    @Query("SELECT * FROM tasks WHERE childId = :childId ORDER BY dueAtMillis ASC")
    List<TaskEntity> getTasksForChild(long childId);

    // ---------------- TASK COMPLETION / STATUS ----------------
    @Query("UPDATE tasks SET done = :done, completedAtMillis = :completedAtMillis, pointsEarned = :pointsEarned WHERE taskId = :taskId")
    void markTaskDone(long taskId, boolean done, long completedAtMillis, int pointsEarned);

    // ---------------- CLEAR COMPLETED ----------------
    @Query("DELETE FROM tasks WHERE parentId = :parentId AND done = 1")
    void deleteCompletedTasksForParent(long parentId);

    // ---------------- STEPS ----------------
    @Query("SELECT * FROM task_steps WHERE taskOwnerId = :taskId ORDER BY stepId ASC")
    List<TaskStepEntity> getStepsForTask(long taskId);

    @Query("UPDATE task_steps SET isDone = :isDone WHERE stepId = :stepId")
    void setStepDone(long stepId, boolean isDone);

    // ---------------- SUMMARY METRICS (ALL-TIME / POINT-IN-TIME) ----------------
    @Query("SELECT COUNT(*) FROM tasks WHERE childId = :childId AND done = 1")
    int getCompletedCount(long childId);

    @Query("SELECT COUNT(*) FROM tasks WHERE childId = :childId AND done = 0 AND dueAtMillis >= :nowMillis")
    int getPendingCount(long childId, long nowMillis);

    @Query("SELECT COUNT(*) FROM tasks WHERE childId = :childId AND done = 0 AND dueAtMillis < :nowMillis")
    int getOverdueCount(long childId, long nowMillis);

    @Query("SELECT COALESCE(SUM(pointsEarned), 0) FROM tasks WHERE childId = :childId")
    int getTotalPoints(long childId);

    @Query("SELECT COUNT(*) FROM task_steps WHERE taskOwnerId = :taskId")
    int countStepsForTask(long taskId);

    @Query("SELECT COUNT(*) FROM task_steps WHERE taskOwnerId = :taskId AND isDone = 1")
    int countDoneStepsForTask(long taskId);

    // ---------------- REPORT & PROGRESS TRACKER (TIME-RANGED) ----------------
    // NOTE: completedAtMillis may be in seconds (10-digit) or millis (13-digit). Normalize inline.
    @Query(
            "SELECT COUNT(*) FROM tasks " +
                    "WHERE childId = :childId AND done = 1 " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) >= :startMillis " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) <  :endMillis"
    )
    int getCompletedCount(long childId, long startMillis, long endMillis);

    @Query(
            "SELECT * FROM tasks " +
                    "WHERE childId = :childId AND done = 1 " +
                    "ORDER BY (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) DESC " +
                    "LIMIT 10"
    )
    List<TaskEntity> latestCompletions(long childId);

    @Query(
            "SELECT * FROM tasks " +
                    "WHERE childId = :childId AND done = 1 " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) >= :startMillis " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) <  :endMillis " +
                    "ORDER BY (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) DESC " +
                    "LIMIT :limit"
    )
    List<TaskEntity> latestCompletionsInRange(long childId, long startMillis, long endMillis, int limit);

    @Query(
            "SELECT COALESCE(SUM(pointsEarned), 0) FROM tasks " +
                    "WHERE childId = :childId AND done = 1 " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) >= :startMillis " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) <  :endMillis"
    )
    int getPointsEarnedInRange(long childId, long startMillis, long endMillis);

    @Query(
            "SELECT * FROM tasks " +
                    "WHERE childId = :childId AND done = 1 " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) >= :startMillis " +
                    "AND (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) <  :endMillis " +
                    "ORDER BY (CASE WHEN completedAtMillis < 1000000000000 THEN completedAtMillis * 1000 ELSE completedAtMillis END) DESC"
    )
    List<TaskEntity> getCompletedTasksInRange(long childId, long startMillis, long endMillis);

    // ---------------- SIMPLE (NO TIMESTAMPS) COMPLETED TASK APIs ----------------
    // Total completed tasks for a child, ignoring dates
    @Query("SELECT COUNT(*) FROM tasks WHERE childId = :childId AND done = 1")
    int getCompletedTasksSimple(long childId);

    // All completed tasks (no date filter), newest first
    @Query("SELECT * FROM tasks WHERE childId = :childId AND done = 1 ORDER BY completedAtMillis DESC")
    List<TaskEntity> getAllCompletedTasksSimple(long childId);

    // ---------------- ONE-TIME NORMALIZERS (optional) ----------------
    @Query("UPDATE tasks SET completedAtMillis = completedAtMillis * 1000 " +
            "WHERE completedAtMillis > 0 AND completedAtMillis < 1000000000000")
    void normalizeCompletedAtToMillis();

    @Query("UPDATE tasks SET dueAtMillis = dueAtMillis * 1000 " +
            "WHERE dueAtMillis > 0 AND dueAtMillis < 1000000000000")
    void normalizeDueAtToMillis();

    // ---------------- DEBUG HELPERS (optional) ----------------
    @Query("SELECT COUNT(*) FROM tasks WHERE childId = :childId AND done = 1")
    int debugCountAllDone(long childId);

    @Query("SELECT MAX(completedAtMillis) FROM tasks WHERE childId = :childId AND done = 1")
    Long debugMaxCompletedAt(long childId);
}
