package com.github.budgetbuddy.database;

public final class DBConstants {
    private DBConstants() {}

    // Streak Constants
    public static final int MIN_STREAK = 1;
    public static final int INVALID = -1;
    public static final String DATABASE_NAME = "budget_buddy_database";

    public static final String FOOD_CATEGORY = "Food";
    public static final String HOME_CATEGORY = "Home";
    public static final String TRANSPORT_CATEGORY = "Transport";
    public static final String SCHOOL_CATEGORY = "School";
    public static final String HEALTH_CATEGORY = "Health";
    public static final String SHOPPING_CATEGORY = "Shopping";
    public static final String FUN_CATEGORY = "Fun";
    public static final String TRAVEL_CATEGORY = "Travel";
    public static final String PET_CATEGORY = "Pet";
    public static final String OTHER_CATEGORY = "Other";

    public static final Object[][] DEFAULT_CATEGORIES = {
    { "Food",      "🍴", "#4A7C7C" },
    { "Fun",       "⭐", "#F08040" },
    { "Travel",    "✈️", "#4A90C0" },
    { "Pet",       "🐾", "#88B948" },
    { "School",    "📚", "#9C6EBA" },
    { "Health",    "❤️", "#E05252" },
    { "Shopping",  "🛍️", "#3BAE8A" },
    { "Other",     "···","#9E9E9E" },
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
