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
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;
import com.github.budgetbuddy.models.AddExpenseViewModel;
import com.github.budgetbuddy.utils.MoneyUtils;

import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private Category selectedCategory = null;

    private CategoryAdapter categoryAdapter;
    private AddExpenseViewModel viewModel;
    private int expenseId = -1;

    private EditText amountInput;
    private EditText noteInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_expense_alt);
        expenseId = getIntent().getIntExtra("expenseId", -1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        viewModel = new ViewModelProvider(this)
                .get(AddExpenseViewModel.class);

        setupCategoryGrid();

        amountInput = findViewById(R.id.amountInput);
        noteInput = findViewById(R.id.noteInput);

        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> {

            String amountText =
                    amountInput.getText().toString().trim();

            String note =
                    noteInput.getText().toString().trim();

            if (selectedCategory == null) {
                Toast.makeText(this,
                        "Please select a category",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountText.isEmpty()) {
                Toast.makeText(this,
                        "Please enter an amount",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!MoneyUtils.isValidMoneyInput(amountText)) {
                Toast.makeText(this,
                        "Invalid amount",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            long amountInCents =
                    MoneyUtils.toCents(amountText);

            if (expenseId > 0) {

                viewModel.updateExpense(
                        expenseId,
                        amountInCents,
                        selectedCategory.id,
                        note
                );

            } else {

                viewModel.saveExpense(
                        amountInCents,
                        selectedCategory.id,
                        note
                );
            }

            Toast.makeText(this,
                    "Expense saved",
                    Toast.LENGTH_SHORT).show();

            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
        if (expenseId > 0) {
            loadExpenseForEdit();
        }
    }

    private void setupCategoryGrid() {

        categoryAdapter = new CategoryAdapter(category -> {
            selectedCategory = category;
        });
        RecyclerView categoryGrid =
                findViewById(R.id.categoryGrid);
        categoryGrid.setLayoutManager(
                new GridLayoutManager(this, 4));
        categoryGrid.setAdapter(categoryAdapter);
        // Load categories from DB into the grid
        viewModel.getCategories().observe(this, categories -> {

            categoryAdapter.setCategories(categories);

            // If editing an existing expense,
            // preload the data after categories are loaded
            if (expenseId > 0) {
                loadExpenseForEdit();
            }
        });
    }

    private void loadExpenseForEdit() {

        AppDatabase.databaseWriteExecutor.execute(() -> {
            var expense = viewModel.getExpenseById(expenseId);

            if (expense == null) return;
            runOnUiThread(() -> {
                amountInput.setText(
                        MoneyUtils.fromCentsRaw(expense.amountInCents));
                noteInput.setText(expense.note);
                List<Category> categories =
                        categoryAdapter.getCategories();

                for (Category category : categories) {
                    if (category.id == expense.categoryId) {
                        selectedCategory = category;
                        categoryAdapter.setSelectedCategoryId(category.id);
                        break;
                    }
                }
            });
        });
    }
}