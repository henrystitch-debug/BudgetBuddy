package com.github.budgetbuddy.ui.onboarding.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.database.entity.Category;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;


public class CategoryFragment extends Fragment implements OnboardingAdvancer {

    private ChipGroup chipGroup;
    private final List<String> selectedCategories = new ArrayList<>();
    private final List<Category> newCategories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.fragment_categories_onboarding, c, false);
        chipGroup = v.findViewById(R.id.chip_group_categories);

        // pre-select all defaults
        for (Object[] category : DBConstants.DEFAULT_CATEGORIES) {
            String name = (String) category[0];
            selectedCategories.add(name);
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

        // Inflate custom dialog layout
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category, null);

        EditText inputName = view.findViewById(R.id.inputCategoryName);
        Spinner spinnerEmoji = view.findViewById(R.id.spinnerEmoji);

        // Emoji options
        String[] emojis = {"\uD83D\uDE4A", "\uD83C\uDF33", "\uD83C\uDF0F", "\uD83C\uDFBC",
                "\uD83D\uDCBB", "\uD83D\uDD27", "⛵", "\uD83D\uDEB4", "\uD83C\uDF78", "\uD83D\uDC51"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                emojis
        );

        spinnerEmoji.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_cat)
                .setView(view)

                .setPositiveButton(R.string.add, (dialog, which) -> {

                    String name = inputName.getText().toString().trim();

                    if (name.isEmpty()) {
                        return;
                    }

                    // Prevent duplicates
                    if (selectedCategories.contains(name)) {
                        return;
                    }

                    if((selectedCategories.toArray().length + newCategories.toArray().length) >= 12){
                        return;
                    }

                    // Selected emoji
                    String emoji = spinnerEmoji.getSelectedItem().toString();
                    String colorHex = "";

                    //Assign colors
                    for(int i = 0; i < 16; i++){
                        if( i == newCategories.toArray().length){
                            colorHex = DBConstants.HEX_COLORS[i];
                        }
                    }

                    // Create category object
                    Category category = new Category (name, emoji, colorHex);

                    newCategories.add(category);

                    addChip(category.name, true);
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
        data.newCategories = new ArrayList<>(newCategories);
    }
}
