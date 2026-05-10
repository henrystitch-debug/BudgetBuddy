package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long amountInCents;
    public int categoryId;
    public long entryDateStartInMilliSec;

    public String note;
    public String repeat;
}
