package com.github.budgetbuddy.ui.overview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.utils.MoneyUtils;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(int expenseId);
    }

    private List<Expense> expenses;
    private Map<Integer, Category> categoryMap;  // ── ADDED
    private final OnExpenseClickListener listener;
    private final SettingsManager settingsManager;

    // ── CHANGED: added categoryMap parameter ──────────────────────────────
    public ExpenseAdapter(Context context, List<Expense> expenses,
                          Map<Integer, Category> categoryMap,
                          OnExpenseClickListener listener) {
        this.expenses    = new ArrayList<>(expenses);
        this.categoryMap = categoryMap;
        this.listener    = listener;
        this.settingsManager = new SettingsManager(context);
    }
    // ──────────────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        // ── CHANGED: look up from map instead of CategoryUtils ─────────────
        Category cat      = categoryMap.get(expense.categoryId);
        String   icon    = cat != null ? cat.icon : "?";
        String   catName  = cat != null ? cat.name  : "";
        // ──────────────────────────────────────────────────────────────────

        holder.tvCatIcon.setText(icon);

        String displayNote = (expense.note != null && !expense.note.trim().isEmpty())
                ? expense.note
                : catName;
        holder.tvNote.setText(displayNote);

        String date  = TimeUtils.formatDate(expense.entryDateStartInMilliSec);
        holder.tvMeta.setText(catName + " · " + date);

        String currency = settingsManager.getCurrency();
        holder.tvAmount.setText(MoneyUtils.fromCentsDisplay(expense.amountInCents, currency));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onExpenseClick(expense.id);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    // ── ADDED: allows caller to refresh the map alongside expenses ─────────
    public void updateExpenses(List<Expense> newExpenses, Map<Integer, Category> newCategoryMap) {
        this.expenses    = new ArrayList<>(newExpenses);
        this.categoryMap = newCategoryMap;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCatIcon;
        final TextView tvNote;
        final TextView tvMeta;
        final TextView tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCatIcon = itemView.findViewById(R.id.tv_cat_icon);
            tvNote    = itemView.findViewById(R.id.tv_note);
            tvMeta    = itemView.findViewById(R.id.tv_meta);
            tvAmount  = itemView.findViewById(R.id.tv_amount);
        }
    }
}