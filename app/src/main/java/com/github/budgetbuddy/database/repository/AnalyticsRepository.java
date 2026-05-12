package com.github.budgetbuddy.database.repository;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Budget;

import java.util.List;

/**
 * Repository class providing high-level analytics for the expense tracker.
 */
public class AnalyticsRepository {
    private final ExpenseDao expenseDao;
    private final BudgetDao budgetDao;

    public AnalyticsRepository(ExpenseDao expenseDao, BudgetDao budgetDao) {
        this.expenseDao = expenseDao;
        this.budgetDao = budgetDao;
    }

    /**
     * Gets the total amount spent within a specific time interval.
     */
    public double getTotalSpending(long startDate, long endDate) {
        Long total = expenseDao.getTotalSpending(startDate, endDate);
        return total != null ? total : 0.0;
    }

    /**
     * Returns a breakdown of spending by category for the given period.
     */
    public List<ExpenseDao.CategorySpending> getSpendingByCategory(long startDate, long endDate) {
        return expenseDao.getSpendingByCategory(startDate, endDate);
    }

    /**
     * Returns daily spending totals, useful for plotting line or bar charts.
     */
    public List<ExpenseDao.DailySpending> getDailySpendingTrend(long startDate, long endDate) {
        return expenseDao.getDailySpending(startDate, endDate);
    }

    /**
     * Calculates the percentage of the budget used for the given period.
     */
    public double getBudgetUsagePercentage(long startDate, long endDate) {
        List<Budget> budgets = budgetDao.getBudgetsInInterval(startDate, endDate);
        if (budgets.isEmpty()) {
            return 0.0;
        }

        double totalLimit = 0;
        for (Budget budget : budgets) {
            totalLimit += budget.limitInCents;
        }

        if (totalLimit == 0) return 0.0;

        double totalSpent = getTotalSpending(startDate, endDate);
        return (totalSpent / totalLimit) * 100;
    }

    /**
     * Gets the highest spending category ID for the given period.
     */
    public int getTopSpendingCategory(long startDate, long endDate) {
        // NOTE: instead of doing this in memory, delegate to the DB using `Order by LIMIT 1`
        List<ExpenseDao.CategorySpending> spending = getSpendingByCategory(startDate, endDate);
        if (spending.isEmpty()) return -1;

        int topCategoryId = -1;
        double maxAmount = -1;

        for (ExpenseDao.CategorySpending item : spending) {
            if (item.totalInCents > maxAmount) {
                maxAmount = item.totalInCents;
                topCategoryId = item.categoryId;
            }
        }
        return topCategoryId;
    }
}
