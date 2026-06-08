package com.github.budgetbuddy.database.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.budgetbuddy.database.dao.ExpenseDao;
import com.github.budgetbuddy.database.entity.Expense;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepositoryTest {

    @Mock
    private ExpenseDao expenseDao;

    private ExpenseRepository expenseRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        expenseRepository = new ExpenseRepository(expenseDao);
    }

    @Test
    public void insert_callsDaoInsert() throws InterruptedException {
        Expense expense = new Expense();
        expense.entryDateStartInMilliSec = 1698336000000L;

        expenseRepository.insert(expense);

        Thread.sleep(100);

        verify(expenseDao).insert(expense);
    }

    @Test
    public void updateExpense_callsDaoUpdateExpenseWithNormalizedDate() {
        int id = 1;
        long amount = 5000L;
        int categoryId = 2;
        long entryDate = 1698336000000L;
        String note = "Lunch";
        String repeat = "None";
        Integer budgetId = 3;

        expenseRepository.updateExpense(id, amount, categoryId, entryDate, note, repeat, budgetId);

        verify(expenseDao).updateExpense(id, amount, categoryId,
                com.github.budgetbuddy.utils.TimeUtils.toStartOfDay(entryDate),
                note, repeat, budgetId);
    }

    @Test
    public void getExpenseById_callsDaoGetExpenseById() {
        int id = 99;
        Expense mockExpense = new Expense();
        when(expenseDao.getExpenseById(id)).thenReturn(mockExpense);

        Expense result = expenseRepository.getExpenseById(id);

        assertEquals(mockExpense, result);
        verify(expenseDao).getExpenseById(id);
    }

    @Test
    public void getExpensesOfSpecificDate_callsDaoWithNormalizedDate() {
        long date = 1698336000000L;
        List<Expense> mockList = new ArrayList<>();
        long normalizedDate = com.github.budgetbuddy.utils.TimeUtils.toStartOfDay(date);

        when(expenseDao.getExpensesOfSpecificDate(normalizedDate)).thenReturn(mockList);

        List<Expense> result = expenseRepository.getExpensesOfSpecificDate(date);

        assertEquals(mockList, result);
        verify(expenseDao).getExpensesOfSpecificDate(normalizedDate);
    }

    @Test
    public void getExpensesInterval_callsDao() {
        long start = 1000L;
        long end = 2000L;
        List<Expense> mockList = new ArrayList<>();
        when(expenseDao.getExpensesInterval(start, end)).thenReturn(mockList);

        List<Expense> result = expenseRepository.getExpensesInterval(start, end);

        assertEquals(mockList, result);
        verify(expenseDao).getExpensesInterval(start, end);
    }

    @Test
    public void getExpensesByCategory_callsDao() {
        int catId = 5;
        List<Expense> mockList = new ArrayList<>();
        when(expenseDao.getExpensesByCategory(catId)).thenReturn(mockList);

        List<Expense> result = expenseRepository.getExpensesByCategory(catId);

        assertEquals(mockList, result);
        verify(expenseDao).getExpensesByCategory(catId);
    }

    @Test
    public void getExpensesByCategoryAndInterval_callsDao() {
        int catId = 5;
        long start = 1000L;
        long end = 2000L;
        List<Expense> mockList = new ArrayList<>();
        when(expenseDao.getExpensesByCategoryAndInterval(catId, start, end)).thenReturn(mockList);

        List<Expense> result = expenseRepository.getExpensesByCategoryAndInterval(catId, start, end);

        assertEquals(mockList, result);
        verify(expenseDao).getExpensesByCategoryAndInterval(catId, start, end);
    }

    @Test
    public void deleteExpense_callsDaoDeleteExpense() {
        Expense expense = new Expense();
        expenseRepository.deleteExpense(expense);
        verify(expenseDao).deleteExpense(expense);
    }

    @Test
    public void getTotalSpentForCategoryAndInterval_callsDao() {
        int catId = 3;
        long start = 1000L;
        long end = 2000L;
        long expectedTotal = 15000L;

        when(expenseDao.getTotalForCategoryAndInterval(catId, start, end)).thenReturn(expectedTotal);

        long result = expenseRepository.getTotalSpentForCategoryAndInterval(catId, start, end);

        assertEquals(expectedTotal, result);
        verify(expenseDao).getTotalForCategoryAndInterval(catId, start, end);
    }

    @Test
    public void getTotalSpentForBudget_returnsZeroWhenDaoReturnsNull() {
        int budgetId = 1;
        long start = 1000L;
        long end = 2000L;

        when(expenseDao.getTotalSpentForBudget(budgetId, start, end)).thenReturn(null);

        long result = expenseRepository.getTotalSpentForBudget(budgetId, start, end);

        assertEquals(0L, result);
        verify(expenseDao).getTotalSpentForBudget(budgetId, start, end);
    }

    @Test
    public void getTotalSpentForBudget_returnsValueWhenDaoReturnsValue() {
        int budgetId = 1;
        long start = 1000L;
        long end = 2000L;
        long expectedTotal = 4500L;

        when(expenseDao.getTotalSpentForBudget(budgetId, start, end)).thenReturn(expectedTotal);

        long result = expenseRepository.getTotalSpentForBudget(budgetId, start, end);

        assertEquals(expectedTotal, result);
        verify(expenseDao).getTotalSpentForBudget(budgetId, start, end);
    }

    @Test
    public void getRecentExpenses_callsDaoWithNormalizedDates() {
        long start = 1000L;
        long end = 2000L;
        int limit = 10;
        List<Expense> mockList = new ArrayList<>();

        long normStart = com.github.budgetbuddy.utils.TimeUtils.toStartOfDay(start);
        long normEnd = com.github.budgetbuddy.utils.TimeUtils.toEndOfDay(end);

        when(expenseDao.getExpensesIntervalUnderLimit(normStart, normEnd, limit)).thenReturn(mockList);

        List<Expense> result = expenseRepository.getRecentExpenses(start, end, limit);

        assertEquals(mockList, result);
        verify(expenseDao).getExpensesIntervalUnderLimit(normStart, normEnd, limit);
    }
}