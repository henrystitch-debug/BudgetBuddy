package com.github.budgetbuddy.ui.overview;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Streak;
import com.github.budgetbuddy.utils.CategoryUtils;
import com.github.budgetbuddy.utils.TimeUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {

    private long currentStartDate;
    private long currentEndDate;

    private TextView tvMonth;
    private TextView tabThisMonth, tabLastMonth, tabTwoWeeks;
    private View cardStreak;
    private TextView tvStreakCount, tvStreakX;
    private PieChart pieChart;
    private LinearLayout legendContainer;
    private LinearLayout budgetProgressContainer;
    private RecyclerView rvExpenses;
    private ExpenseAdapter expenseAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        tvMonth = view.findViewById(R.id.tv_month);
        tabThisMonth = view.findViewById(R.id.tab_this_month);
        tabLastMonth = view.findViewById(R.id.tab_last_month);
        tabTwoWeeks = view.findViewById(R.id.tab_two_weeks);
        cardStreak = view.findViewById(R.id.card_streak);
        tvStreakCount = view.findViewById(R.id.tv_streak_count);
        tvStreakX = view.findViewById(R.id.tv_streak_x);
        pieChart = view.findViewById(R.id.pie_chart);
        legendContainer = view.findViewById(R.id.legend_container);
        budgetProgressContainer = view.findViewById(R.id.budget_progress_container);
        rvExpenses = view.findViewById(R.id.rv_expenses);

        // Setup RecyclerView
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), expenseId -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showAddExpenseForEdit(expenseId);
            }
        });
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExpenses.setAdapter(expenseAdapter);

        // Default: this month
        setThisMonth();

        // Tab click listeners
        tabThisMonth.setOnClickListener(v -> {
            setThisMonth();
            updateTabStyles(0);
        });
        tabLastMonth.setOnClickListener(v -> {
            setLastMonth();
            updateTabStyles(1);
        });
        tabTwoWeeks.setOnClickListener(v -> {
            setTwoWeeks();
            updateTabStyles(2);
        });

        return view;
    }

    private void setThisMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(0);
        currentEndDate = TimeUtils.getEndOfMonth(0);
        tvMonth.setText(TimeUtils.getMonthLabel(0));
        updateTabStyles(0);
        loadData();
    }

    private void setLastMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(1);
        currentEndDate = TimeUtils.getEndOfMonth(1);
        tvMonth.setText(TimeUtils.getMonthLabel(1));
        updateTabStyles(1);
        loadData();
    }

    private void setTwoWeeks() {
        currentStartDate = TimeUtils.getStartOfTwoWeeksAgo();
        currentEndDate = TimeUtils.getNow();
        tvMonth.setText("Last 2 Weeks");
        updateTabStyles(2);
        loadData();
    }

    private void updateTabStyles(int selected) {
        // Reset all
        tabThisMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabThisMonth.setTextColor(Color.parseColor("#888888"));
        tabLastMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabLastMonth.setTextColor(Color.parseColor("#888888"));
        tabTwoWeeks.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabTwoWeeks.setTextColor(Color.parseColor("#888888"));

        // Highlight selected
        TextView selectedTab;
        if (selected == 0) selectedTab = tabThisMonth;
        else if (selected == 1) selectedTab = tabLastMonth;
        else selectedTab = tabTwoWeeks;

        selectedTab.setBackgroundColor(Color.parseColor("#4A7C7C"));
        selectedTab.setTextColor(Color.WHITE);
    }

    private void loadData() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        long startDate = currentStartDate;
        long endDate = currentEndDate;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Load streak
            Streak streak = db.streakDao().getCurrentStreak();

            // Load all categories
            List<Category> categories = db.categoryDao().getAllCategories();

            // Load recent expenses (up to 10)
            List<Expense> recentExpenses = db.expenseDao().getRecentExpenses(startDate, endDate, 10);

            // Load all expenses for pie chart
            List<Expense> allExpenses = db.expenseDao().getExpensesInterval(startDate, endDate);

            // Compute spending per category for pie chart
            double[] categoryTotals = new double[9]; // index 1..8
            for (Expense e : allExpenses) {
                if (e.categoryId >= 1 && e.categoryId <= 8) {
                    categoryTotals[e.categoryId] += e.amount;
                }
            }
            double totalSpent = 0;
            for (int i = 1; i <= 8; i++) {
                totalSpent += categoryTotals[i];
            }
            final double finalTotalSpent = totalSpent;

            // Load budgets for categories with budgetId > 0
            // For each category with a budget, get spent amount
            List<BudgetProgressItem> progressItems = new ArrayList<>();
            for (Category cat : categories) {
                if (cat.budgetId > 0) {
                    Budget budget = db.budgetDao().getBudgetById(cat.budgetId);
                    if (budget != null) {
                        double spent = db.expenseDao().getTotalForCategoryAndInterval(cat.id, startDate, endDate);
                        progressItems.add(new BudgetProgressItem(cat.name, spent, budget.limit));
                    }
                }
            }

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                // Update streak card
                if (streak != null && streak.counter > 0) {
                    cardStreak.setVisibility(View.VISIBLE);
                    tvStreakCount.setText(streak.counter + " month streak!");
                    tvStreakX.setText("x" + streak.counter);
                } else {
                    cardStreak.setVisibility(View.GONE);
                }

                // Update pie chart
                updatePieChart(categoryTotals, finalTotalSpent);

                // Update budget progress
                updateBudgetProgress(progressItems);

                // Update recent expenses list
                expenseAdapter.updateExpenses(recentExpenses);
            });
        });
    }

    private void updatePieChart(double[] categoryTotals, double totalSpent) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            if (categoryTotals[i] > 0) {
                entries.add(new PieEntry((float) categoryTotals[i], CategoryUtils.getName(i)));
                colors.add(CategoryUtils.getColor(i));
            }
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            legendContainer.setVisibility(View.GONE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        legendContainer.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText(String.format("€ %.0f\ntotal spent", totalSpent));
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#1A1A1A"));
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.invalidate();

        // Populate legend container
        legendContainer.removeAllViews();
        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            int color = colors.get(i);
            double pct = totalSpent > 0 ? (entry.getValue() / totalSpent) * 100 : 0;

            LinearLayout legendItem = new LinearLayout(getContext());
            legendItem.setOrientation(LinearLayout.HORIZONTAL);
            legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, 0, dpToPx(12), 0);
            legendItem.setLayoutParams(itemParams);

            View dot = new View(getContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
            dotParams.setMargins(0, 0, dpToPx(4), 0);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundColor(color);

            TextView label = new TextView(getContext());
            label.setText(String.format("%s %.0f%%", entry.getLabel(), pct));
            label.setTextSize(11f);
            label.setTextColor(Color.parseColor("#444444"));

            legendItem.addView(dot);
            legendItem.addView(label);
            legendContainer.addView(legendItem);
        }
    }

    private void updateBudgetProgress(List<BudgetProgressItem> items) {
        budgetProgressContainer.removeAllViews();

        if (items.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No budgets set. Add a budget to track spending.");
            empty.setTextSize(13f);
            empty.setTextColor(Color.parseColor("#888888"));
            budgetProgressContainer.addView(empty);
            return;
        }

        for (BudgetProgressItem item : items) {
            // Category name + amount label
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView nameView = new TextView(getContext());
            nameView.setText(item.name);
            nameView.setTextSize(13f);
            nameView.setTextColor(Color.parseColor("#444444"));
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameView.setLayoutParams(nameParams);

            TextView amtView = new TextView(getContext());
            amtView.setText(String.format("€ %.0f / € %d", item.spent, item.limit));
            amtView.setTextSize(11f);
            amtView.setTextColor(Color.parseColor("#888888"));

            row.addView(nameView);
            row.addView(amtView);
            budgetProgressContainer.addView(row);

            // ProgressBar
            ProgressBar progressBar = new ProgressBar(getContext(), null,
                    android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(8)
            );
            pbParams.setMargins(0, dpToPx(4), 0, dpToPx(12));
            progressBar.setLayoutParams(pbParams);
            progressBar.setMax(100);

            int pct = item.limit > 0 ? (int) ((item.spent / item.limit) * 100) : 0;
            if (pct > 100) pct = 100;
            progressBar.setProgress(pct);

            int progressColor;
            if (pct >= 95) {
                progressColor = Color.parseColor("#E53935"); // red
            } else if (pct >= 80) {
                progressColor = Color.parseColor("#FFA726"); // orange
            } else {
                progressColor = Color.parseColor("#4A7C7C"); // green/teal
            }
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(progressColor));

            budgetProgressContainer.addView(progressBar);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class BudgetProgressItem {
        String name;
        double spent;
        int limit;

        BudgetProgressItem(String name, double spent, int limit) {
            this.name = name;
            this.spent = spent;
            this.limit = limit;
        }
    }
}
