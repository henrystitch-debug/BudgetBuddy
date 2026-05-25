package com.github.budgetbuddy.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense",
        foreignKeys = @ForeignKey(
                entity = Budget.class,
                parentColumns = "id",
                childColumns = "budget_id",
                // delete all expenses, associated with this budgetID
                // onUpdate, the foreign key is automatically changed.
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("budget_id")}
)
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long amountInCents;
    public int categoryId;

    /**
     * The budget this expense is charged against, or NULL if no budget
     * exists for this category in the current month.
     *
     * Populated automatically by AddExpenseViewModel at save/update time
     * by querying BudgetRepository for the active budget for this
     * category + month. This replaces the old pattern of computing
     * "spent" by summing categoryId across expenses — the Overview can
     * now query expenses directly by budgetId.
     */
    @ColumnInfo(name = "budget_id")
    public Integer budgetId;
    public long entryDateStartInMilliSec;
    public String note;
    public String repeat;
}
