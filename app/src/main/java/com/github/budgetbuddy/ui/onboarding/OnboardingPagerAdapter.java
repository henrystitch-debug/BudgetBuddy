package com.github.budgetbuddy.ui.onboarding;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.budgetbuddy.ui.onboarding.fragments.WelcomeFragment;
import com.github.budgetbuddy.ui.onboarding.fragments.NameFragment;
import com.github.budgetbuddy.ui.onboarding.fragments.CurrencyFragment;
import com.github.budgetbuddy.ui.onboarding.fragments.NotificationsFragment;
import com.github.budgetbuddy.ui.onboarding.fragments.CategoryFragment;

public class OnboardingPagerAdapter extends FragmentStateAdapter {
    public static final int PAGE_COUNT = 5;

    public OnboardingPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new WelcomeFragment();
            case 1: return new NameFragment();
            case 2: return new CurrencyFragment();
            case 3: return new CategoryFragment();
            case 4: return new NotificationsFragment();
            default: throw new IllegalArgumentException("Invalid position " + position);
        }
    }

    @Override
    public int getItemCount() { return PAGE_COUNT; }
}