package com.github.budgetbuddy.database.repository;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.entity.Budget;

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
        return budgetDao.getBudgetsInInterval(start, end);
    }

    /**
     * Increments the spent amount for a specific budget.
     * Useful when a new expense is added.
     */
    public void addToSpentAmount(int budgetId, int amount) {
        budgetDao.incrementCurrentAmount(budgetId, amount);
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
    public boolean isBudgetExceeded(long currentTimeMillis) {
        Budget active = getActiveBudget(currentTimeMillis);
        return active != null && active.current_amount > active.limit;
    }
}
