package com.github.budgetbuddy.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "category")
public class Category {
    public static final int DEFAULT_CATEGORY = 69;
    public static final String DEFAULT_NAME = "default";
    public static final String DEFAULT_ICON = ":)";

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String icon;   // was "icon", rename or keep
    public String color;   // hex string e.g. "#4A7C7C"

    public Category(String name, String emoji, String colorHex) {
        this.name = name;
        this.icon = emoji;
        this.color = colorHex;
    }

    public Category() {
    }
}
