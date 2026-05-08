package com.github.budgetbuddy.ui.settings;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.AppDatabase;
import com.github.budgetbuddy.database.entity.Settings;
import com.github.budgetbuddy.notification.NotificationHelper;
import com.github.budgetbuddy.ui.onboarding.OnboardingActivity;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    private TextView tvCurrentCurrency;
    private TextView tvCurrentName;
    private MaterialSwitch switchNotifications;

    private String currentCurrency = "€";
    private boolean notificationsEnabled = false;
    private int settingsId = 1;

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
        tvCurrentName     = view.findViewById(R.id.tv_current_name);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        loadSettings();

        view.findViewById(R.id.card_change_name).setOnClickListener(v -> showChangeNameDialog());
        view.findViewById(R.id.card_currency).setOnClickListener(v -> showCurrencyPicker());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
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
        String name = OnboardingActivity.getUserName(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Settings settings = AppDatabase.getDatabase(requireContext()).settingsDao().getSettings();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (settings != null) {
                    settingsId = settings.id;
                    currentCurrency = settings.currency != null ? settings.currency : "€";
                    notificationsEnabled = settings.notifsEnabled;
                }
                tvCurrentName.setText(name != null ? name : "Not set");
                updateCurrencyLabel();
                switchNotifications.setChecked(notificationsEnabled);
            });
        });
    }

    private void showChangeNameDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        String current = OnboardingActivity.getUserName(requireContext());
        if (current != null) input.setText(current);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
                .setTitle("Change name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OnboardingActivity.setUserName(requireContext(), newName);
                    tvCurrentName.setText(newName);
                    Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                        .updateSettings(currency, notifs, id)
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
