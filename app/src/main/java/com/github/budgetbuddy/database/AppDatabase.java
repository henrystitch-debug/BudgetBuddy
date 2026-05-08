package com.github.budgetbuddy.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

    @Database(entities = {Expense.class, Category.class, Budget.class, Streak.class, Settings.class}, version = 2)
    public abstract class AppDatabase extends RoomDatabase {

        public abstract ExpenseDao expenseDao();
        public abstract CategoryDao categoryDao();
        public abstract BudgetDao budgetDao();
        public abstract StreakDao streakDao();
        public abstract SettingsDao settingsDao();

        private static volatile AppDatabase INSTANCE;

        public static AppDatabase getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (AppDatabase.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                "budgetbuddy.db"
                        ).addCallback(new RoomDatabase.Callback() {
                            @Override
                            public void onCreate(SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                // Runs ONCE when the database is first created
                                // Insert your default categories here with raw SQL
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Food', '🍔')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Transport', '🚗')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Shopping', '🛍️')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Health', '💊')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Entertainment', '🎬')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Housing', '🏠')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Travel', '✈️')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Education', '📚')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Sports', '⚽')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Utilities', '💡')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Pets', '🐾')");
                                db.execSQL("INSERT INTO category (name, icon) VALUES ('Gifts', '🎁')");
                            }
                        }).fallbackToDestructiveMigration(true)
                                .build();
                    }
                }
            }
            return INSTANCE;
        }
    }

