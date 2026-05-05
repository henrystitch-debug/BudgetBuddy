package com.github.budgetbuddy;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.ui.budget.CreateBudgetFragment;
import com.github.budgetbuddy.ui.expense.AddExpenseFragment;
import com.github.budgetbuddy.ui.myfinances.MyFinancesFragment;
import com.github.budgetbuddy.ui.overview.OverviewFragment;
import com.github.budgetbuddy.ui.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fab_add_expense);

        // 預設選中 Home tab 並顯示 My Finances
        loadFragment(new MyFinancesFragment());
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new MyFinancesFragment();
            } else if (id == R.id.nav_overview) {
                selected = new OverviewFragment();
            } else if (id == R.id.nav_budget) {
                selected = new CreateBudgetFragment();
            } else if (id == R.id.nav_settings) {
                selected = new SettingsFragment();
            } else {
                return false;
            }

            loadFragment(selected);
            return true;
        });

        // FAB 開啟 Add Expense（隱藏底部導航）
        fab.setOnClickListener(v -> {
            bottomNav.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, AddExpenseFragment.newInstance(-1))
                    .addToBackStack(null)
                    .commit();
        });

        // 關閉 Add Expense 後恢復底部導航
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                bottomNav.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
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
        loadFragment(new OverviewFragment());
    }

    public void showAddExpenseForEdit(int expenseId) {
        bottomNav.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        AddExpenseFragment fragment = AddExpenseFragment.newInstance(expenseId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
