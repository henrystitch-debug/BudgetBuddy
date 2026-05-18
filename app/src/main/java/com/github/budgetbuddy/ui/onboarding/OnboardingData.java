package com.github.budgetbuddy.ui.onboarding;

import com.github.budgetbuddy.database.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class OnboardingData {
    public String name = "";
    public String currency = "";
    public List<String> selectedCategories = new ArrayList<>();
    public List<Category> newCategories = new ArrayList<>();
    public boolean notifsEnabled = false;
}
