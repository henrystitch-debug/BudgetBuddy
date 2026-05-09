package com.github.budgetbuddy.database.repository;

import android.app.Application;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;
import java.util.List;

/**
 * Repository class for managing Expense data.
 * Normalizes {@link Expense#entryDate} to the start of the day (midnight millis) on every
 * insert and update, so that {@link ExpenseDao#getDailySpending} can group rows correctly
 * using a plain {@code GROUP BY entryDate}.
 */
public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    /**
     * Inserts a new expense. {@code expense.entryDate} is normalized to start-of-day millis
     * (midnight, local time) before persisting. This modifies the passed object in place.
     */
    public void insert(Expense expense) {
        expense.entryDate = toStartOfDay(expense.entryDate);
        executor.execute(()->expenseDao.insert(expense));
    }

    /**
     * Updates an existing expense. {@code entryDate} is normalized to start-of-day millis.
     */
    public void updateExpense(int id, double amount, int categoryId, long entryDate,
                              String note, String repeat) {
        expenseDao.updateExpense(id, amount, categoryId, toStartOfDay(entryDate), note, repeat);
    }

    public Expense getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }

    public List<Expense> getExpensesOfSpecificDate(long date) {
        return expenseDao.getExpensesOfSpecificDate(toStartOfDay(date));
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

    /**
     * Truncates a millisecond timestamp to the start of its calendar day using the device's
     * default (local) time zone, so that {@link ExpenseDao#getDailySpending} can group expenses
     * by day with a simple {@code GROUP BY entryDate}.
     */
    static long toStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
