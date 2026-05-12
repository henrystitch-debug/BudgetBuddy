package com.github.budgetbuddy.database.repository;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.List;

/**
 * Repository class for managing Budget data.
 */
public class BudgetRepository {

    private final BudgetDao budgetDao;

    public BudgetRepository(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    /**
     * Creates a new budget.
     */
    public void createBudget(Budget budget) {
        budgetDao.insertBudget(budget);
    }

    /**
     * Updates an existing budget's details.
     */
    public void updateBudget(Budget budget) {
        budgetDao.updateBudget(budget);
    }

    /**
     * Retrieves the budget currently active for the given timestamp.
     */
    public Budget getActiveBudget(long currentTimeMillis) {
        return budgetDao.getActiveBudget(currentTimeMillis);
    }

    /**
     * Finds a budget by its unique ID.
     */
    public Budget getBudgetById(int id) {
        return budgetDao.getBudgetById(id);
    }

    /**
     * Returns all budgets that fall within the specified time range.
     */
    public List<Budget> getBudgetsInRange(long start, long end) {
        long startOfDay = TimeUtils.toStartOfDay(start);
        long endOfDay  =  TimeUtils.toEndOfDay(end);
        return budgetDao.getBudgetsInInterval(startOfDay, endOfDay);
    }

    /**
     * Increments the spent amount for a specific budget.
     * Useful when a new expense is added.
     */
    public void addToSpentAmount(int budgetId, long amountInCents) {
        budgetDao.incrementCurrentAmount(budgetId, amountInCents);
    }

    /**
     * Removes a budget from the database.
     */
    public void deleteBudget(Budget budget) {
        budgetDao.deleteBudget(budget);
    }

    /**
     * Checks if the active budget is exceeded.
     */
    public boolean isActiveBudgetExceeded(long currentTimeMillis) {
        long startOfDay = TimeUtils.toStartOfDay(currentTimeMillis);
        Budget active = getActiveBudget(startOfDay);
        return active != null && active.currentAmountInCents > active.limitInCents;
    }

    public Budget getBudgetForCategoryAndMonth(int categoryId, long start, long end) {
        return budgetDao.getBudgetByCategoryAndInterval(categoryId, start, end);
    }

    public void updateBudget(int existingBudgetId, long limitInCents, long start, long end) {
        this.budgetDao.updateBudget(existingBudgetId, limitInCents, start, end);
    }

    public long insertBudgetGetId(Budget newBudget) {
        return budgetDao.insertBudget(newBudget);
    }
}
