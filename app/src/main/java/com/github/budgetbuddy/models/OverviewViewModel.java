package com.github.budgetbuddy.models;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Streak;
import com.github.budgetbuddy.database.repository.BudgetRepository;
import com.github.budgetbuddy.database.repository.CategoryRepository;
import com.github.budgetbuddy.database.repository.ExpenseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewViewModel extends AndroidViewModel {

    private static final String TAG = "BudgetBuddy_OverviewVM";

    private final ExpenseRepository  expenseRepository;
    private final BudgetRepository   budgetRepository;
    private final CategoryRepository categoryRepository;
    private final SettingsManager    settingsManager;


    /** Greeting line, e.g. "Hey, Alice!" */
    private final MutableLiveData<String> _greeting     = new MutableLiveData<>();
    public  final LiveData<String>         greeting      = _greeting;

    /** Subtitle line — streak or default prompt. */
    private final MutableLiveData<String> _subtitle     = new MutableLiveData<>();
    public  final LiveData<String>         subtitle      = _subtitle;

    /** Currency symbol read from SettingsManager. */
    private final MutableLiveData<String> _currency     = new MutableLiveData<>();
    public  final LiveData<String>         currency      = _currency;

    /**
     * Everything the Overview needs to render one refresh:
     * categories, expenses, budget progress, and totals.
     */
    private final MutableLiveData<OverviewData> _overviewData = new MutableLiveData<>();
    public  final LiveData<OverviewData>          overviewData  = _overviewData;

    public OverviewViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db     = AppDatabase.getDatabase(application);
        expenseRepository  = new ExpenseRepository(db.expenseDao());
        budgetRepository   = new BudgetRepository(db.budgetDao());
        categoryRepository = new CategoryRepository(db.categoryDao());
        settingsManager    = ((BudgetBuddyApp) application).getSettingsManager();
    }

    // ── Public API called by the fragment ──────────────────────────────────

    /**
     * Load the greeting, streak, and currency once on first view creation.
     * Safe to call multiple times — cheap SharedPrefs read on the executor.
     */
    public void loadGreetingAndCurrency() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String name     = settingsManager.getUserName();
            String currency = settingsManager.getCurrency();

            AppDatabase db  = AppDatabase.getDatabase(getApplication());
            Streak streak   = db.streakDao().getCurrentStreak();
            int streakCount = streak != null ? streak.counter : 0;

            _greeting.postValue("Hey, " + (name != null ? name : "there") + "!");
            _currency.postValue(currency);

            if (streakCount > 0) {
                _subtitle.postValue("🔥 " + streakCount + "-day streak — keep it up!");
            } else {
                // Post the string resource value; fragment resolves the resource itself
                _subtitle.postValue(null); // null = use R.string.log_xpense default
            }
        });
    }

    /**
     * Reload all overview data for the given date range.
     * Called whenever the user switches tabs or the fragment resumes.
     */
    public void loadData(long startDate, long endDate) {
         AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String currency = settingsManager.getCurrency();

                List<Category> allCategories = categoryRepository.getAllCategories();
                Map<Integer, Category> categoryMap = new HashMap<>();
                for (Category c : allCategories) categoryMap.put(c.id, c);

                List<Expense> allExpenses = expenseRepository.getExpensesInterval(startDate, endDate);
                Log.d(TAG, "Loaded " + allExpenses.size() + " expenses for interval.");

                // 3. Category totals for the pie chart
                Map<Integer, Long> categoryTotals = new HashMap<>();
                for (Expense e : allExpenses) {
                    categoryTotals.merge(e.categoryId, e.amountInCents, Long::sum);
                }
                long totalSpent = 0;
                for (long v : categoryTotals.values()) totalSpent += v;

                // 4. Recent expenses list
                List<Expense> recentExpenses =
                        expenseRepository.getRecentExpenses(startDate, endDate, 100);

                // 5. Budgets for the period
                List<Budget> budgets = budgetRepository.getBudgetsInTimeRange(startDate, endDate);
                Log.d(TAG, "Found " + budgets.size() + " budgets in range.");

                List<BudgetProgress> budgetProgressList = new ArrayList<>();
                for (Budget budget : budgets) {
                    long spentCents = expenseRepository.getTotalSpentForCategoryAndInterval(
                            budget.categoryId, budget.startDate, budget.endDate);
                    
                    Log.d(TAG, "Budget ID: " + budget.id + ", Category: " + budget.categoryId + 
                          ", Spent: " + spentCents + ", Limit: " + budget.limitInCents);
                    
                    budgetProgressList.add(new BudgetProgress(budget, spentCents, categoryMap));
                }

                _currency.postValue(currency);
                _overviewData.postValue(new OverviewData(
                        categoryMap,
                        categoryTotals,
                        totalSpent,
                        recentExpenses,
                        budgetProgressList,
                        currency
                ));
            } catch (Exception e) {
                Log.e(TAG, "Error loading overview data", e);
            }
         });
    }

    // ── Data classes posted to LiveData ────────────────────────────────────
    /** All data needed to render a single Overview refresh. */
    public static class OverviewData {
        public final Map<Integer, Category> categoryMap;
        public final Map<Integer, Long>     categoryTotals;
        public final long                   totalSpent;
        public final List<Expense>          recentExpenses;
        public final List<BudgetProgress>   budgetProgressList;
        public final String                 currency;

        public OverviewData(Map<Integer, Category> categoryMap,
                            Map<Integer, Long>     categoryTotals,
                            long                   totalSpent,
                            List<Expense>          recentExpenses,
                            List<BudgetProgress>   budgetProgressList,
                            String                 currency) {
            this.categoryMap        = categoryMap;
            this.categoryTotals     = categoryTotals;
            this.totalSpent         = totalSpent;
            this.recentExpenses     = recentExpenses;
            this.budgetProgressList = budgetProgressList;
            this.currency           = currency;
        }
    }

    /** One row of budget progress data, pre-resolved for the fragment to render. */
    public static class BudgetProgress {
        public final int    budgetId;
        public final int    categoryId;
        public final String categoryLabel;
        public final long   limitInCents;
        public final long   spentInCents;

        public BudgetProgress(Budget budget, long spentInCents,
                              Map<Integer, Category> categoryMap) {
            this.budgetId      = budget.id;
            this.categoryId    = budget.categoryId;
            this.limitInCents  = budget.limitInCents;
            this.spentInCents  = spentInCents;

            Category cat       = categoryMap.get(budget.categoryId);
            this.categoryLabel = cat != null ? cat.name : "Unknown";
        }
    }
}
