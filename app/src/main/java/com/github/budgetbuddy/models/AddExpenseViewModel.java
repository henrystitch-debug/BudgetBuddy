package com.github.budgetbuddy.models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.repository.CategoryRepository;
import com.github.budgetbuddy.database.repository.ExpenseRepository;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.List;

public class AddExpenseViewModel extends AndroidViewModel {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public AddExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryRepository = new CategoryRepository(db.categoryDao());
        expenseRepository = new ExpenseRepository(db.expenseDao());
    }

    // Returns all categories to populate the grid
    public LiveData<List<Category>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    public void saveExpense(long amountInCents, int categoryId, String note) {
            Expense expense = new Expense();
            expense.amountInCents = amountInCents;
            expense.categoryId = categoryId;
            expense.note = note;
            expense.entryDateStartInMilliSec = TimeUtils.toStartOfDay(System.currentTimeMillis());
            expenseRepository.insert(expense);
    }
}