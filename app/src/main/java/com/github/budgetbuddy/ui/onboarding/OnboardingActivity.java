package com.github.budgetbuddy.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;

public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "budget_buddy_prefs";
    public static final String KEY_USER_NAME = "user_name";

    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

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

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_NAME, name).apply();

        Toast.makeText(this, "Hi " + name + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public static String getUserName(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_NAME, null);
    }

    public static void setUserName(Context ctx, String name) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_NAME, name)
                .apply();
    }
}
