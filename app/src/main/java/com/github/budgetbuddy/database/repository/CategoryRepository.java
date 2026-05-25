package com.github.budgetbuddy.database.repository;

import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.entity.Category;

import java.util.List;

public class CategoryRepository {
    private final CategoryDao categoryDao;

    public CategoryRepository(CategoryDao dao) {
        categoryDao = dao;
    }

    // Just wraps the DAO method — ViewModel calls this instead of the DAO directly
    public List<Category> getAllCategories() {
        return categoryDao.getCategories();
    }

    public Category getCategoryById(int id) {
        return categoryDao.getCategoryById(id);
    }
}
