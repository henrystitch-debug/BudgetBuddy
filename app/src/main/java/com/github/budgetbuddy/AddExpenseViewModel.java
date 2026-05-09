package com.github.budgetbuddy;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.repository.CategoryRepository;
import com.github.budgetbuddy.database.repository.ExpenseRepository;

import java.util.List;

public class AddExpenseViewModel extends AndroidViewModel {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public AddExpenseViewModel(Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        expenseRepository = new ExpenseRepository(application);
    }

    // Returns all categories to populate the grid
    public LiveData<List<Category>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    // Saves the expense on a background thread (DB work must not run on the main thread)
    public void saveExpense(double amount, int categoryId, String note) {
            Expense expense = new Expense();
            expense.amount = amount;
            expense.categoryId = categoryId;
            expense.note = note;
            expense.entryDate = System.currentTimeMillis();
            expenseRepository.insert(expense);
    }
}