package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;

@Entity(tableName = "settings")
public class Settings {

    public int id;
    public String currency;
    public boolean notifsEnabled;
}
