package com.github.budgetbuddy.ui.expense;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Expense;

public class AddExpenseFragment extends Fragment {

    private static final String ARG_EXPENSE_ID = "expenseId";

    private int selectedCategoryId = -1;
    private int expenseId = -1;

    private LinearLayout catFood, catHome, catTransport, catSchool;
    private LinearLayout catHealth, catShopping, catFun, catOther;
    private EditText etAmount, etNote;

    private static final int COLOR_SELECTED = Color.parseColor("#4A7C7C");
    private static final int COLOR_DEFAULT = Color.parseColor("#F5F5F5");

    public static AddExpenseFragment newInstance(int expenseId) {
        AddExpenseFragment fragment = new AddExpenseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EXPENSE_ID, expenseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Read arguments
        if (getArguments() != null) {
            expenseId = getArguments().getInt(ARG_EXPENSE_ID, -1);
        }

        // Bind views
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);

        catFood = view.findViewById(R.id.cat_food);
        catHome = view.findViewById(R.id.cat_home);
        catTransport = view.findViewById(R.id.cat_transport);
        catSchool = view.findViewById(R.id.cat_school);
        catHealth = view.findViewById(R.id.cat_health);
        catShopping = view.findViewById(R.id.cat_shopping);
        catFun = view.findViewById(R.id.cat_fun);
        catOther = view.findViewById(R.id.cat_other);

        // Set category click listeners
        catFood.setOnClickListener(v -> selectCategory(1));
        catHome.setOnClickListener(v -> selectCategory(2));
        catTransport.setOnClickListener(v -> selectCategory(3));
        catSchool.setOnClickListener(v -> selectCategory(4));
        catHealth.setOnClickListener(v -> selectCategory(5));
        catShopping.setOnClickListener(v -> selectCategory(6));
        catFun.setOnClickListener(v -> selectCategory(7));
        catOther.setOnClickListener(v -> selectCategory(8));

        // Cancel button
        view.findViewById(R.id.btn_cancel).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        // Save button
        view.findViewById(R.id.btn_save_expense).setOnClickListener(v -> saveExpense());

        // Edit mode: load existing expense
        if (expenseId > 0) {
            loadExpenseForEdit();
        }

        return view;
    }

    private void selectCategory(int categoryId) {
        selectedCategoryId = categoryId;
        resetAllCategories();
        getLayoutForCategory(categoryId).setBackgroundColor(COLOR_SELECTED);
    }

    private void resetAllCategories() {
        catFood.setBackgroundColor(COLOR_DEFAULT);
        catHome.setBackgroundColor(COLOR_DEFAULT);
        catTransport.setBackgroundColor(COLOR_DEFAULT);
        catSchool.setBackgroundColor(COLOR_DEFAULT);
        catHealth.setBackgroundColor(COLOR_DEFAULT);
        catShopping.setBackgroundColor(COLOR_DEFAULT);
        catFun.setBackgroundColor(COLOR_DEFAULT);
        catOther.setBackgroundColor(COLOR_DEFAULT);
    }

    private LinearLayout getLayoutForCategory(int categoryId) {
        switch (categoryId) {
            case 1: return catFood;
            case 2: return catHome;
            case 3: return catTransport;
            case 4: return catSchool;
            case 5: return catHealth;
            case 6: return catShopping;
            case 7: return catFun;
            case 8: return catOther;
            default: return catOther;
        }
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote.getText().toString().trim();
        long now = System.currentTimeMillis();

        AppDatabase db = AppDatabase.getDatabase(requireContext());

        if (expenseId > 0) {
            // Update existing expense
            int idToUpdate = expenseId;
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.expenseDao().updateExpense(idToUpdate, amount, selectedCategoryId, now, note, "");
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    getParentFragmentManager().popBackStack();
                });
            });
        } else {
            // Insert new expense
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Expense expense = new Expense();
                expense.amount = amount;
                expense.categoryId = selectedCategoryId;
                expense.entryDate = now;
                expense.note = note;
                expense.repeat = "";
                db.expenseDao().insert(expense);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    getParentFragmentManager().popBackStack();
                });
            });
        }
    }

    private void loadExpenseForEdit() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Expense expense = db.expenseDao().getExpenseById(expenseId);
            if (!isAdded()) return;
            if (expense == null) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                etAmount.setText(String.valueOf(expense.amount));
                if (expense.note != null) {
                    etNote.setText(expense.note);
                }
                selectCategory(expense.categoryId);
            });
        });
    }
}
