package com.github.budgetbuddy.database;

public final class DBConstants {
    private DBConstants() {}

    // Streak Constants
    public static final int MIN_STREAK = 1;
    public static final int INVALID = -1;
    public static final String DATABASE_NAME = "budget_buddy_database";

    public static final Object[][] DEFAULT_CATEGORIES = {
    { "Food",      "🍴"},
    { "Fun",       "⭐"},
    { "Travel",    "✈️"},
    { "Pet",       "🐾"},
    { "School",    "📚"},
    { "Health",    "❤️"},
    { "Shopping",  "🛍️"},
    { "Home",      "🏠"},
    { "Transport", "🚌"},
};
    public static final String[] HEX_COLORS = {"#4A90C0", "#88B948", "#CEA7EB", "#F0A781", "#D3DB51", "#E05CD9", "#E05252", "#3E9645", "#F08040",
                                               "#4A7C7C", "#7D4242", "#4331BD"};
    public static final String DOLLAR_SIGN_CURRENCY = "$";
    public static final String BR_POUND_SIGN_CURRENCY = "£";

    public static final String YUAN_SIGN_CURRENCY = "¥";

    public static final String EURO_SIGN_CURRENCY = "€";
    public static final String DEFAULT_CURRENCY = EURO_SIGN_CURRENCY;
    public static final boolean DEFAULT_NOTIFS_ENABLED = false;
}
