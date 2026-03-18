package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {

    // ---------- INSERT ----------
    @Insert
    void insert(FocusSessionEntity session);

    // ---------- QUERIES ----------

    // Sessions in range for charts (handles seconds or milliseconds)
    @Query(
            "SELECT * FROM focus_sessions " +
                    "WHERE userId = :userId " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) >= :start " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) < :end " +
                    "ORDER BY startTime ASC"
    )
    List<FocusSessionEntity> getSessionsInRange(long userId, long start, long end);

    // Total focus minutes in range (works with seconds or millis)
    @Query(
            "SELECT COALESCE(SUM(" +
                    "CASE " +
                    "WHEN endTime < 1000000000000 AND startTime < 1000000000000 THEN (endTime - startTime) / 60 " +      // seconds → minutes
                    "ELSE (endTime - startTime) / 60000 END" +                                                           // millis → minutes
                    "), 0) " +
                    "FROM focus_sessions " +
                    "WHERE userId = :userId " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) >= :start " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) < :end"
    )
    Integer getTotalFocusMinutes(long userId, long start, long end);

    // Total distractions in range
    @Query(
            "SELECT COALESCE(SUM(distractions), 0) " +
                    "FROM focus_sessions " +
                    "WHERE userId = :userId " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) >= :start " +
                    "AND (CASE WHEN startTime < 1000000000000 THEN startTime * 1000 ELSE startTime END) < :end"
    )
    Integer getTotalDistractions(long userId, long start, long end);

    // Optional: get latest start time (for debugging)
    @Query("SELECT MAX(startTime) FROM focus_sessions WHERE userId = :userId")
    Long maxStartForUser(long userId);
}
