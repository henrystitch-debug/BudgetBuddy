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

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Streak;
import com.github.budgetbuddy.utils.ColorUtils;
import com.github.budgetbuddy.utils.TimeUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewFragment extends Fragment {

    private long currentStartDate;
    private long currentEndDate;
    private String currentCurrency = "€";

    private TextView tvGreeting, tvSubtitle;
    private TextView tvMonth, tabThisMonth, tabLastMonth, tabTwoWeeks;
    private PieChart pieChart;
    private LinearLayout legendContainer;
    private LinearLayout budgetProgressContainer;
    private RecyclerView rvExpenses;
    private TextView tvSeeAllExpenses;
    private ExpenseAdapter expenseAdapter;

    private static final int COLLAPSED_COUNT = 3;
    private boolean expensesExpanded = false;
    private List<Expense> currentExpenses = new ArrayList<>();

    // ── ADDED: cache loaded from DB once per data refresh ──────────────────
    private Map<Integer, Category> categoryMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting              = view.findViewById(R.id.tv_greeting);
        tvSubtitle              = view.findViewById(R.id.tv_subtitle);
        tvMonth                 = view.findViewById(R.id.tv_month);
        tabThisMonth            = view.findViewById(R.id.tab_this_month);
        tabLastMonth            = view.findViewById(R.id.tab_last_month);
        tabTwoWeeks             = view.findViewById(R.id.tab_two_weeks);
        pieChart                = view.findViewById(R.id.pie_chart);
        legendContainer         = view.findViewById(R.id.legend_container);
        budgetProgressContainer = view.findViewById(R.id.budget_progress_container);
        rvExpenses              = view.findViewById(R.id.rv_expenses);
        tvSeeAllExpenses        = view.findViewById(R.id.tv_see_all_expenses);

        expenseAdapter = new ExpenseAdapter(requireContext(), new ArrayList<>(),
                categoryMap,
                expenseId -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showAddExpenseForEdit(expenseId);
            }
        });
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExpenses.setAdapter(expenseAdapter);

        tvSeeAllExpenses.setOnClickListener(v -> {
            expensesExpanded = !expensesExpanded;
            renderExpenses();
        });

        tabThisMonth.setOnClickListener(v -> { setThisMonth(); updateTabStyles(0); });
        tabLastMonth.setOnClickListener(v -> { setLastMonth(); updateTabStyles(1); });
        tabTwoWeeks.setOnClickListener(v  -> { setTwoWeeks();  updateTabStyles(2); });

        loadGreetingAndCurrency();
    }

    private void loadGreetingAndCurrency() {
        SettingsManager sm = ((BudgetBuddyApp) requireActivity()
                .getApplication()).getSettingsManager();
        String name = sm.getUserName();
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            final String currency = sm.getCurrency();
            Streak streak = db.streakDao().getCurrentStreak();
            final int streakCount = streak != null ? streak.counter : 0;

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                currentCurrency = currency;
                String s = "Hey, " + (name != null ? name : "there") + "!";
                tvGreeting.setText(s);
                if (streakCount > 0) {
                    s = "🔥 " + streakCount + "-day streak — keep it up!";
                    tvSubtitle.setText(s);
                } else {
                    tvSubtitle.setText(R.string.log_xpense);
                }
                setThisMonth();
            });
        });
    }

    private void setThisMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(0);
        currentEndDate   = TimeUtils.getEndOfMonth(0);
        tvMonth.setText(TimeUtils.getMonthLabel(0));
        updateTabStyles(0);
        loadDetailData();
    }

    private void setLastMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(1);
        currentEndDate   = TimeUtils.getEndOfMonth(1);
        tvMonth.setText(TimeUtils.getMonthLabel(1));
        updateTabStyles(1);
        loadDetailData();
    }

    private void setTwoWeeks() {
        currentStartDate = TimeUtils.getStartOfTwoWeeksAgo();
        currentEndDate   = TimeUtils.getNow();
        tvMonth.setText(R.string.last_2wks);
        updateTabStyles(2);
        loadDetailData();
    }

    private void updateTabStyles(int selected) {
        tabThisMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabThisMonth.setTextColor(Color.parseColor("#888888"));
        tabLastMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabLastMonth.setTextColor(Color.parseColor("#888888"));
        tabTwoWeeks.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabTwoWeeks.setTextColor(Color.parseColor("#888888"));

        TextView selectedTab = selected == 0 ? tabThisMonth : selected == 1 ? tabLastMonth : tabTwoWeeks;
        selectedTab.setBackgroundColor(ColorUtils.FOOD);
        selectedTab.setTextColor(Color.WHITE);
    }

    private void loadDetailData() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        long startDate  = currentStartDate;
        long endDate    = currentEndDate;

        AppDatabase.databaseWriteExecutor.execute(() -> {

            // ── ADDED: load all categories and build lookup map ────────────
            List<Category> allCategories = db.categoryDao().getAllCategories();
            Map<Integer, Category> catMap = new HashMap<>();
            for (Category c : allCategories) catMap.put(c.id, c);

            List<Expense> allExpenses = db.expenseDao().getExpensesInterval(startDate, endDate);

            // ── CHANGED: accumulate totals by actual DB id, not magic 1–12 ─
            Map<Integer, Long> categoryTotals = new HashMap<>();
            for (Expense e : allExpenses) {
                categoryTotals.merge(e.categoryId, e.amountInCents, Long::sum);
            }

            long totalSpent = 0;
            for (long v : categoryTotals.values()) totalSpent += v;
            final long finalTotalSpent = totalSpent;

            List<Expense> recent = db.expenseDao().getRecentExpenses(startDate, endDate, 1000);

            if (!isAdded()) return;

            // ── CHANGED: capture finals for lambda ─────────────────────────
            final Map<Integer, Category> finalCatMap     = catMap;
            final Map<Integer, Long>   finalCatTotals  = categoryTotals;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                // ── CHANGED: store map on fragment, pass to render methods ──
                categoryMap = finalCatMap;
                updatePieChart(finalCatTotals, finalTotalSpent);
                updateBudgetProgress(new ArrayList<>(), finalCatTotals);
                // ────────────────────────────────────────────────────────────
                currentExpenses  = recent;
                expensesExpanded = false;
                renderExpenses();
            });
        });
    }

    private void renderExpenses() {
        int total = currentExpenses.size();
        List<Expense> toShow;
        if (expensesExpanded || total <= COLLAPSED_COUNT) {
            toShow = currentExpenses;
        } else {
            toShow = currentExpenses.subList(0, COLLAPSED_COUNT);
        }
        expenseAdapter.updateExpenses(toShow, categoryMap);

        if (total <= COLLAPSED_COUNT) {
            tvSeeAllExpenses.setVisibility(View.GONE);
        } else {
            tvSeeAllExpenses.setVisibility(View.VISIBLE);
            tvSeeAllExpenses.setText(expensesExpanded
                    ? "Show less"
                    : "See all (" + total + ")");
        }
    }

    private void updatePieChart(Map<Integer, Long> categoryTotals, long totalSpent) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer>  colors  = new ArrayList<>();

        for (Map.Entry<Integer, Long> entry : categoryTotals.entrySet()) {
            if (entry.getValue() <= 0) continue;
            Category cat = categoryMap.get(entry.getKey());
            if (cat == null) continue;                         // unknown id, skip
            entries.add(new PieEntry(entry.getValue() / 100f, cat.name));
            colors.add(Color.parseColor(cat.color));
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
        String s = String.format("%s %.0f\ntotal spent", currentCurrency, totalSpent / 100f);
        pieChart.setCenterText(s);
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#1A1A1A"));
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.invalidate();

        legendContainer.removeAllViews();
        final int itemsPerRow = 3;
        LinearLayout currentRow = null;
        for (int i = 0; i < entries.size(); i++) {
            if (i % itemsPerRow == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) rowParams.topMargin = dpToPx(6);
                currentRow.setLayoutParams(rowParams);
                legendContainer.addView(currentRow);
            }

            PieEntry entry  = entries.get(i);
            int      color  = colors.get(i);
            double   pct    = totalSpent > 0 ? (entry.getValue() / totalSpent) * 100 : 0;

            LinearLayout legendItem = new LinearLayout(getContext());
            legendItem.setOrientation(LinearLayout.HORIZONTAL);
            legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
            legendItem.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            View dot = new View(getContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            dotParams.setMargins(0, 0, dpToPx(4), 0);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundColor(color);

            TextView label = new TextView(getContext());
            label.setText(String.format("%s %.0f%%", entry.getLabel(), pct));
            label.setTextSize(10f);
            label.setTextColor(Color.parseColor("#444444"));
            label.setMaxLines(1);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);

            legendItem.addView(dot);
            legendItem.addView(label);
            currentRow.addView(legendItem);
        }
        int leftover = entries.size() % itemsPerRow;
        if (leftover != 0 && currentRow != null) {
            for (int i = 0; i < itemsPerRow - leftover; i++) {
                View spacer = new View(getContext());
                spacer.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                currentRow.addView(spacer);
            }
        }
    }

    // ── CHANGED: signature now takes Map<Integer,Double> instead of double[] ─
    private void updateBudgetProgress(List<int[]> rows, Map<Integer, Long> categoryTotals) {
        budgetProgressContainer.removeAllViews();

        if (rows.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No budgets set for this period.");
            empty.setTextSize(13f);
            empty.setTextColor(Color.parseColor("#888888"));
            budgetProgressContainer.addView(empty);
            return;
        }

        for (int idx = 0; idx < rows.size(); idx++) {
            int[]  row        = rows.get(idx);
            int    categoryId = row[0];
            int    limit      = row[1];
            Long spentInCents      = categoryTotals.getOrDefault(categoryId, 0L);
            addCategoryProgressRow(categoryId, limit, spentInCents, idx > 0);
        }
    }
    // ────────────────────────────────────────────────────────────────────────

    private void addCategoryProgressRow(int categoryId, int limit, Long spent, boolean addTopMargin) {
        boolean exceeded   = spent > limit;
        int     pct        = limit > 0 ? (int) ((spent / limit) * 100) : 0;
        int     displayPct = Math.min(pct, 100);

        // ── CHANGED: look up Category from map instead of CategoryUtils ────
        Category cat      = categoryMap.get(categoryId);
        String   catLabel = cat != null ? cat.icon + "  " + cat.name : "? Unknown";
        // ──────────────────────────────────────────────────────────────────

        LinearLayout block = new LinearLayout(getContext());
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (addTopMargin) blockParams.topMargin = dpToPx(12);
        block.setLayoutParams(blockParams);

        LinearLayout labelRow = new LinearLayout(getContext());
        labelRow.setOrientation(LinearLayout.HORIZONTAL);
        labelRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        labelRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView nameView = new TextView(getContext());
        nameView.setText(catLabel);                            // ── CHANGED
        nameView.setTextSize(13f);
        nameView.setTextColor(Color.parseColor("#1A1A1A"));
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        double spentDisplay = spent / 100.0;
        double limitDisplay = limit / 100.0;
        String s = String.format("%s %.2f / %s %.2f", currentCurrency, spentDisplay,
                currentCurrency, limitDisplay);
        TextView amtView = new TextView(getContext());
        amtView.setText(s);
        amtView.setTextSize(11f);
        amtView.setTextColor(exceeded ? Color.parseColor("#E53935") : Color.parseColor("#888888"));

        labelRow.addView(nameView);
        labelRow.addView(amtView);
        block.addView(labelRow);

        if (exceeded) {
            TextView over = new TextView(getContext());
            over.setText(String.format("Over budget by %s %.0f (%d%%)", currentCurrency, spent - limit, pct));
            over.setTextSize(11f);
            over.setTextColor(Color.parseColor("#E53935"));
            LinearLayout.LayoutParams overParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            overParams.topMargin = dpToPx(2);
            over.setLayoutParams(overParams);
            block.addView(over);
        }

        ProgressBar progressBar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(10));
        pbParams.setMargins(0, dpToPx(6), 0, 0);
        progressBar.setLayoutParams(pbParams);
        progressBar.setMax(100);
        progressBar.setProgress(displayPct);

        int progressColor = exceeded || pct >= 95 ? Color.parseColor("#E53935")
                : pct >= 80 ? Color.parseColor("#FFA726")
                : Color.parseColor("#4A7C7C");
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(progressColor));

        block.addView(progressBar);
        budgetProgressContainer.addView(block);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}