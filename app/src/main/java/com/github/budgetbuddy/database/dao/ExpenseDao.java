package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Expense;

import java.util.List;
@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Query("SELECT * FROM expense WHERE id = :id")
    Expense getExpenseById(int id);

    @Query("SELECT * FROM expense WHERE entryDateStartInMilliSec = :date")
    List<Expense> getExpensesOfSpecificDate(long date);

    @Query("SELECT * FROM expense WHERE entryDateStartInMilliSec >= :startDate and entryDateStartInMilliSec <= :endDate")
    List<Expense> getExpensesInterval(long startDate, long endDate);

    @Query("SELECT * FROM expense WHERE categoryId = :categoryId")
    List<Expense> getExpensesByCategory(int categoryId);

    @Query("SELECT * FROM expense WHERE categoryId = :categoryId AND entryDateStartInMilliSec >= :startDate and entryDateStartInMilliSec <= :endDate")
    List<Expense> getExpensesByCategoryAndInterval(int categoryId, long startDate, long endDate);

    @Query("SELECT SUM(amountInCents) FROM expense WHERE entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate")
    Long getTotalSpending(long startDate, long endDate);

    @Query("SELECT categoryId, SUM(amountInCents) as total FROM expense WHERE entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate GROUP BY categoryId")
    List<CategorySpending> getSpendingByCategory(long startDate, long endDate);

    // Requires entryDateStartInMilliSec to be stored as start-of-day millis (use ExpenseRepository to insert/update).
    @Query("SELECT entryDateStartInMilliSec as date, SUM(amountInCents) as total FROM expense WHERE entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate GROUP BY entryDateStartInMilliSec")
    List<DailySpending> getDailySpending(long startDate, long endDate);

    @Query("UPDATE expense SET amountInCents = :amount, categoryId = :categoryId, entryDateStartInMilliSec = :entryDateStartInMilliSec, note = :note, repeat = :repeat WHERE id = :id")
    void updateExpense(int id, long amount, int categoryId, long entryDateStartInMilliSec, String note, String repeat);

    @Delete
    void deleteExpense(Expense expense);

    class CategorySpending {
        public int categoryId;
        public long totalInCents;
    }

    class DailySpending {
        public long date;
        public long totalInCents;
    }

    // NOTE: calls to COALESCE(_) in sql do not return null so Long is acceptable here
    @Query("SELECT COALESCE(SUM(amountInCents), 0) FROM expense WHERE entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate")
    long getTotalForInterval(long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amountInCents), 0) FROM expense WHERE categoryId = :categoryId AND entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate")
    long getTotalForCategoryAndInterval(int categoryId, long startDate, long endDate);

    @Query("SELECT * FROM expense WHERE entryDateStartInMilliSec >= :startDate AND entryDateStartInMilliSec <= :endDate ORDER BY entryDateStartInMilliSec DESC LIMIT :maxResults")
    List<Expense> getRecentExpenses(long startDate, long endDate, int maxResults);
}
