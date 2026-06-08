package com.github.budgetbuddy.database.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.entity.Category;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryTest {

    @Mock
    private CategoryDao categoryDao;

    private CategoryRepository categoryRepository;

    @Before
    public void setUp() {
        // 初始化 Mockito
        MockitoAnnotations.openMocks(this);
        categoryRepository = new CategoryRepository(categoryDao);
    }

    @Test
    public void getAllCategories_callsDaoGetCategories() {
        List<Category> mockList = new ArrayList<>();
        mockList.add(new Category());

        when(categoryDao.getCategories()).thenReturn(mockList);

        List<Category> result = categoryRepository.getAllCategories();

        assertEquals(mockList, result);
        verify(categoryDao).getCategories();
    }

    @Test
    public void getCategoryById_callsDaoGetCategoryById() {

        int categoryId = 42;
        Category mockCategory = new Category();

        when(categoryDao.getCategoryById(categoryId)).thenReturn(mockCategory);

        Category result = categoryRepository.getCategoryById(categoryId);

        assertEquals(mockCategory, result);
        verify(categoryDao).getCategoryById(categoryId);
    }
}