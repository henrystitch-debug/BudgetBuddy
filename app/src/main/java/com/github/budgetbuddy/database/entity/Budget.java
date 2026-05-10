package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    // NOTE: we store any money related values in cents
    // Caller must make sure, conversion from other units is done before being called here
    public long limitInCents;
    public long currentAmountInCents;
    public int categoryId;
    public long startDate;
    public long endDate;
}
