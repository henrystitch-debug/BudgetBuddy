package com.github.budgetbuddy.database;

public final class DBConstants {
    private DBConstants() {}

    // Streak Constants
    public static final int MIN_STREAK = 1;
    public static final int INVALID = -1;
    public static final String DATABASE_NAME = "budget_buddy_database";

    public static final Object[][] DEFAULT_CATEGORIES = {
    { "Food",      "🍴", "#4A7C7C" },
    { "Fun",       "⭐", "#F08040" },
    { "Travel",    "✈️", "#4A90C0" },
    { "Pet",       "🐾", "#88B948" },
    { "School",    "📚", "#9C6EBA" },
    { "Health",    "❤️", "#E05252" },
    { "Shopping",  "🛍️", "#3BAE8A" },
    { "Home",      "🏠", "#5C85D6" },
    { "Transport", "🚌", "#E8A838" },
};
    public static final String DOLLAR_SIGN_CURRENCY = "$";
    public static final String BR_POUND_SIGN_CURRENCY = "£";

    public static final String YUAN_SIGN_CURRENCY = "¥";

    public static final String EURO_SIGN_CURRENCY = "€";
    public static final String DEFAULT_CURRENCY = EURO_SIGN_CURRENCY;
    public static final boolean DEFAULT_NOTIFS_ENABLED = false;
}
