package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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

    @Query("SELECT * FROM streak ORDER BY last_updated DESC, id DESC LIMIT 1")
    Streak getCurrentStreak();

    @Query("UPDATE streak SET counter = :count, last_updated = :timestamp WHERE id = :id")
    void updateCurrentStreak(int id, int count, long timestamp);
}
