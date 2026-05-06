package com.github.budgetbuddy.utils;

import android.graphics.Color;

public class CategoryUtils {

    public static String getName(int categoryId) {
        switch (categoryId) {
            case 1: return "Food";
            case 2: return "Home";
            case 3: return "Transport";
            case 4: return "School";
            case 5: return "Health";
            case 6: return "Shopping";
            case 7: return "Fun";
            case 8: return "Other";
            case 9: return "Coffee";
            case 10: return "Travel";
            case 11: return "Gift";
            case 12: return "Pet";
            default: return "Unknown";
        }
    }

    public static String getEmoji(int categoryId) {
        switch (categoryId) {
            case 1: return "🍴";
            case 2: return "🏠";
            case 3: return "🚌";
            case 4: return "📚";
            case 5: return "❤️";
            case 6: return "🛍️";
            case 7: return "⭐";
            case 8: return "···";
            case 9: return "☕";
            case 10: return "✈️";
            case 11: return "🎁";
            case 12: return "🐾";
            default: return "?";
        }
    }

    public static int getColor(int categoryId) {
        switch (categoryId) {
            case 1: return Color.parseColor("#4A7C7C"); // teal - Food
            case 2: return Color.parseColor("#5C85D6"); // blue - Home
            case 3: return Color.parseColor("#E8A838"); // amber - Transport
            case 4: return Color.parseColor("#9C6EBA"); // purple - School
            case 5: return Color.parseColor("#E05252"); // red - Health
            case 6: return Color.parseColor("#3BAE8A"); // green - Shopping
            case 7: return Color.parseColor("#F08040"); // orange - Fun
            case 8: return Color.parseColor("#9E9E9E"); // gray - Other
            case 9: return Color.parseColor("#8B6F47"); // brown - Coffee
            case 10: return Color.parseColor("#4A90C0"); // sky blue - Travel
            case 11: return Color.parseColor("#E8669C"); // pink - Gift
            case 12: return Color.parseColor("#88B948"); // lime - Pet
            default: return Color.parseColor("#CCCCCC");
        }
    }
}
