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

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.database.entity.Streak;

import java.util.Calendar;

public class AddExpenseFragment extends Fragment {

    private static final String ARG_EXPENSE_ID = "expenseId";

    private int selectedCategoryId = -1;
    private int expenseId = -1;

    private LinearLayout catFood, catHome, catTransport, catSchool;
    private LinearLayout catHealth, catShopping, catFun, catOther;
    private LinearLayout catCoffee, catTravel, catGift, catPet;
    private EditText etAmount, etNote;

    private static final int COLOR_SELECTED = Color.parseColor("#4A7C7C");
    private static final int COLOR_DEFAULT  = Color.parseColor("#F5F5F5");

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

        if (getArguments() != null) {
            expenseId = getArguments().getInt(ARG_EXPENSE_ID, -1);
        }

        etAmount     = view.findViewById(R.id.et_amount);
        etNote       = view.findViewById(R.id.et_note);

        catFood      = view.findViewById(R.id.cat_food);
        catHome      = view.findViewById(R.id.cat_home);
        catTransport = view.findViewById(R.id.cat_transport);
        catSchool    = view.findViewById(R.id.cat_school);
        catHealth    = view.findViewById(R.id.cat_health);
        catShopping  = view.findViewById(R.id.cat_shopping);
        catFun       = view.findViewById(R.id.cat_fun);
        catOther     = view.findViewById(R.id.cat_other);
        catCoffee    = view.findViewById(R.id.cat_coffee);
        catTravel    = view.findViewById(R.id.cat_travel);
        catGift      = view.findViewById(R.id.cat_gift);
        catPet       = view.findViewById(R.id.cat_pet);

        catFood.setOnClickListener(v      -> selectCategory(1));
        catHome.setOnClickListener(v      -> selectCategory(2));
        catTransport.setOnClickListener(v -> selectCategory(3));
        catSchool.setOnClickListener(v    -> selectCategory(4));
        catHealth.setOnClickListener(v    -> selectCategory(5));
        catShopping.setOnClickListener(v  -> selectCategory(6));
        catFun.setOnClickListener(v       -> selectCategory(7));
        catOther.setOnClickListener(v     -> selectCategory(8));
        catCoffee.setOnClickListener(v    -> selectCategory(9));
        catTravel.setOnClickListener(v    -> selectCategory(10));
        catGift.setOnClickListener(v      -> selectCategory(11));
        catPet.setOnClickListener(v       -> selectCategory(12));

        view.findViewById(R.id.btn_save_expense).setOnClickListener(v -> saveExpense());
        view.findViewById(R.id.btn_cancel_expense).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToOverview();
            }
        });

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
        catCoffee.setBackgroundColor(COLOR_DEFAULT);
        catTravel.setBackgroundColor(COLOR_DEFAULT);
        catGift.setBackgroundColor(COLOR_DEFAULT);
        catPet.setBackgroundColor(COLOR_DEFAULT);
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
            case 9: return catCoffee;
            case 10: return catTravel;
            case 11: return catGift;
            case 12: return catPet;
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
            int idToUpdate = expenseId;
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.expenseDao().updateExpense(idToUpdate, amount, selectedCategoryId, now, note, "");
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToOverview();
                    }
                });
            });
        } else {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Expense expense = new Expense();
                expense.amount     = amount;
                expense.categoryId = selectedCategoryId;
                expense.entryDate  = now;
                expense.note       = note;
                expense.repeat     = "";
                db.expenseDao().insert(expense);

                updateStreakAfterLog(db, now);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Expense saved!", Toast.LENGTH_SHORT).show();
                    resetFormForNextEntry();
                });
            });
        }
    }

    private void resetFormForNextEntry() {
        etAmount.setText("");
        etNote.setText("");
        selectedCategoryId = -1;
        resetAllCategories();
        etAmount.requestFocus();
    }

    /** Day-streak: same day = no change, next day = +1, gap > 1 = reset to 1. */
    private static void updateStreakAfterLog(AppDatabase db, long now) {
        Streak streak = db.streakDao().getCurrentStreak();
        if (streak == null) {
            createStreakRecord(db, now);
            return;
        }

        long lastUpdated = streak.last_updated;
        int days = daysBetween(lastUpdated, now);
        if (lastUpdated == 0) {
            int existingOrMinimumCount = Math.max(1, streak.counter);
            db.streakDao().updateStreakById(existingOrMinimumCount, now, streak.id);
            return;
        }

        if (days >= 2) {
            createStreakRecord(db, now);
            return;
        } else if (days == 1) {
            int newCount = streak.counter + 1;
            db.streakDao().updateStreakById(newCount, now, streak.id);
        } else {
            return; // same day, no-op
        }
    }

    private static void createStreakRecord(AppDatabase db, long now) {
        Streak newStreak = new Streak();
        newStreak.counter = 1;
        newStreak.start_Date = now;
        newStreak.last_updated = now;
        db.streakDao().insertNewStreak(newStreak);
    }

    private static int daysBetween(long fromMillis, long toMillis) {
        if (fromMillis <= 0) return Integer.MAX_VALUE;
        Calendar a = Calendar.getInstance();
        a.setTimeInMillis(fromMillis);
        a.set(Calendar.HOUR_OF_DAY, 0);
        a.set(Calendar.MINUTE, 0);
        a.set(Calendar.SECOND, 0);
        a.set(Calendar.MILLISECOND, 0);
        Calendar b = Calendar.getInstance();
        b.setTimeInMillis(toMillis);
        b.set(Calendar.HOUR_OF_DAY, 0);
        b.set(Calendar.MINUTE, 0);
        b.set(Calendar.SECOND, 0);
        b.set(Calendar.MILLISECOND, 0);
        long diff = b.getTimeInMillis() - a.getTimeInMillis();
        return (int) (diff / (24L * 60L * 60L * 1000L));
    }

    private void loadExpenseForEdit() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Expense expense = db.expenseDao().getExpenseById(expenseId);
            if (!isAdded() || expense == null) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                etAmount.setText(String.valueOf(expense.amount));
                if (expense.note != null) etNote.setText(expense.note);
                selectCategory(expense.categoryId);
            });
        });
    }
}
