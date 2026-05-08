package com.github.budgetbuddy.database.dao;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT * FROM category")
    LiveData<List<Category>> getCategories();

    @Delete
    void deleteCategory(Category category);
}
