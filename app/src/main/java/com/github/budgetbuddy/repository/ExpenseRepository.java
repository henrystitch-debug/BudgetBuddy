package com.github.budgetbuddy.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;

import java.util.List;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return expenseDao.getAll();
    }

    public void insert(Expense expense) {
        // Always run DB writes off the main thread
        AppDatabase.databaseWriteExecutor.execute(() ->
                expenseDao.insert(expense)
        );
    }
}