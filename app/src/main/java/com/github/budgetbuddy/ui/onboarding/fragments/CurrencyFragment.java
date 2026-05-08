package com.github.budgetbuddy.ui.onboarding.fragments;


import androidx.fragment.app.Fragment;

import com.github.budgetbuddy.database.DBConstants;
import com.github.budgetbuddy.ui.onboarding.OnboardingData;
import com.google.android.material.card.MaterialCardView;

import java.util.LinkedHashMap;
import java.util.Map;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.budgetbuddy.R;
import com.github.budgetbuddy.ui.onboarding.OnboardingAdvancer;

public class CurrencyFragment extends Fragment implements OnboardingAdvancer {
    private String selected = DBConstants.DEFAULT_CURRENCY;

    private final Map<String, MaterialCardView> tiles = new LinkedHashMap<>();

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        View v = i.inflate(R.layout.fragment_currency_onboarding, c, false);

        tiles.put(DBConstants.EURO_SIGN_CURRENCY, v.findViewById(R.id.tile_eur));
        tiles.put(DBConstants.DOLLAR_SIGN_CURRENCY, v.findViewById(R.id.tile_usd));
        tiles.put(DBConstants.BR_POUND_SIGN_CURRENCY, v.findViewById(R.id.tile_gbp));
        tiles.put(DBConstants.YUAN_SIGN_CURRENCY, v.findViewById(R.id.tile_jpy));

        for (Map.Entry<String, MaterialCardView> entry : tiles.entrySet()) {
            entry.getValue().setOnClickListener(view -> selectTile(entry.getKey()));
        }

        selectTile(selected); // default highlight
        return v;
    }

    private void selectTile(String currency) {
        selected = currency;
        for (Map.Entry<String, MaterialCardView> entry : tiles.entrySet()) {
            boolean active = entry.getKey().equals(currency);
            entry.getValue().setStrokeColor(active ? 0xFF4A7C7C : 0x00000000);
            entry.getValue().setStrokeWidth(active ? 4 : 0);
            entry.getValue().setCardBackgroundColor(active ? 0xFFE8F2F2 : 0xFFFFFFFF);
        }
    }

    @Override public boolean validate() { return true; }

    @Override
    public void save(OnboardingData data) {
        data.currency = selected;
    }
}
