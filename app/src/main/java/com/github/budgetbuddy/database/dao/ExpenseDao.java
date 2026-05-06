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

    @Query("SELECT * FROM expense WHERE entryDate = :date")
    List<Expense> getExpensesOfSpecificDate(long date);

    @Query("SELECT * FROM expense WHERE entryDate >= :startDate and entryDate <= :endDate")
    List<Expense> getExpensesInterval(long startDate, long endDate);

    @Query("SELECT * FROM expense WHERE categoryId = :categoryId")
    List<Expense> getExpensesByCategory(int categoryId);

    @Query("SELECT * FROM expense WHERE categoryId = :categoryId AND entryDate >= :startDate and entryDate <= :endDate")
    List<Expense> getExpensesByCategoryAndInterval(int categoryId, long startDate, long endDate);

    @Query("UPDATE expense SET amount = :amount, categoryId = :categoryId, entryDate = :entryDate, note = :note, repeat = :repeat WHERE id = :id")
    void updateExpense(int id, double amount, int categoryId, long entryDate, String note, String repeat);

    @Delete
    void deleteExpense(Expense expense);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense WHERE entryDate >= :startDate AND entryDate <= :endDate")
    double getTotalForInterval(long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense WHERE categoryId = :categoryId AND entryDate >= :startDate AND entryDate <= :endDate")
    double getTotalForCategoryAndInterval(int categoryId, long startDate, long endDate);

    @Query("SELECT * FROM expense WHERE entryDate >= :startDate AND entryDate <= :endDate ORDER BY entryDate DESC LIMIT :maxResults")
    List<Expense> getRecentExpenses(long startDate, long endDate, int maxResults);
}
