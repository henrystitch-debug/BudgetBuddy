package com.github.budgetbuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budgetbuddy.adapters.CategoryAdapter;
import com.github.budgetbuddy.database.entity.Category;

public class AddExpense extends AppCompatActivity {

    private Category selectedCategory = null;
    private CategoryAdapter categoryAdapter;
    private AddExpenseViewModel viewModel; // ← added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_alt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);

        categoryAdapter = new CategoryAdapter(category -> {
            selectedCategory = category;
        });

        RecyclerView categoryGrid = findViewById(R.id.categoryGrid);
        categoryGrid.setLayoutManager(new GridLayoutManager(this, 3));
        categoryGrid.setAdapter(categoryAdapter);

        // Load categories from DB into the grid
        viewModel.getCategories().observe(this, categories -> {
            categoryAdapter.setCategories(categories);
        });

        EditText amountInput = findViewById(R.id.amountInput);
        EditText noteInput = findViewById(R.id.noteInput);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String amountText = amountInput.getText().toString();
            String note = noteInput.getText().toString();

            // Validate first, then save
            if (selectedCategory == null) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountText);
            viewModel.saveExpense(amount, selectedCategory.id, note);
            finish(); // go back to previous screen after saving
        });
    }
}