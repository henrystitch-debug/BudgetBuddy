package com.github.budgetbuddy.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Profile;
import com.github.budgetbuddy.database.entity.Settings;
import com.github.budgetbuddy.database.entity.Streak;

public class OnboardingActivity extends AppCompatActivity {

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

        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Profile existing = db.profileDao().getProfileByName(name);
            int profileId;
            boolean isReturning;
            if (existing != null) {
                profileId = existing.id;
                isReturning = true;
            } else {
                Profile p = new Profile();
                p.name = name;
                long newId = db.profileDao().insertProfile(p);
                profileId = (int) newId;

                Streak streak = new Streak();
                streak.counter      = 0;
                streak.last_updated = 0;
                streak.start_Date   = System.currentTimeMillis();
                streak.profileId    = profileId;
                db.streakDao().insertNewStreak(streak);
                isReturning = false;
            }

            Settings settings = db.settingsDao().getSettings();
            if (settings != null) {
                db.settingsDao().setActiveProfileId(settings.id, profileId);
            }

            final boolean returning = isReturning;
            runOnUiThread(() -> {
                Toast.makeText(this,
                        returning ? "Welcome back, " + name + "!" : "Hi " + name + ", let's get started!",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
