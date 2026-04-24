package com.github.budgetbuddy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.database.entity.Expense;

public class MainActivity extends AppCompatActivity {

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "budget_buddy_db"
        ).allowMainThreadQueries().build();

        //Test to initialize tables

        Category c = new Category();
        c.name = "Food";
        db.categoryDao().insertCategory(c);
        Expense e = new Expense();
        e.amount = 0;
        e.categoryId = 1;
        e.entryDate = System.currentTimeMillis();
        e.note = "Test";
        e.repeat = "NEVER";

        db.expenseDao().insert(e);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}