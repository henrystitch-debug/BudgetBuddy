package com.github.budgetbuddy.models;

import android.app.Application;
import android.util.Log;

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
    private final LiveData<List<Category>> categories;

    public AddExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryRepository = new CategoryRepository(db.categoryDao());
        expenseRepository = new ExpenseRepository(db.expenseDao());
        categories = categoryRepository.getAllCategories();
    }

    // Returns all categories to populate the grid
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public void updateExpense(int expenseId,
                              long amountInCents,
                              int categoryId,
                              String note) {

        AppDatabase.databaseWriteExecutor.execute(() -> {

            expenseRepository.updateExpense(
                    expenseId,
                    amountInCents,
                    categoryId,
                    System.currentTimeMillis(),
                    note,
                    ""
            );
        });
    }

    public Expense getExpenseById(int expenseId) {
        return expenseRepository.getExpenseById(expenseId);
    }

    public void saveExpense(long amountInCents, int categoryId, String note) {
            Expense expense = new Expense();
            expense.amountInCents = amountInCents;
            expense.categoryId = categoryId;
            expense.note = note;
            expense.entryDateStartInMilliSec = TimeUtils.toStartOfDay(System.currentTimeMillis());
            AppDatabase.databaseWriteExecutor.execute(()->expenseRepository.insert(expense));
    }
}