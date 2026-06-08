package com.github.budgetbuddy.ui.onboarding;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.dao.CategoryDao;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.DBConstants;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private LinearLayout dotsContainer;
    private OnboardingPagerAdapter adapter;

    public final OnboardingData data = new OnboardingData();
    public List<Category> finalCategories = new ArrayList<>();
    public List<Category> defaultCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_trial);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btn_next);
        dotsContainer = findViewById(R.id.dots_container);

        adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // nav via button only

        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                btnNext.setText(position == adapter.getItemCount() - 1 ?
                        R.string.letsgo : R.string.contd);
            }
        });

        btnNext.setOnClickListener(v -> advance());
    }

    private void advance() {
        int current = viewPager.getCurrentItem();
        // validate current step before advancing
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("f" + current);
        if (fragment instanceof OnboardingAdvancer) {
            if (!((OnboardingAdvancer) fragment).validate()) return;
            ((OnboardingAdvancer) fragment).save(data);
        }

        if (current < adapter.getItemCount() - 1) {
            viewPager.setCurrentItem(current + 1);
        } else {
            finishOnboarding();
        }
    }

    private void finishOnboarding() {
        SettingsManager settings = ((BudgetBuddyApp) getApplication()).getSettingsManager();
        settings.setUserName(data.name);
        settings.setCurrency(data.currency);
        settings.setNotifsEnabled(data.notifsEnabled);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (Object[] data : DBConstants.DEFAULT_CATEGORIES) {
                Category cat = new Category();
                cat.name  = (String) data[0];
                cat.icon = (String) data[1];
                cat.color = "";
                defaultCategories.add(cat);
            }

            // FIXME: unselected categories should never be removed from the database.
            // we should use a boolean field to hide the unselected categories.
            defaultCategories.removeIf(cat -> !data.selectedCategories.contains(cat.name));

            // insert selected categories
            int startColor = data.newCategories.toArray().length;
            int rounds = 0;
            for(Category cat : defaultCategories){
                cat.color = DBConstants.HEX_COLORS[startColor + rounds];
                finalCategories.add(cat);
                rounds++;
            }
            // NOTE: add Default Category first so we it is assigned ID 1
            finalCategories.add(new Category(Category.DEFAULT_NAME, Category.DEFAULT_ICON, ""));
            finalCategories.addAll(data.newCategories);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                CategoryDao dao = AppDatabase.getDatabase(this).categoryDao();
                for(Category cat : finalCategories) {
                    dao.insertCategory(cat);
                }
            });
        });

        Toast.makeText(this, "Hi " + data.name + "! 👋", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupDots(int activeIndex) {
        dotsContainer.removeAllViews();
        int count = adapter.getItemCount();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int size = i == activeIndex ? 24 : 16;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(size), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(dpToPx(4));
            shape.setColor(i == activeIndex ? 0xFF4A7C7C : 0xFFCCCCCC);
            dot.setBackground(shape);
            dotsContainer.addView(dot);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}

