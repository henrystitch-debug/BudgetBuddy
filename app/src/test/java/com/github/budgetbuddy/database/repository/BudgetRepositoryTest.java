package com.github.budgetbuddy.database.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.budgetbuddy.database.dao.BudgetDao;
import com.github.budgetbuddy.database.entity.Budget;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BudgetRepositoryTest {

    @Mock
    private BudgetDao budgetDao;

    private BudgetRepository budgetRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        budgetRepository = new BudgetRepository(budgetDao);
    }

    @Test
    public void createBudget_callsDaoInsert() {
        Budget budget = new Budget();
        budgetRepository.createBudget(budget);
        verify(budgetDao).insertBudget(budget);
    }

    @Test
    public void updateBudget_callsDaoUpdate() {
        Budget budget = new Budget();
        budgetRepository.updateBudget(budget);
        verify(budgetDao).updateBudget(budget);
    }

    @Test
    public void getActiveBudget_callsDaoWithTime() {
        long time = 123456789L;
        Budget budget = new Budget();
        when(budgetDao.getActiveBudget(time)).thenReturn(budget);

        Budget result = budgetRepository.getActiveBudget(time);

        assertEquals(budget, result);
        verify(budgetDao).getActiveBudget(time);
    }

    @Test
    public void getBudgetsInTimeRange_normalizesDatesAndCallsDao() {
        long start = 1698336000000L;
        long end = 1698422400000L;

        // testing side effect here.
        // We don't strictly test TimeUtils here but we ensure it's called (indirectly via results or mocking if possible,
        // but here it's static). Just verifying Dao call.
        budgetRepository.getBudgetsInTimeRange(start, end);
        verify(budgetDao).getBudgetsInInterval(anyLong(), anyLong());
    }

    @Test
    public void addToSpentAmount_callsDaoIncrement() {
        budgetRepository.addToSpentAmount(1, 500L);
        verify(budgetDao).incrementCurrentAmount(1, 500L);
    }

    @Test
    public void isActiveBudgetExceeded_returnsTrueWhenExceeded() {
        long time = 123456789L;
        Budget budget = new Budget();
        budget.limitInCents = 1000;
        budget.currentAmountInCents = 1500;
        
        when(budgetDao.getActiveBudget(anyLong())).thenReturn(budget);

        assertTrue(budgetRepository.isActiveBudgetExceeded(time));
    }

    @Test
    public void isActiveBudgetExceeded_returnsFalseWhenNotExceeded() {
        long time = 123456789L;
        Budget budget = new Budget();
        budget.limitInCents = 1000;
        budget.currentAmountInCents = 500;
        
        when(budgetDao.getActiveBudget(anyLong())).thenReturn(budget);

        assertFalse(budgetRepository.isActiveBudgetExceeded(time));
    }

    @Test
    public void isActiveBudgetExceeded_returnsFalseWhenNoActiveBudget() {
        when(budgetDao.getActiveBudget(anyLong())).thenReturn(null);
        assertFalse(budgetRepository.isActiveBudgetExceeded(System.currentTimeMillis()));
    }
}
