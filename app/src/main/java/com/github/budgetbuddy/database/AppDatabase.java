package com.github.budgetbuddy.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.dao.StreakDao;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Streak;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class, Category.class, Budget.class, Streak.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract StreakDao streakDao();

    public static final ExecutorService databaseWriteExecutor = Executors
                                                .newFixedThreadPool(4);

    private static volatile AppDatabase INSTANCE;


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DBConstants.DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                AppDatabase database = INSTANCE;

                                CategoryDao categoryDao = database.categoryDao();
                                for (String name : DBConstants.DEFAULT_CATEGORIES) {
                                    Category cat = new Category();
                                    cat.name = name;
                                    categoryDao.insertCategory(cat);
                                }

                                StreakDao streakDao = database.streakDao();
                                Streak streak = new Streak();
                                streak.counter = 0;
                                streak.last_updated = System.currentTimeMillis();
                                streak.start_Date = System.currentTimeMillis();
                                streakDao.insertNewStreak(streak);
                            });
                        }
                    }).build();
                }
            }
        }
        return INSTANCE;
    }
}
