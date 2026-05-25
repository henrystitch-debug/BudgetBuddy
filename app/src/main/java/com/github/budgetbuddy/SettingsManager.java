package com.github.budgetbuddy;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.budgetbuddy.database.DBConstants;

import java.util.Locale;

public class SettingsManager {
    private static final String PREFS_NAME = "budget_buddy_prefs";
    private static final String KEY_CURRENCY = "currency";

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_NOTIFS_ENABLED = "notifs_enabled";

    private static final Locale ENGLISH_REGION_LOCALE = Locale.ENGLISH;
    private static final Locale CHINESE_REGION_LOCALE = Locale.CHINESE;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getCurrency() {
        return prefs.getString(KEY_CURRENCY, DBConstants.DEFAULT_CURRENCY);
    }

    public void setCurrency(String currency) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply();
    }

    public boolean isNotifsEnabled() {
        return prefs.getBoolean(KEY_NOTIFS_ENABLED, DBConstants.DEFAULT_NOTIFS_ENABLED);
    }

    public void setNotifsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFS_ENABLED, enabled).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }
}
