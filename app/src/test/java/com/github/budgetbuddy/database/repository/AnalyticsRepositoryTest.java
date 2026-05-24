package com.github.budgetbuddy.database.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Budget;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnalyticsRepositoryTest {

    @Mock
    private ExpenseDao expenseDao;

    @Mock
    private BudgetDao budgetDao;

    private AnalyticsRepository analyticsRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        analyticsRepository = new AnalyticsRepository(expenseDao, budgetDao);
    }

    @Test
    public void getTotalSpending_returnsValueFromDao() {
        long start = 1000L;
        long end = 2000L;
        when(expenseDao.getTotalSpending(start, end)).thenReturn(5000L);

        double result = analyticsRepository.getTotalSpending(start, end);

        assertEquals(5000.0, result, 0.001);
    }

    @Test
    public void getTotalSpending_returnsZeroWhenDaoReturnsNull() {
        long start = 1000L;
        long end = 2000L;
        when(expenseDao.getTotalSpending(start, end)).thenReturn(null);

        double result = analyticsRepository.getTotalSpending(start, end);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void getSpendingByCategory_returnsListFromDao() {
        long start = 1000L;
        long end = 2000L;
        List<ExpenseDao.CategorySpending> spending = new ArrayList<>();
        when(expenseDao.getSpendingByCategory(start, end)).thenReturn(spending);

        List<ExpenseDao.CategorySpending> result = analyticsRepository.getSpendingByCategory(start, end);

        assertEquals(spending, result);
    }

    @Test
    public void getDailySpendingTrend_returnsListFromDao() {
        long start = 1000L;
        long end = 2000L;
        List<ExpenseDao.DailySpending> trend = new ArrayList<>();
        when(expenseDao.getDailySpending(start, end)).thenReturn(trend);

        List<ExpenseDao.DailySpending> result = analyticsRepository.getDailySpendingTrend(start, end);

        assertEquals(trend, result);
    }

    @Test
    public void getBudgetUsagePercentage_returnsZeroWhenNoBudgets() {
        long start = 1000L;
        long end = 2000L;
        when(budgetDao.getBudgetsInInterval(start, end)).thenReturn(Collections.emptyList());

        double result = analyticsRepository.getBudgetUsagePercentage(start, end);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void getBudgetUsagePercentage_returnsZeroWhenTotalLimitIsZero() {
        long start = 1000L;
        long end = 2000L;
        Budget budget = new Budget();
        budget.limitInCents = 0;
        when(budgetDao.getBudgetsInInterval(start, end)).thenReturn(Collections.singletonList(budget));

        double result = analyticsRepository.getBudgetUsagePercentage(start, end);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void getBudgetUsagePercentage_calculatesCorrectPercentage() {
        long start = 1000L;
        long end = 2000L;
        
        Budget b1 = new Budget();
        b1.limitInCents = 10000; // $100
        Budget b2 = new Budget();
        b2.limitInCents = 10000; // $100
        
        when(budgetDao.getBudgetsInInterval(start, end)).thenReturn(Arrays.asList(b1, b2));
        when(expenseDao.getTotalSpending(start, end)).thenReturn(5000L); // $50

        // Total limit = 20000, Total spent = 5000 -> 25%
        double result = analyticsRepository.getBudgetUsagePercentage(start, end);

        assertEquals(25.0, result, 0.001);
    }

    @Test
    public void getTopSpendingCategory_returnsMinusOneWhenNoSpending() {
        long start = 1000L;
        long end = 2000L;
        when(expenseDao.getSpendingByCategory(start, end)).thenReturn(Collections.emptyList());

        int result = analyticsRepository.getTopSpendingCategory(start, end);

        assertEquals(-1, result);
    }

    @Test
    public void getTopSpendingCategory_returnsCategoryWithMaxSpending() {
        long start = 1000L;
        long end = 2000L;
        
        ExpenseDao.CategorySpending s1 = new ExpenseDao.CategorySpending();
        s1.categoryId = 1;
        s1.totalInCents = 1000;

        ExpenseDao.CategorySpending s2 = new ExpenseDao.CategorySpending();
        s2.categoryId = 2;
        s2.totalInCents = 5000;

        ExpenseDao.CategorySpending s3 = new ExpenseDao.CategorySpending();
        s3.categoryId = 3;
        s3.totalInCents = 2000;

        when(expenseDao.getSpendingByCategory(start, end)).thenReturn(Arrays.asList(s1, s2, s3));

        int result = analyticsRepository.getTopSpendingCategory(start, end);

        assertEquals(2, result);
    }
}
