package com.github.budgetbuddy.database.entity;

import androidx.room.PrimaryKey;

public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int limit;
    public int current_amount;
}
