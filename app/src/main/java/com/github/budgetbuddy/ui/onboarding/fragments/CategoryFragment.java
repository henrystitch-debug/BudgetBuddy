package com.github.budgetbuddy.ui.onboarding.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CategoryFragment extends Fragment implements OnboardingAdvancer {

    private ChipGroup chipGroup;
    private final List<String> selectedCategories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.fragment_categories_onboarding, c, false);
        chipGroup = v.findViewById(R.id.chip_group_categories);

        // pre-select all defaults
        selectedCategories.addAll(Arrays.asList(DBConstants.DEFAULT_CATEGORIES));
        for (String name : DBConstants.DEFAULT_CATEGORIES) {
            addChip(name, true);
        }

        v.findViewById(R.id.btn_add_custom).setOnClickListener(view -> showAddDialog());
        return v;
    }

    private void addChip(String name, boolean checked) {
        Chip chip = new Chip(requireContext());
        chip.setText(name);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setChipBackgroundColorResource(R.color.chip_background_selector);
        chip.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) selectedCategories.add(name);
            else selectedCategories.remove(name);
        });
        chipGroup.addView(chip);
    }

    private void showAddDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.cat_name);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_cat)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty() && !selectedCategories.contains(name)) {
                        selectedCategories.add(name);
                        addChip(name, true);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean validate() {
        if (selectedCategories.isEmpty()) {
            Toast.makeText(getContext(), R.string.empty_cat_prompt, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void save(OnboardingData data) {
        data.selectedCategories = new ArrayList<>(selectedCategories);
    }
}