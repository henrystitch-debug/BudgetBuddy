package com.github.budgetbuddy.ui.onboarding.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;

public class NameFragment extends Fragment implements OnboardingAdvancer {
    private EditText etName;

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.fragment_name_onboarding, c, false);
        etName = v.findViewById(R.id.et_name);
        return v;
    }

    @Override
    public boolean validate() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Please enter your name");
            return false;
        }
        return true;
    }

    @Override
    public void save(OnboardingData data) {
        data.name = etName.getText().toString().trim();
    }
}