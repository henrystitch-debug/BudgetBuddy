package com.github.budgetbuddy.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.budgetbuddy.BudgetBuddyApp;
import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.SettingsManager;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etName;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        settingsManager = ((BudgetBuddyApp) getApplication()).getSettingsManager();

        etName = findViewById(R.id.et_name);
        Button btnContinue = findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        settingsManager.setUserName(name);

        Toast.makeText(this, "Hi " + name + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
