package com.github.budgetbuddy.ui.onboarding;

public interface OnboardingAdvancer {
    boolean validate(); // if false do not advance to next onboarding step
    void save(OnboardingData data);
}
