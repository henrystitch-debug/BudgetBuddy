package com.github.budgetbuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    // Interface so the Fragment gets notified when a category is clicked
    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    private List<Category> categories = new ArrayList<>();
    private int selectedPosition = (int) RecyclerView.NO_ID; // tracks which card is selected
    private final OnCategorySelectedListener listener;

    public CategoryAdapter(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public List<Category> getCategories() {
        return categories;
    }

    // Call this to load/update the list of categories
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).id == categoryId) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    // --- The 3 required methods ---

    // 1. Inflates the card XML layout and wraps it in a ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    // 2. Fills each card with data from the Category object at that position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.icon.setText(category.icon);
        holder.name.setText(category.name);

        // Highlight the card if it's the selected one
        holder.itemView.setActivated(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previous);         // deselect old card
            notifyItemChanged(selectedPosition); // highlight new card

            listener.onCategorySelected(category); // tell the Fragment
        });
    }

    // 3. Tells the RecyclerView how many cards to draw
    @Override
    public int getItemCount() {
        return categories.size();
    }

    // --- ViewHolder ---
    // Holds references to the views inside one card so Android
    // doesn't have to look them up repeatedly (which is slow)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.categoryIcon);
            name = itemView.findViewById(R.id.categoryName);
        }
    }
}
