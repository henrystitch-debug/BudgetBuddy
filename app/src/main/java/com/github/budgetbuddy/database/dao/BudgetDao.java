package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insertBudget(Budget budget);

    @Query("SELECT * FROM budget WHERE startDate = :start AND endDate = :end")
    List<Budget> getAllBudgetsOfThisMonth(long start, long end);

    @Query("SELECT * FROM budget WHERE id = :id")
    Budget getBudgetById(int id);

    @Delete
    void deleteBudget(Budget budget);
}
