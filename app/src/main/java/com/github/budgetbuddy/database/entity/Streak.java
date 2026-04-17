package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;

@Entity(tableName = "streak")
public class Streak {
    public int id;
    public int counter;
    public long last_updated;
    public long start_Date;

}
