package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.github.budgetbuddy.database.entity.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insertBudget(Budget budget);

    @Update
    void updateBudget(Budget budget);

    @Query("SELECT * FROM budget WHERE startDate <= :date AND endDate >= :date LIMIT 1")
    Budget getActiveBudget(long date);

    @Query("SELECT * FROM budget WHERE startDate >= :start AND endDate <= :end")
    List<Budget> getBudgetsInInterval(long start, long end);

    @Query("SELECT * FROM budget WHERE id = :id")
    Budget getBudgetById(int id);

    @Query("UPDATE budget SET current_amount = current_amount + :amount WHERE id = :id")
    void incrementCurrentAmount(int id, int amount);

    @Delete
    void deleteBudget(Budget budget);
}
