package com.github.budgetbuddy.database.entity;

import androidx.room.PrimaryKey;

public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int budgetId;
}
