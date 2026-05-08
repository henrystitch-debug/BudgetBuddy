package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.github.budgetbuddy.database.entity.Settings;

import java.util.List;

@Dao
public interface SettingsDao {

    @Query("UPDATE settings SET currency = :currency, notifsEnabled = :enabled WHERE id = :id")
    void updateSettings(String currency, boolean enabled, int id);

    @Query("SELECT * FROM settings")
    List<Settings> getAllSettings();

    @Query("SELECT * FROM settings WHERE id = :id")
    Settings getSettings(int id);

    @Insert
    void insertSettings(Settings settings);
}
