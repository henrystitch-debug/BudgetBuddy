package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insertCategory(Category category);

    @Query("SELECT * FROM category WHERE id = :id")
    Category getCategoryById(int id);

    @Delete
    void deleteCategory(Category category);

    @Query("SELECT * FROM category")
    List<Category> getAllCategories();

    @Query("UPDATE category SET budgetId = :budgetId WHERE id = :id")
    void updateBudgetId(int id, int budgetId);
}
