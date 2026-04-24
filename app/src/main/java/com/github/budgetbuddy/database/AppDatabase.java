package com.github.budgetbuddy.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class, Category.class, Budget.class, Streak.class, Settings.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
        private static volatile AppDatabase INSTANCE;
        private static final int NUMBER_OF_THREADS = 4;
        public static final ExecutorService databaseWriteExecutor =
                Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        public static AppDatabase getInstance(final Context context) {
            if (INSTANCE == null) {
                synchronized (AppDatabase.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class, "budget_buddy_db"
                        ).build();
                    }
                }
            }
            return INSTANCE;
        }

        public abstract ExpenseDao expenseDao();
        public abstract CategoryDao categoryDao();
        public abstract BudgetDao budgetDao();
        public abstract StreakDao streakDao();
        public abstract SettingsDao settingsDao();
    }

