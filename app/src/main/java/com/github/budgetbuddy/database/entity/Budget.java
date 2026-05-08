package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double limit;
    public double current_amount;
    public int categoryId;
    public long startDate;
    public long endDate;
}
