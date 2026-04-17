package com.github.budgetbuddy.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Expense;

import java.util.List;
//TODO: add queries
public interface ExpenseDao {
        @Insert
        void insert(Expense expense);

       /* @Query("SELECT * FROM Expense")
        List<Expense> getAll();*/

        @Delete
        void delete(Expense expense);
}
