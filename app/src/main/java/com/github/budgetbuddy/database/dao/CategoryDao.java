package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;

@Dao
public interface CategoryDao {

    @Insert
    void insertCategory(Category category);

    @Query("SELECT * FROM category WHERE id = :id")
    Category getCategoryById(int id);

    @Delete
    void deleteCategory(Category category);
}
