package com.github.budgetbuddy.ui.settings;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.text.InputType;
import android.widget.EditText;

import com.github.budgetbuddy.MainActivity;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Budget;
import com.github.budgetbuddy.database.entity.Profile;
import com.github.budgetbuddy.database.entity.Settings;
import com.github.budgetbuddy.database.entity.Streak;
import com.github.budgetbuddy.notification.NotificationHelper;
import com.github.budgetbuddy.ui.onboarding.OnboardingActivity;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class SettingsFragment extends Fragment {

    private TextView tvCurrentCurrency;
    private TextView tvActiveUser;
    private MaterialSwitch switchNotifications;

    private String currentCurrency = "€";
    private boolean notificationsEnabled = false;
    private int settingsId = 1;
    private int activeProfileId = 0;

    private static final String[] CURRENCY_SYMBOLS = {"€", "$", "£", "¥", "₩", "CHF", "kr", "zł"};
    private static final String[] CURRENCY_LABELS  = {
            "€  Euro", "$  Dollar", "£  Pound", "¥  Yen / Yuan",
            "₩  Won", "CHF  Swiss Franc", "kr  Krona", "zł  Złoty"
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    NotificationHelper.scheduleDailyReminder(requireContext());
                    saveSettings(currentCurrency, true);
                    Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    switchNotifications.setChecked(false);
                    notificationsEnabled = false;
                    Toast.makeText(requireContext(), "Permission denied — notifications disabled", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentCurrency = view.findViewById(R.id.tv_current_currency);
        tvActiveUser      = view.findViewById(R.id.tv_active_user);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        loadSettings();

        view.findViewById(R.id.card_switch_user).setOnClickListener(v -> showSwitchUserDialog());
        view.findViewById(R.id.card_add_user).setOnClickListener(v -> showAddUserDialog());
        view.findViewById(R.id.card_delete_user).setOnClickListener(v -> confirmDeleteUser());

        // Currency card click → show picker dialog
        view.findViewById(R.id.card_currency).setOnClickListener(v -> showCurrencyPicker());

        // Notifications switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // ignore programmatic changes
            if (isChecked) {
                enableNotifications();
            } else {
                NotificationHelper.cancelDailyReminder(requireContext());
                notificationsEnabled = false;
                saveSettings(currentCurrency, false);
                Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSettings() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Settings settings = db.settingsDao().getSettings();
            Profile activeProfile = null;
            if (settings != null && settings.activeProfileId > 0) {
                activeProfile = db.profileDao().getProfileById(settings.activeProfileId);
            }
            final Profile finalProfile = activeProfile;
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (settings != null) {
                    settingsId = settings.id;
                    activeProfileId = settings.activeProfileId;
                    currentCurrency = settings.currency != null ? settings.currency : "€";
                    notificationsEnabled = settings.notifsEnabled;
                }
                tvActiveUser.setText(finalProfile != null ? finalProfile.name : "Not set");
                updateCurrencyLabel();
                switchNotifications.setChecked(notificationsEnabled);
            });
        });
    }

    private void showSwitchUserDialog() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Profile> profiles = db.profileDao().getAllProfiles();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (profiles.isEmpty()) {
                    Toast.makeText(requireContext(), "No users yet — add one first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] names = new String[profiles.size()];
                int currentIndex = 0;
                for (int i = 0; i < profiles.size(); i++) {
                    names[i] = profiles.get(i).name;
                    if (profiles.get(i).id == activeProfileId) currentIndex = i;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Switch user")
                        .setSingleChoiceItems(names, currentIndex, (dialog, which) -> {
                            int chosenId = profiles.get(which).id;
                            dialog.dismiss();
                            switchToProfile(chosenId, profiles.get(which).name);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void switchToProfile(int profileId, String name) {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        int sid = settingsId;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.settingsDao().setActiveProfileId(sid, profileId);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                activeProfileId = profileId;
                tvActiveUser.setText(name);
                Toast.makeText(requireContext(), "Switched to " + name, Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToOverview();
                }
            });
        });
    }

    private void showAddUserDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Person name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add a new person")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addNewUser(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteUser() {
        if (activeProfileId <= 0) {
            Toast.makeText(requireContext(), "No active user to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = tvActiveUser.getText().toString();
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete user")
                .setMessage("This will permanently delete \"" + name + "\" and all of their expenses, budgets, and streak. This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteActiveUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteActiveUser() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        int profileId = activeProfileId;
        int sid = settingsId;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Reset category.budgetId references for this profile's budgets
            List<Budget> budgets = db.budgetDao().getBudgetsByProfile(profileId);
            for (Budget b : budgets) {
                db.categoryDao().clearBudgetId(b.id);
            }
            db.budgetDao().deleteByProfileId(profileId);
            db.expenseDao().deleteByProfileId(profileId);
            db.streakDao().deleteByProfileId(profileId);

            Profile profile = db.profileDao().getProfileById(profileId);
            if (profile != null) db.profileDao().deleteProfile(profile);

            db.settingsDao().setActiveProfileId(sid, 0);

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "User deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), OnboardingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    private void addNewUser(String name) {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        int sid = settingsId;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Profile existing = db.profileDao().getProfileByName(name);
            int profileId;
            if (existing != null) {
                profileId = existing.id;
            } else {
                Profile p = new Profile();
                p.name = name;
                long newId = db.profileDao().insertProfile(p);
                Streak streak = new Streak();
                streak.counter      = 0;
                streak.last_updated = 0;
                streak.start_Date   = System.currentTimeMillis();
                streak.profileId    = (int) newId;
                db.streakDao().insertNewStreak(streak);
                profileId = (int) newId;
            }
            db.settingsDao().setActiveProfileId(sid, profileId);
            final int finalProfileId = profileId;
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                activeProfileId = finalProfileId;
                tvActiveUser.setText(name);
                Toast.makeText(requireContext(),
                        existing != null ? "Switched to " + name : "Added " + name, Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToOverview();
                }
            });
        });
    }

    private void showCurrencyPicker() {
        int currentIndex = 0;
        for (int i = 0; i < CURRENCY_SYMBOLS.length; i++) {
            if (CURRENCY_SYMBOLS[i].equals(currentCurrency)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Currency")
                .setSingleChoiceItems(CURRENCY_LABELS, currentIndex, (dialog, which) -> {
                    currentCurrency = CURRENCY_SYMBOLS[which];
                    updateCurrencyLabel();
                    saveSettings(currentCurrency, notificationsEnabled);
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Currency updated to " + currentCurrency, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.isNotificationPermissionGranted(requireContext())) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        NotificationHelper.scheduleDailyReminder(requireContext());
        notificationsEnabled = true;
        saveSettings(currentCurrency, true);
        Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
    }

    private void saveSettings(String currency, boolean notifs) {
        int id = settingsId;
        AppDatabase.databaseWriteExecutor.execute(() ->
                AppDatabase.getDatabase(requireContext())
                        .settingsDao()
                        .updateStreak(currency, notifs, id)
        );
    }

    private void updateCurrencyLabel() {
        for (int i = 0; i < CURRENCY_SYMBOLS.length; i++) {
            if (CURRENCY_SYMBOLS[i].equals(currentCurrency)) {
                tvCurrentCurrency.setText(CURRENCY_LABELS[i]);
                return;
            }
        }
        tvCurrentCurrency.setText(currentCurrency);
    }
}
