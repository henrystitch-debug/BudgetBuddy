package com.github.budgetbuddy.database.entity;

import androidx.room.PrimaryKey;

public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double amount;
    public int categoryId;
    public long date;
    public String note;
    public String repeat;
}
