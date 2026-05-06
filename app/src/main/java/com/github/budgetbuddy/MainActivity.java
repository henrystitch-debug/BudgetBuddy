package com.github.budgetbuddy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.ui.budget.CreateBudgetFragment;
import com.github.budgetbuddy.ui.expense.AddExpenseFragment;
import com.github.budgetbuddy.ui.overview.OverviewFragment;
import com.github.budgetbuddy.ui.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FloatingActionButton fabAddExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        loadFragment(new OverviewFragment());
        bottomNav.setSelectedItemId(R.id.nav_overview);

        fabAddExpense.setOnClickListener(v -> {
            loadFragment(AddExpenseFragment.newInstance(-1));
            fabAddExpense.hide();
            bottomNav.getMenu().setGroupCheckable(0, false, true);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                selected = new OverviewFragment();
            } else if (id == R.id.nav_budget) {
                selected = new CreateBudgetFragment();
            } else if (id == R.id.nav_settings) {
                selected = new SettingsFragment();
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
        loadFragment(AddExpenseFragment.newInstance(expenseId));
        fabAddExpense.hide();
        bottomNav.getMenu().setGroupCheckable(0, false, true);
    }
}
