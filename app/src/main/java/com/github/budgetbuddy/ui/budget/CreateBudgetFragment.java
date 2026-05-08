package com.github.budgetbuddy.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.BuildConfig;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.api.ClaudeApiHelper;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.utils.CategoryUtils;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.Objects;

public class CreateBudgetFragment extends Fragment {

    private int selectedCategoryId = -1;
    private int existingBudgetId = -1;
    private String currentCurrency = "€";

    private View cardBudgetInfo, cardAiResult, cardInput, btnAiRecommend, btnSaveBudget, labelSetBudget;
    private TextView tvSelectedCategory, tvCurrentLimit, tvSpent, tvAiRecommendation;
    private EditText etBudgetAmount;

    private final int[] categoryIds   = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private final int[] categoryViews = {
            R.id.cat_food, R.id.cat_home, R.id.cat_transport, R.id.cat_school,
            R.id.cat_health, R.id.cat_shopping, R.id.cat_fun, R.id.cat_other,
            R.id.cat_coffee, R.id.cat_travel, R.id.cat_gift, R.id.cat_pet
    };
    private View[] categoryContainers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardBudgetInfo    = view.findViewById(R.id.card_budget_info);
        cardAiResult      = view.findViewById(R.id.card_ai_result);
        cardInput         = view.findViewById(R.id.card_input);
        btnAiRecommend    = view.findViewById(R.id.btn_ai_recommend);
        btnSaveBudget     = view.findViewById(R.id.btn_save_budget);
        labelSetBudget    = view.findViewById(R.id.label_set_budget);
        tvSelectedCategory = view.findViewById(R.id.tv_selected_category);
        tvCurrentLimit    = view.findViewById(R.id.tv_current_limit);
        tvSpent           = view.findViewById(R.id.tv_spent);
        tvAiRecommendation = view.findViewById(R.id.tv_ai_recommendation);
        etBudgetAmount    = view.findViewById(R.id.et_budget_amount);

        categoryContainers = new View[categoryViews.length];
        for (int i = 0; i < categoryViews.length; i++) {
            categoryContainers[i] = view.findViewById(categoryViews[i]);
            final int catId = categoryIds[i];
            categoryContainers[i].setOnClickListener(v -> onCategorySelected(catId));
        }

        loadCurrency();

        btnAiRecommend.setOnClickListener(v -> getAiRecommendation());
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void loadCurrency() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
                SettingsManager sm = ((BudgetBuddyApp) Objects.requireNonNull(getActivity())
                        .getApplication()).getSettingsManager();
                currentCurrency = sm.getCurrency();
        });
    }

    private void onCategorySelected(int categoryId) {
        selectedCategoryId = categoryId;
        highlightCategory(categoryId);
        cardAiResult.setVisibility(View.GONE);
        loadBudgetForCategory(categoryId);
    }

    private void highlightCategory(int categoryId) {
        for (int i = 0; i < categoryIds.length; i++) {
            categoryContainers[i].setBackgroundColor(
                    categoryIds[i] == categoryId ? 0xFF4A7C7C : 0xFFF5F5F5
            );
        }
    }

    private void loadBudgetForCategory(int categoryId) {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        long start = TimeUtils.getStartOfMonth(0);
        long end   = TimeUtils.getEndOfMonth(0);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Category cat = db.categoryDao().getCategoryById(categoryId);
            Budget budget = null;
            if (cat != null && cat.budgetId > 0) {
                Budget candidate = db.budgetDao().getBudgetById(cat.budgetId);
                // Only treat as "current" if it matches the active month
                if (candidate != null && candidate.startDate == start && candidate.endDate == end) {
                    budget = candidate;
                }
            }
            double spent = db.expenseDao().getTotalForCategoryAndInterval(categoryId, start, end);

            final Budget finalBudget = budget;
            final double finalSpent  = spent;

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                existingBudgetId = finalBudget != null ? finalBudget.id : -1;

                cardBudgetInfo.setVisibility(View.VISIBLE);
                btnAiRecommend.setVisibility(View.VISIBLE);
                labelSetBudget.setVisibility(View.VISIBLE);
                cardInput.setVisibility(View.VISIBLE);
                btnSaveBudget.setVisibility(View.VISIBLE);

                tvSelectedCategory.setText(CategoryUtils.getEmoji(categoryId) + "  " + CategoryUtils.getName(categoryId));
                tvSpent.setText(String.format("%s %.2f", currentCurrency, finalSpent));

                if (finalBudget != null && finalBudget.limit > 0) {
                    tvCurrentLimit.setText(String.format("%s %d", currentCurrency, finalBudget.limit));
                    etBudgetAmount.setText(String.valueOf(finalBudget.limit));
                } else {
                    tvCurrentLimit.setText("Not set");
                    etBudgetAmount.setText("");
                }
            });
        });
    }

    private void getAiRecommendation() {
        if (selectedCategoryId == -1) return;

        tvAiRecommendation.setText("Loading…");
        cardAiResult.setVisibility(View.VISIBLE);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            double m0 = db.expenseDao().getTotalForCategoryAndInterval(
                    selectedCategoryId, TimeUtils.getStartOfMonth(0), TimeUtils.getEndOfMonth(0));
            double m1 = db.expenseDao().getTotalForCategoryAndInterval(
                    selectedCategoryId, TimeUtils.getStartOfMonth(1), TimeUtils.getEndOfMonth(1));
            double m2 = db.expenseDao().getTotalForCategoryAndInterval(
                    selectedCategoryId, TimeUtils.getStartOfMonth(2), TimeUtils.getEndOfMonth(2));
            double avg = (m0 + m1 + m2) / 3.0;
            String catName = CategoryUtils.getName(selectedCategoryId);

            ClaudeApiHelper.getBudgetRecommendation(
                    BuildConfig.ANTHROPIC_API_KEY,
                    catName, currentCurrency, m0, m1, avg,
                    new ClaudeApiHelper.ApiCallback() {
                        @Override public void onSuccess(String recommendation) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                tvAiRecommendation.setText(recommendation);
                                cardAiResult.setVisibility(View.VISIBLE);
                            });
                        }
                        @Override public void onError(String error) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() ->
                                    tvAiRecommendation.setText("⚠️ " + error));
                        }
                    }
            );
        });
    }

    private void saveBudget() {
        if (selectedCategoryId == -1) {
            Toast.makeText(requireContext(), "Please select a category first", Toast.LENGTH_SHORT).show();
            return;
        }
        String amountStr = etBudgetAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount;
        try {
            amount = (int) Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        long start = TimeUtils.getStartOfMonth(0);
        long end   = TimeUtils.getEndOfMonth(0);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (existingBudgetId > 0) {
                db.budgetDao().updateBudget(existingBudgetId, amount, start, end);
            } else {
                Budget newBudget = new Budget();
                newBudget.limit          = amount;
                newBudget.current_amount = 0;
                newBudget.startDate      = start;
                newBudget.endDate        = end;
                long newId = db.budgetDao().insertBudgetGetId(newBudget);
                db.categoryDao().updateBudgetId(selectedCategoryId, (int) newId);
                existingBudgetId = (int) newId;
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show();
                loadBudgetForCategory(selectedCategoryId);
            });
        });
    }
}
