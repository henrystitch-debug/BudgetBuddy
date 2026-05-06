package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double amount;
    public int categoryId;
    public long entryDate;
    public String note;
    public String repeat;
    public int profileId;
}
