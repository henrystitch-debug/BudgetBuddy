package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile")
public class Profile {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
