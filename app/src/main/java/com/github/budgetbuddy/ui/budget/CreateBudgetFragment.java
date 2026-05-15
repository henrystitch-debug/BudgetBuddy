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
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.adapters.CategoryAdapter;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.models.CreateBudgetViewModel;
import com.github.budgetbuddy.utils.MoneyUtils;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

public class CreateBudgetFragment extends Fragment {

    private View cardBudgetInfo, cardAiResult, cardInput, btnAiRecommend, btnSaveBudget, labelSetBudget;
    private TextView tvSelectedCategory, tvCurrentLimit, tvSpent, tvAiRecommendation;
    private EditText etBudgetAmount;
    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    private CreateBudgetViewModel viewModel;

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

        btnAiRecommend.setOnClickListener(v -> viewModel.requestAiRecommendation());
        btnSaveBudget.setOnClickListener(v  ->
                viewModel.saveBudget(etBudgetAmount.getText().toString().trim()));
        setupRecyclerView();
        loadCategories();
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
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
    }

    private void observeViewModel() {
        String currency = viewModel.getCurrency();

        viewModel.budget.observe(getViewLifecycleOwner(), budget -> {
            cardBudgetInfo.setVisibility(View.VISIBLE);
            btnAiRecommend.setVisibility(View.VISIBLE);
            labelSetBudget.setVisibility(View.VISIBLE);
            cardInput.setVisibility(View.VISIBLE);
            btnSaveBudget.setVisibility(View.VISIBLE);

            tvSelectedCategory.setText("");

            if (budget != null && budget.limitInCents > 0) {
                tvCurrentLimit.setText(MoneyUtils.fromCentsDisplay(budget.limitInCents, currency));
                etBudgetAmount.setText(MoneyUtils.fromCentsRaw(budget.limitInCents));
            } else {
                tvCurrentLimit.setText(R.string.val_not_set);
                etBudgetAmount.setText("");
            }
        });

        viewModel.spentCents.observe(getViewLifecycleOwner(), spentCents -> {
            if (spentCents == null) return;
            tvSpent.setText(MoneyUtils.fromCentsDisplay(spentCents, currency));
        });

        viewModel.aiResult.observe(getViewLifecycleOwner(), result -> {
            cardAiResult.setVisibility(View.VISIBLE);
            tvAiRecommendation.setText(result == null ? getString(R.string.loading) : result);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {

        categoryAdapter = new CategoryAdapter(category -> {

            selectedCategoryId = category.id;

            cardAiResult.setVisibility(View.GONE);

            tvSelectedCategory.setText(
                    category.icon + "  " + category.name
            );

            viewModel.onCategorySelected(category.id);
        });

        categoryRecyclerView.setLayoutManager(
                new GridLayoutManager(requireContext(), 3)
        );

        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadCategories() {

        AppDatabase.databaseWriteExecutor.execute(() -> {

            List<Category> categories =
                    AppDatabase
                            .getDatabase(requireContext())
                            .categoryDao()
                            .getAllCategories();

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {

                categoryAdapter.setCategories(categories);
            });
        });
    }
}