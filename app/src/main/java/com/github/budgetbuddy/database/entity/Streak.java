package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "streak")
public class Streak {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int counter;
    public long last_updated;
    public long start_Date;

}
