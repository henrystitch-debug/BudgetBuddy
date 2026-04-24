package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Streak;

@Dao
public interface StreakDao {
    @Insert
    void insertNewStreak(Streak streak);

    @Query("UPDATE streak SET counter = :amount WHERE id = :id")
    void updateStreak(int id, double amount);

    @Query("SELECT * FROM streak WHERE id = :id")
    Streak getStreakById(int id);

    @Query("SELECT MAX(counter) FROM streak")
    int getLongestStreak();

}
