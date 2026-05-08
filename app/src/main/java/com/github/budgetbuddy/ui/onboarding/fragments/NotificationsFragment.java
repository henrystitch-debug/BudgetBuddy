package com.github.budgetbuddy.ui.onboarding.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationsFragment extends Fragment implements OnboardingAdvancer {

    private SwitchMaterial switchNotifs;

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.fragment_notifications_onboarding, c, false);
        switchNotifs = v.findViewById(R.id.switch_notifs);
        return v;
    }

    @Override public boolean validate() { return true; }

    @Override
    public void save(OnboardingData data) {
        data.notifsEnabled = switchNotifs.isChecked();
    }
}
