package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int limit;
    public int current_amount;
    public long startDate;
    public long endDate;
    public int profileId;
    public int categoryId;
}
