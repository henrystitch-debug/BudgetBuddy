package com.github.budgetbuddy.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget",
        foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "category_id",
        // NOTE: we don't want to delete all the budgets associated
        // with an id, instead change its id to some default Category e.g `Other`
        onDelete = ForeignKey.SET_DEFAULT
),
indices = {@Index("category_id")}
)
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    // NOTE: we store any money related values in cents
    // Caller must make sure, conversion from other units is done before being called here
    public long limitInCents;
    public long currentAmountInCents;
    // a deleted category should not result in loss of its associated budget
    @ColumnInfo(name = "category_id", defaultValue = "1")
    public int categoryId;
    public long startDate;
    public long endDate;
}
