package com.github.budgetbuddy.ui.onboarding.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import com.github.budgetbuddy.R;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;

public class WelcomeFragment extends Fragment implements OnboardingAdvancer {
    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_welcome_onboarding, c, false);
    }

    @Override public boolean validate() { return true; }
    @Override public void save(OnboardingData data) {}
}