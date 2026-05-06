package com.github.budgetbuddy.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.dao.ProfileDao;
import com.github.budgetbuddy.database.dao.SettingsDao;
import com.github.budgetbuddy.database.dao.StreakDao;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Profile;
import com.github.budgetbuddy.database.entity.Settings;
import com.github.budgetbuddy.database.entity.Streak;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class, Category.class, Budget.class, Streak.class, Settings.class, Profile.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract StreakDao streakDao();
    public abstract SettingsDao settingsDao();
    public abstract ProfileDao profileDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    private static volatile AppDatabase INSTANCE;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `profile` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
            database.execSQL("ALTER TABLE `budget` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `expense` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `budget` ADD COLUMN `categoryId` INTEGER NOT NULL DEFAULT 0");
            // Backfill categoryId from the existing category.budgetId mapping
            database.execSQL(
                "UPDATE `budget` SET `categoryId` = COALESCE(" +
                    "(SELECT `id` FROM `category` WHERE `category`.`budgetId` = `budget`.`id` LIMIT 1), 0)"
            );
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `settings` ADD COLUMN `activeProfileId` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `streak` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 0");
            // If there's already a profile in DB, mark the first one as active so
            // existing dev installs don't get pushed back through onboarding
            database.execSQL(
                "UPDATE `settings` SET `activeProfileId` = COALESCE(" +
                    "(SELECT `id` FROM `profile` ORDER BY `id` ASC LIMIT 1), 0)"
            );
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "budget_buddy_database"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            databaseWriteExecutor.execute(() -> {
                                AppDatabase database = INSTANCE;

                                CategoryDao categoryDao = database.categoryDao();
                                List<Category> existingCategories = categoryDao.getAllCategories();
                                String[] names = {
                                        "Food", "Home", "Transport", "School",
                                        "Health", "Shopping", "Fun", "Other",
                                        "Coffee", "Travel", "Gift", "Pet"
                                };
                                Set<String> existingNames = new HashSet<>();
                                if (existingCategories != null) {
                                    for (Category c : existingCategories) {
                                        if (c.name != null) existingNames.add(c.name);
                                    }
                                }
                                for (String name : names) {
                                    if (!existingNames.contains(name)) {
                                        Category cat = new Category();
                                        cat.name = name;
                                        cat.budgetId = 0;
                                        categoryDao.insertCategory(cat);
                                    }
                                }

                                SettingsDao settingsDao = database.settingsDao();
                                Settings existingSettings = settingsDao.getSettings();
                                if (existingSettings == null) {
                                    Settings settings = new Settings();
                                    settings.currency = "€";
                                    settings.notifsEnabled = false;
                                    settings.activeProfileId = 0;
                                    settingsDao.insertSettings(settings);
                                }
                            });
                        }
                    }).build();
                }
            }
        }
        return INSTANCE;
    }
}
