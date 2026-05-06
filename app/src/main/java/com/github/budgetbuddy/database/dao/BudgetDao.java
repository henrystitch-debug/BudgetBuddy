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

    @Insert
    long insertBudgetGetId(Budget budget);

    @Query("UPDATE budget SET `limit` = :limit, startDate = :startDate, endDate = :endDate WHERE id = :id")
    void updateBudget(int id, int limit, long startDate, long endDate);

    @Query("SELECT * FROM budget WHERE startDate = :start AND endDate = :end")
    List<Budget> getAllBudgetsOfThisMonth(long start, long end);

    @Query("SELECT * FROM budget WHERE id = :id")
    Budget getBudgetById(int id);

    @Delete
    void deleteBudget(Budget budget);

    @Query("SELECT COALESCE(SUM(`limit`), 0) FROM budget WHERE startDate >= :startDate AND endDate <= :endDate")
    double getTotalBudgetLimitForInterval(long startDate, long endDate);

    @Query("SELECT * FROM budget WHERE profileId = :profileId")
    List<Budget> getBudgetsByProfile(int profileId);

    @Query("DELETE FROM budget WHERE profileId = :profileId")
    void deleteByProfileId(int profileId);

    @Query("SELECT * FROM budget WHERE profileId = :profileId AND categoryId = :categoryId AND startDate = :start AND endDate = :end LIMIT 1")
    Budget getBudgetForProfileCategoryInterval(int profileId, int categoryId, long start, long end);
}
