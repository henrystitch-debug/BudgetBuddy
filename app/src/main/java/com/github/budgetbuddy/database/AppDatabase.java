package com.github.budgetbuddy.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.dao.SettingsDao;
import com.github.budgetbuddy.database.dao.StreakDao;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Streak;
import com.github.budgetbuddy.database.entity.Settings;

    @Database(entities = {Expense.class, Category.class, Budget.class, Streak.class, Settings.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {

        public abstract ExpenseDao expenseDao();
        public abstract CategoryDao categoryDao();
        public abstract BudgetDao budgetDao();
        public abstract StreakDao streakDao();
        public abstract SettingsDao settingsDao();
    }

