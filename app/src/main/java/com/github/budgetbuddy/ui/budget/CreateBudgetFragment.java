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
import androidx.lifecycle.ViewModelProvider;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.models.CreateBudgetViewModel;
import com.github.budgetbuddy.utils.CategoryUtils;
import com.github.budgetbuddy.utils.MoneyUtils;

public class CreateBudgetFragment extends Fragment {

    private int selectedCategoryId = -1;

    private View     cardBudgetInfo, cardAiResult, cardInput, btnAiRecommend, btnSaveBudget, labelSetBudget;
    private TextView tvSelectedCategory, tvCurrentLimit, tvSpent, tvAiRecommendation;
    private EditText etBudgetAmount;

    private CreateBudgetViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(CreateBudgetViewModel.class);

        bindViews(view);
        observeViewModel();
        setupCategoryClicks();

        btnAiRecommend.setOnClickListener(v -> viewModel.requestAiRecommendation());
        btnSaveBudget.setOnClickListener(v  ->
                viewModel.saveBudget(etBudgetAmount.getText().toString().trim()));
    }

    private void bindViews(@NonNull View view) {
        cardBudgetInfo     = view.findViewById(R.id.card_budget_info);
        cardAiResult       = view.findViewById(R.id.card_ai_result);
        cardInput          = view.findViewById(R.id.card_input);
        btnAiRecommend     = view.findViewById(R.id.btn_ai_recommend);
        btnSaveBudget      = view.findViewById(R.id.btn_save_budget);
        labelSetBudget     = view.findViewById(R.id.label_set_budget);
        tvSelectedCategory = view.findViewById(R.id.tv_selected_category);
        tvCurrentLimit     = view.findViewById(R.id.tv_current_limit);
        tvSpent            = view.findViewById(R.id.tv_spent);
        tvAiRecommendation = view.findViewById(R.id.tv_ai_recommendation);
        etBudgetAmount     = view.findViewById(R.id.et_budget_amount);

        categoryContainers = new View[categoryViews.length];
        for (int i = 0; i < categoryViews.length; i++) {
            categoryContainers[i] = view.findViewById(categoryViews[i]);
        }
    }

    private void observeViewModel() {
        String currency = viewModel.getCurrency();

        // Budget loaded for selected category
        viewModel.budget.observe(getViewLifecycleOwner(), budget -> {
            cardBudgetInfo.setVisibility(View.VISIBLE);
            btnAiRecommend.setVisibility(View.VISIBLE);
            labelSetBudget.setVisibility(View.VISIBLE);
            cardInput.setVisibility(View.VISIBLE);
            btnSaveBudget.setVisibility(View.VISIBLE);

            String toSet = CategoryUtils.getEmoji(selectedCategoryId)
                            + "  " + CategoryUtils.getName(selectedCategoryId);
            tvSelectedCategory.setText(toSet);

            if (budget != null && budget.limitInCents > 0) {
                tvCurrentLimit.setText(MoneyUtils.fromCentsDisplay(budget.limitInCents, currency));
                etBudgetAmount.setText(MoneyUtils.fromCentsRaw(budget.limitInCents));
            } else {
                tvCurrentLimit.setText(R.string.val_not_set);
                etBudgetAmount.setText("");
            }
        });

        // Spent amount for selected category
        viewModel.spentCents.observe(getViewLifecycleOwner(), spentCents -> {
            if (spentCents == null) return;
            tvSpent.setText(MoneyUtils.fromCentsDisplay(spentCents, currency));
        });

        viewModel.aiResult.observe(getViewLifecycleOwner(), result -> {
            cardAiResult.setVisibility(View.VISIBLE);
            if (result == null) {
                tvAiRecommendation.setText(R.string.loading);
            } else {
                tvAiRecommendation.setText(result);
            }
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCategoryClicks() {
        for (int i = 0; i < categoryIds.length; i++) {
            final int catId = categoryIds[i];
            categoryContainers[i].setOnClickListener(v -> {
                selectedCategoryId = catId;
                highlightCategory(catId);
                cardAiResult.setVisibility(View.GONE);
                viewModel.onCategorySelected(catId);
            });
        }
    }

    private void highlightCategory(int categoryId) {
        for (int i = 0; i < categoryIds.length; i++) {
            categoryContainers[i].setBackgroundColor(
                    categoryIds[i] == categoryId ? 0xFF4A7C7C : 0xFFF5F5F5
            );
        }
    }
}