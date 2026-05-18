package com.github.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.ui.budget.CreateBudgetFragment;
import com.github.budgetbuddy.ui.onboarding.OnboardingActivity;
import com.github.budgetbuddy.ui.overview.OverviewFragment;
import com.github.budgetbuddy.ui.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    private OverviewFragment overviewFragment;
    private CreateBudgetFragment budgetFragment;
    private SettingsFragment settingsFragment;

    SettingsManager settingsManager;
    BottomNavigationView bottomNav;
    FloatingActionButton fabAddExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = ((BudgetBuddyApp) getApplication()).getSettingsManager();

        // first-launch
        if (settingsManager.getUserName() == null) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottom_navigation);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        // create fragments once
        overviewFragment = new OverviewFragment();
        budgetFragment = new CreateBudgetFragment();
        settingsFragment = new SettingsFragment();

        loadFragment(overviewFragment);
        bottomNav.setSelectedItemId(R.id.nav_overview);

        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_overview) {
                selected = overviewFragment;
            } else if (id == R.id.nav_budget) {
                selected = budgetFragment;
            } else if (id == R.id.nav_settings) {
                selected = settingsFragment;
            } else {
                return false;
            }
            bottomNav.getMenu().setGroupCheckable(0, true, true);
            loadFragment(selected);
            fabAddExpense.show();
            return true;
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToOverview() {
        bottomNav.setSelectedItemId(R.id.nav_overview);
    }

    public void showAddExpenseForEdit(int expenseId) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("expenseId", expenseId);

        startActivity(intent);

        fabAddExpense.hide();
        bottomNav.getMenu().setGroupCheckable(0, false, true);
    }
}