package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Settings;

@Dao
public interface SettingsDao {

    @Insert
    void insertSettings(Settings settings);

    @Query("UPDATE settings SET currency = :currency, notifsEnabled = :enabled WHERE id = :id")
    void updateStreak(String currency, boolean enabled, int id);

    @Query("SELECT * FROM settings WHERE id = :id")
    Settings getSettings(int id);

    @Query("SELECT * FROM settings LIMIT 1")
    Settings getSettings();
}
