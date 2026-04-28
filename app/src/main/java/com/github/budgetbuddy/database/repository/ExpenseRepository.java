package com.github.budgetbuddy.database.repository;

import android.app.Application;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;


public class ExpenseRepository {
    private final ExpenseDao expenseDao;

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    public void insert(Expense expense) {
        expenseDao.insert(expense);
    }
}
