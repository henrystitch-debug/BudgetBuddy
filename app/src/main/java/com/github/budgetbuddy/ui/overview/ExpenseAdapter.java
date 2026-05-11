package com.github.budgetbuddy.ui.overview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.utils.CategoryUtils;
import com.github.budgetbuddy.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(int expenseId);
    }

    private List<Expense> expenses;
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(List<Expense> expenses, OnExpenseClickListener listener) {
        this.expenses = new ArrayList<>(expenses);
        this.listener = listener;
    }

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

        // Category icon (emoji)
        holder.tvCatIcon.setText(CategoryUtils.getEmoji(expense.categoryId));

        // Note: use category name if note is null or empty
        String displayNote = (expense.note != null && !expense.note.trim().isEmpty())
                ? expense.note
                : CategoryUtils.getName(expense.categoryId);
        holder.tvNote.setText(displayNote);

        // Meta: "CategoryName · Apr 10"
        String categoryName = CategoryUtils.getName(expense.categoryId);
        String date = TimeUtils.formatDate(expense.entryDateStartInMilliSec);
        holder.tvMeta.setText(categoryName + " · " + date);

        // Amount
        holder.tvAmount.setText(String.format("- € %.2f", expense.amountInCents));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = new ArrayList<>(newExpenses);
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
            tvNote = itemView.findViewById(R.id.tv_note);
            tvMeta = itemView.findViewById(R.id.tv_meta);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
