package com.github.budgetbuddy;

import android.app.Application;

public class BudgetBuddyApp extends Application {
    private SettingsManager sm;

    @Override
    public void onCreate() {
        super.onCreate();
        sm = new SettingsManager(this);
    }

    public SettingsManager getSettingsManager() {
        return sm;
    }
}
