package com.github.budgetbuddy.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.entity.Category;

import java.util.List;

public class CategoryRepository {
    private final CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        categoryDao = db.categoryDao();
    }

    // Just wraps the DAO method — ViewModel calls this instead of the DAO directly
    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getCategories();
    }
}
