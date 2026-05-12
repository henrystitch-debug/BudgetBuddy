package com.github.budgetbuddy.ui.myfinances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.utils.TimeUtils;

public class MyFinancesFragment extends Fragment {

    private TextView tvBalance, tvIncome, tvExpenses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_finances, container, false);

        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpenses = view.findViewById(R.id.tv_expenses);

        // Balance card click navigates to Overview
        View cardBalance = view.findViewById(R.id.card_balance);
        cardBalance.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToOverview();
            }
        });

        loadData();

        return view;
    }

    private void loadData() {
        long startDate = TimeUtils.getStartOfMonth(0);
        long endDate = TimeUtils.getEndOfMonth(0);

        AppDatabase db = AppDatabase.getDatabase(requireContext());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            double expenses = db.expenseDao().getTotalForInterval(startDate, endDate);
            double income = db.budgetDao().getTotalBudgetLimitForInterval(startDate, endDate);
            double balance = income - expenses;

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                tvBalance.setText(String.format("€ %.2f", balance));
                tvIncome.setText(String.format("+ € %.2f", income));
                tvExpenses.setText(String.format("- € %.2f", expenses));
            });
        });
    }
}
