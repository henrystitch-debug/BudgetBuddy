package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class Settings {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String currency;
    public boolean notifsEnabled;
}
