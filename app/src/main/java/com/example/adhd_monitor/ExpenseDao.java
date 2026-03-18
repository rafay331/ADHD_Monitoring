package com.example.adhd_monitor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(ExpenseEntity expense);

    // ✅ Month sum (uses createdAt because your entity has createdAt)
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE userId = :userId AND createdAt >= :startMillis AND createdAt < :endMillis")
    int sumForMonth(long userId, long startMillis, long endMillis);

    // ✅ Month list (uses createdAt consistently)
    @Query("SELECT * FROM expenses " +
            "WHERE userId = :userId AND createdAt >= :start AND createdAt < :end " +
            "ORDER BY createdAt DESC")
    List<ExpenseEntity> getExpensesForMonth(long userId, long start, long end);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    List<ExpenseEntity> getRecentExpenses(long userId, int limit);

}
