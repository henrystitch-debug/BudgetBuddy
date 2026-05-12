package com.github.budgetbuddy.database.repository;

import android.app.Application;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

/**
 * Repository class for managing Expense data.
 * Normalizes {@link Expense#entryDateStartInMilliSec} to the start of the day (midnight millis) on every
 * insert and update, so that {@link ExpenseDao#getDailySpending} can group rows correctly
 * using a plain {@code GROUP BY entryDate}.
 */
public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(ExpenseDao dao) {
        expenseDao = dao;
    }

    /**
     * Inserts a new expense. {@code expense.entryDate} is normalized to start-of-day millis
     * (midnight, local time) before persisting. This modifies the passed object in place.
     */
    public void insert(Expense expense) {
        expense.entryDateStartInMilliSec = TimeUtils.toStartOfDay(expense.entryDateStartInMilliSec);
        executor.execute(()->expenseDao.insert(expense));
    }

    /**
     * Updates an existing expense. {@code entryDate} is normalized to start-of-day millis.
     */
    public void updateExpense(int id, long amountInCents, int categoryId, long entryDate,
                              String note, String repeat) {
        expenseDao.updateExpense(id, amountInCents, categoryId, TimeUtils.toStartOfDay(entryDate), note, repeat);
    }

    public Expense getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }

    public List<Expense> getExpensesOfSpecificDate(long date) {
        return expenseDao.getExpensesOfSpecificDate(TimeUtils.toStartOfDay(date));
    }

    public List<Expense> getExpensesInterval(long startDate, long endDate) {
        return expenseDao.getExpensesInterval(startDate, endDate);
    }

    public List<Expense> getExpensesByCategory(int categoryId) {
        return expenseDao.getExpensesByCategory(categoryId);
    }

    public List<Expense> getExpensesByCategoryAndInterval(int categoryId,
                                                           long startDate, long endDate) {
        return expenseDao.getExpensesByCategoryAndInterval(categoryId, startDate, endDate);
    }

    public void deleteExpense(Expense expense) {
        expenseDao.deleteExpense(expense);
    }

    public long getTotalSpentForCategoryAndInterval(int categoryId, long start, long end) {
        return Long.MIN_VALUE; // TODO fix this
    }
}
