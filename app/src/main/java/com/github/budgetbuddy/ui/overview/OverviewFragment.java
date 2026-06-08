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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.models.OverviewViewModel;
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
    private OverviewViewModel viewModel;

    // ── Date range ─────────────────────────────────────────────────────────
    private long currentStartDate;
    private long currentEndDate;

    // ── Last-known currency (kept in sync from LiveData) ───────────────────
    private String currentCurrency = DBConstants.DEFAULT_CURRENCY;

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView     tvGreeting, tvSubtitle;
    private TextView     tvMonth, tabThisMonth, tabLastMonth, tabTwoWeeks;
    private PieChart     pieChart;
    private LinearLayout legendContainer;
    private LinearLayout budgetProgressContainer;
    private RecyclerView rvExpenses;
    private TextView     tvSeeAllExpenses;

    // ── Adapter / expand state ─────────────────────────────────────────────
    private ExpenseAdapter        expenseAdapter;
    private static final int      COLLAPSED_COUNT  = 3;
    private boolean               expensesExpanded = false;
    private List<Expense>         currentExpenses  = new ArrayList<>();
    private Map<Integer, Category> categoryMap     = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
        bindViews(view);
        setupAdapter();
        setupTabListeners();
        observeViewModel();

        // One-time greeting / streak load
        viewModel.loadGreetingAndCurrency();
        // Default to this month on first creation
        setThisMonth();
    }

    /**
     * Reload data every time the fragment becomes visible — covers the
     * "user added an expense then navigated back" case.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (currentStartDate != 0) {
            viewModel.loadData(currentStartDate, currentEndDate);
        }
    }

    private void bindViews(View view) {
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
    }

    private void setupAdapter() {
        expenseAdapter = new ExpenseAdapter(
                requireContext(),
                new ArrayList<>(),
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
    }

    private void setupTabListeners() {
        tabThisMonth.setOnClickListener(v -> setThisMonth());
        tabLastMonth.setOnClickListener(v -> setLastMonth());
        tabTwoWeeks.setOnClickListener(v  -> setTwoWeeks());
    }

    // ── ViewModel observation ──────────────────────────────────────────────
    private void observeViewModel() {
        viewModel.greeting.observe(getViewLifecycleOwner(), text -> {
            if (text != null) tvGreeting.setText(text);
        });

        viewModel.subtitle.observe(getViewLifecycleOwner(), text -> {
            if (text != null) {
                tvSubtitle.setText(text);
            } else {
                tvSubtitle.setText(R.string.log_xpense);
            }
        });

        viewModel.currency.observe(getViewLifecycleOwner(), c -> {
            if (c != null) currentCurrency = c;
        });

        viewModel.overviewData.observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;

            currentCurrency = data.currency;
            categoryMap     = data.categoryMap;

            updatePieChart(data.categoryTotals, data.totalSpent);
            updateBudgetProgress(data.budgetProgressList);

            currentExpenses  = data.recentExpenses;
            expensesExpanded = false;
            renderExpenses();
        });
    }

    private void setThisMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(0);
        currentEndDate   = TimeUtils.getEndOfMonth(0);
        tvMonth.setText(TimeUtils.getMonthLabel(0));
        updateTabStyles(0);
        viewModel.loadData(currentStartDate, currentEndDate);
    }

    private void setLastMonth() {
        currentStartDate = TimeUtils.getStartOfMonth(1);
        currentEndDate   = TimeUtils.getEndOfMonth(1);
        tvMonth.setText(TimeUtils.getMonthLabel(1));
        updateTabStyles(1);
        viewModel.loadData(currentStartDate, currentEndDate);
    }

    private void setTwoWeeks() {
        currentStartDate = TimeUtils.getStartOfTwoWeeksAgo();
        currentEndDate   = TimeUtils.getNow();
        tvMonth.setText(R.string.last_2wks);
        updateTabStyles(2);
        viewModel.loadData(currentStartDate, currentEndDate);
    }

    private void updateTabStyles(int selected) {
        tabThisMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabThisMonth.setTextColor(Color.parseColor("#888888"));
        tabLastMonth.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabLastMonth.setTextColor(Color.parseColor("#888888"));
        tabTwoWeeks.setBackgroundColor(Color.parseColor("#F0F0F0"));
        tabTwoWeeks.setTextColor(Color.parseColor("#888888"));

        TextView selectedTab = selected == 0 ? tabThisMonth
                : selected == 1 ? tabLastMonth
                : tabTwoWeeks;
        selectedTab.setBackgroundColor(ColorUtils.FOOD);
        selectedTab.setTextColor(Color.WHITE);
    }

    // ── Render methods (UI-thread only, called from LiveData observers) ─────

    private void renderExpenses() {
        int total = currentExpenses.size();
        List<Expense> toShow = (expensesExpanded || total <= COLLAPSED_COUNT)
                ? currentExpenses
                : currentExpenses.subList(0, COLLAPSED_COUNT);

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
            if (cat == null) continue;
            // NOTE: entry.getValue() is converted back from cents to the Main currency unit
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
        pieChart.setCenterText(String.format("%s %.0f\ntotal spent", currentCurrency, totalSpent / 100f));
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

            PieEntry entry = entries.get(i);
            int      color = colors.get(i);
            // see line237: total spent should also be normalized to get the correct percentage
            double   pct   = totalSpent > 0 ? (entry.getValue() / (totalSpent/100.0)) * 100.0 : 0;

            LinearLayout legendItem = new LinearLayout(getContext());
            legendItem.setOrientation(LinearLayout.HORIZONTAL);
            legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
            legendItem.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            View dot = new View(getContext());
            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
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

        // Fill trailing empty cells so columns stay even
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

    /**
     * Renders budget progress bars from pre-resolved {@link OverviewViewModel.BudgetProgress}
     * objects. No category/budget lookups happen here — the ViewModel already did that work.
     */
    private void updateBudgetProgress(List<OverviewViewModel.BudgetProgress> progressList) {
        budgetProgressContainer.removeAllViews();

        if (progressList.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.no_budgets_set_for_this_period);
            empty.setTextSize(13f);
            empty.setTextColor(Color.parseColor("#888888"));
            budgetProgressContainer.addView(empty);
            return;
        }

        for (int idx = 0; idx < progressList.size(); idx++) {
            addBudgetProgressRow(progressList.get(idx), idx > 0);
        }
    }

    private void addBudgetProgressRow(OverviewViewModel.BudgetProgress bp, boolean addTopMargin) {
        long    spent    = bp.spentInCents;
        long    limit    = bp.limitInCents;
        boolean exceeded = spent > limit;
        int     pct      = limit > 0 ? (int) ((spent * 100L) / limit) : 0;
        int     displayPct = Math.min(pct, 100);

        LinearLayout block = new LinearLayout(getContext());
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (addTopMargin) blockParams.topMargin = dpToPx(12);
        block.setLayoutParams(blockParams);

        // ── Label row (category name left, amounts right) ──────────────────
        LinearLayout labelRow = new LinearLayout(getContext());
        labelRow.setOrientation(LinearLayout.HORIZONTAL);
        labelRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        labelRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView nameView = new TextView(getContext());
        nameView.setText(bp.categoryLabel);
        nameView.setTextSize(13f);
        nameView.setTextColor(Color.parseColor("#1A1A1A"));
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView amtView = new TextView(getContext());
        amtView.setText(String.format("%s %.2f / %s %.2f",
                currentCurrency, spent  / 100.0,
                currentCurrency, limit  / 100.0));
        amtView.setTextSize(11f);
        amtView.setTextColor(exceeded
                ? Color.parseColor("#E53935")
                : Color.parseColor("#888888"));

        labelRow.addView(nameView);
        labelRow.addView(amtView);
        block.addView(labelRow);

        // ── "Over budget" detail line ──────────────────────────────────────
        if (exceeded) {
            TextView over = new TextView(getContext());
            over.setText(String.format("Over budget by %s %.0f (%d%%)",
                    currentCurrency, (spent - limit) / 100.0, pct));
            over.setTextSize(11f);
            over.setTextColor(Color.parseColor("#E53935"));
            LinearLayout.LayoutParams overParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            overParams.topMargin = dpToPx(2);
            over.setLayoutParams(overParams);
            block.addView(over);
        }

        // ── Progress bar ───────────────────────────────────────────────────
        ProgressBar progressBar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(10));
        pbParams.setMargins(0, dpToPx(6), 0, 0);
        progressBar.setLayoutParams(pbParams);
        progressBar.setMax(100);
        progressBar.setProgress(displayPct);

        int progressColor = (exceeded || pct >= 95) ? Color.parseColor("#E53935")
                : pct >= 80               ? Color.parseColor("#FFA726")
                :                           Color.parseColor("#4A7C7C");
        progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(progressColor));

        block.addView(progressBar);
        budgetProgressContainer.addView(block);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
