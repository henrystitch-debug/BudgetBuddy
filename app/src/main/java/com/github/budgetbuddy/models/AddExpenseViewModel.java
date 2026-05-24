package com.github.budgetbuddy.models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.repository.BudgetRepository;
import com.github.budgetbuddy.database.repository.CategoryRepository;
import com.github.budgetbuddy.database.repository.ExpenseRepository;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.List;

public class AddExpenseViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository  expenseRepository;
    private final BudgetRepository   budgetRepository;

    private final LiveData<List<Category>> categories;

    public AddExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryRepository = new CategoryRepository(db.categoryDao());
        expenseRepository  = new ExpenseRepository(db.expenseDao());
        budgetRepository   = new BudgetRepository(db.budgetDao());

        MutableLiveData<List<Category>> mutableCategories = new MutableLiveData<>();
        this.categories = mutableCategories;
        AppDatabase.databaseWriteExecutor.execute(() ->
            mutableCategories.postValue(categoryRepository.getAllCategories()));
    }

    /** Returns all categories to populate the grid. */
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public Expense getExpenseById(int expenseId) {
        return expenseRepository.getExpenseById(expenseId);
    }

    /**
     * Save a brand-new expense.
     *
     * Looks up the active budget for this category + current month on the
     * background thread and links it via budgetId (NULL if none exists).
     */
    public void saveExpense(long amountInCents, int categoryId, String note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int budgetId = resolveActiveBudgetId(categoryId);

            Expense expense = new Expense();
            expense.amountInCents           = amountInCents;
            expense.categoryId              = categoryId;
            expense.note                    = note;
            expense.entryDateStartInMilliSec = TimeUtils.toStartOfDay(System.currentTimeMillis());
            expense.budgetId                = budgetId;

            expenseRepository.insert(expense);
        });
    }

    /**
     * Update an existing expense.
     *
     * Re-resolves the budgetId in case the user changed the category —
     * the expense should now be charged to the budget for the new
     * category, not the original one.
     */
    public void updateExpense(int expenseId,
                              long amountInCents,
                              int categoryId,
                              String note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int budgetId = resolveActiveBudgetId(categoryId);

            expenseRepository.updateExpense(
                    expenseId,
                    amountInCents,
                    categoryId,
                    System.currentTimeMillis(),
                    note,
                    "",
                    budgetId
            );
        });
    }


    /**
     * Returns the id of the active budget for the given category in the
     * current month, or NULL if no budget has been created yet.
     *
     * Must be called from a background thread.
     */
    private int resolveActiveBudgetId(int categoryId) {
        long start  = TimeUtils.getStartOfMonth(0);
        long end    = TimeUtils.getEndOfMonth(0);
        Budget budget = budgetRepository.getBudgetForCategoryAndMonth(categoryId, start, end);
        return budget != null ? budget.id : DBConstants.INVALID;
    }
}
