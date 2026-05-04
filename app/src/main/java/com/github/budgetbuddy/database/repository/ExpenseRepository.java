package com.github.budgetbuddy.database.repository;

import android.app.Application;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    public void insert(Expense expense) {
        executor.execute(()->expenseDao.insert(expense));
    }
}
