package com.github.budgetbuddy.utils;

import android.icu.math.BigDecimal;
import android.icu.text.NumberFormat;

import androidx.annotation.Nullable;

import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Locale;

public final class MoneyUtils {
    final static String TWO_DEC_PLACES_REGEX = "\\d+(\\.\\d{0,2})?";
    final static String SIGNIFICANT_DIGITS_REGEX = "[^\\d.]";

    private MoneyUtils() {} // prevent instantiation

    /**
     * Converts a user-entered monetary string to cents (long).
     * Accepts: "12", "12.9", "12.99", "12.999" (truncates beyond 2 decimal places)
     * Returns 0 if input is null, empty, or invalid.
     */
    public static long toCents(String input, Locale locale) {
        if (input == null || input.trim().isEmpty()) return 0L;

        try {
            NumberFormat fmt = NumberFormat.getInstance(locale);
            Number parsed = fmt.parse(input.trim());
            if (parsed == null) return 0L;

            return new BigDecimal(parsed.toString())
                    .setScale(2, RoundingMode.DOWN.ordinal())
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

        } catch (ParseException | NumberFormatException e) {
            return 0L;
        }
    }

    // Convenience — uses device locale
    public static long toCents(String input) {
        return toCents(input, Locale.getDefault());
    }

    /**
     * For internal use: storage, logging, inter-layer passing.
     * Always produces dot-decimal. Safe to re-parse.
     * e.g. 1299L → "12.99"
     */
    public static String fromCentsRaw(long cents) {
        return String.format(Locale.US, "%.2f", cents / 100.0);
    }

    /**
     * For UI display only. Respects device locale.
     * e.g. 1299L → "12.99" (US) or "12,99" (DE) or "12.99" (TW)
     * WARNING: do NOT parse this output back with toCents().
     */
    public static String fromCentsDisplay(long cents) {
        return String.format(Locale.getDefault(), "%.2f", cents / 100.0);
    }

    /**
     * For UI display with currency symbol.
     * e.g. 1299L → "NT$12.99"
     */
    public static String fromCentsDisplay(long cents, String currencySymbol) {
        return currencySymbol + fromCentsDisplay(cents);
    }

    /**
     *
     * Converts cents to a currency-prefixed display string.
     * e.g. 1299L → "$12.99"
     */
    public static String fromCentsFormatted(long cents, String currencySymbol) {
        return currencySymbol + fromCentsRaw(cents);
    }

    /**
     * Validates whether a string is a valid monetary input.
     * Allows: "12", "12.9", "12.99" — rejects multiple dots, letters, negatives.
     */
    public static boolean isValidMoneyInput(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String cleaned = input.trim().replaceAll(SIGNIFICANT_DIGITS_REGEX, "");
        // must be a valid number with at most one dot and at most 2 decimal places
        return cleaned.matches(TWO_DEC_PLACES_REGEX);
    }

    /**
     * Safe toCents — returns null if input is invalid instead of 0.
     * Useful when you need to distinguish between "user typed 0" and "user typed nothing".
     */
    @Nullable
    public static Long toCentsOrNull(String input) {
        if (!isValidMoneyInput(input)) return null;
        return toCents(input);
    }
}