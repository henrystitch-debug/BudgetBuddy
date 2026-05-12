package com.github.budgetbuddy.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.api.ClaudeApiHelper;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.repository.BudgetRepository;
import com.github.budgetbuddy.database.repository.CategoryRepository;
import com.github.budgetbuddy.database.repository.ExpenseRepository;
import com.github.budgetbuddy.utils.MoneyUtils;
import com.github.budgetbuddy.utils.TimeUtils;

public class CreateBudgetViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SettingsManager settingsManager;

    // — UI State —
    private final MutableLiveData<Budget>  _budget       = new MutableLiveData<>();
    private final MutableLiveData<Long>    _spentCents   = new MutableLiveData<>();
    private final MutableLiveData<String>  _aiResult     = new MutableLiveData<>();
    private final MutableLiveData<String>  _toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _navigateBack = new MutableLiveData<>();

    public final LiveData<Budget>  budget       = _budget;
    public final LiveData<Long>    spentCents   = _spentCents;
    public final LiveData<String>  aiResult     = _aiResult;
    public final LiveData<String>  toastMessage = _toastMessage;
    public final LiveData<Boolean> navigateBack = _navigateBack;

    // Held in ViewModel so fragment survives rotation
    private int    selectedCategoryId = DBConstants.INVALID;
    private int    existingBudgetId   = DBConstants.INVALID;
    private String currentCurrency    = "";

    public CreateBudgetViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetRepository   = new BudgetRepository(db.budgetDao());
        expenseRepository  = new ExpenseRepository(db.expenseDao());
        categoryRepository = new CategoryRepository(db.categoryDao());
        settingsManager    = ((BudgetBuddyApp) application).getSettingsManager();
        currentCurrency    = settingsManager.getCurrency();
    }

    public String getCurrency() {
        return currentCurrency;
    }

    public int getSelectedCategoryId() {
        return selectedCategoryId;
    }


    public void onCategorySelected(int categoryId) {
        selectedCategoryId = categoryId;
        loadBudgetForCategory(categoryId);
    }

    public void loadBudgetForCategory(int categoryId) {
        long start = TimeUtils.getStartOfMonth(0);
        long end   = TimeUtils.getEndOfMonth(0);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Budget budget     = budgetRepository.getBudgetForCategoryAndMonth(categoryId, start, end);
            long   spentCents = expenseRepository.getTotalSpentForCategoryAndInterval(categoryId, start, end);

            existingBudgetId = budget != null ? budget.id : -1;
            _budget.postValue(budget);
            _spentCents.postValue(spentCents);
        });
    }


    public void requestAiRecommendation() {
    if (selectedCategoryId == DBConstants.INVALID) return;

    _aiResult.postValue(null);

    AppDatabase.databaseWriteExecutor.execute(() -> {
        long m0 = expenseRepository.getTotalSpentForCategoryAndInterval(
                selectedCategoryId, TimeUtils.getStartOfMonth(0), TimeUtils.getEndOfMonth(0));
        long m1 = expenseRepository.getTotalSpentForCategoryAndInterval(
                selectedCategoryId, TimeUtils.getStartOfMonth(1), TimeUtils.getEndOfMonth(1));
        long m2 = expenseRepository.getTotalSpentForCategoryAndInterval(
                selectedCategoryId, TimeUtils.getStartOfMonth(2), TimeUtils.getEndOfMonth(2));
        long avg = (m0 + m1 + m2) / 3L;

        // ── CHANGED: fetch name from DB via repository instead of CategoryUtils ──
        Category cat     = categoryRepository.getCategoryById(selectedCategoryId);
        String catName   = cat != null ? cat.name : "";
        // ────────────────────────────────────────────────────────────────────────

        String m0Display  = MoneyUtils.fromCentsDisplay(m0,  currentCurrency);
        String m1Display  = MoneyUtils.fromCentsDisplay(m1,  currentCurrency);
        String avgDisplay = MoneyUtils.fromCentsDisplay(avg, currentCurrency);

        ClaudeApiHelper.getBudgetRecommendation(
                null,
                catName, currentCurrency, m0Display, m1Display, avgDisplay,
                new ClaudeApiHelper.ApiCallback() {
                    @Override public void onSuccess(String recommendation) {
                        _aiResult.postValue(recommendation);
                    }
                    @Override public void onError(String error) {
                        _aiResult.postValue("⚠️ " + error);
                    }
                }
        );
    });
}
    // ── Save budget ───────────────────────────────────────────────────
    public void saveBudget(String amountStr) {
        if (selectedCategoryId == DBConstants.INVALID) {
            String s = getStringFromResource(R.string.empty_cat_prompt);
            _toastMessage.postValue(s);
            return;
        }
        if (amountStr == null || amountStr.trim().isEmpty()) {
            String s = getStringFromResource(R.string.empty_cat_prompt);
            _toastMessage.postValue(s);
            return;
        }
        if (!MoneyUtils.isValidMoneyInput(amountStr)) {
            String s = getStringFromResource(R.string.invalid_amount_prompt);
            _toastMessage.postValue(s);
            return;
        }

        long limitInCents = MoneyUtils.toCents(amountStr);
        if (limitInCents <= 0) {
            String s = getStringFromResource(R.string.nonpos_amount_prompt);
            _toastMessage.postValue(s);
            return;
        }

        long start = TimeUtils.getStartOfMonth(0);
        long end   = TimeUtils.getEndOfMonth(0);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (existingBudgetId > 0) {
                budgetRepository.updateBudget(existingBudgetId, limitInCents, start, end);
            } else {
                Budget newBudget         = new Budget();
                newBudget.limitInCents   = limitInCents;
                newBudget.currentAmountInCents = 0L;
                newBudget.startDate      = start;
                newBudget.endDate        = end;
                newBudget.categoryId = selectedCategoryId;
                long newId = budgetRepository.insertBudgetGetId(newBudget);
                existingBudgetId = (int) newId;
            }
            _toastMessage.postValue("Budget saved!");
            loadBudgetForCategory(selectedCategoryId);
        });
    }

    private String getStringFromResource(int resourceId) {
        return getApplication().getString(resourceId);
    }
}
