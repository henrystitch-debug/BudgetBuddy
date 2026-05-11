package com.github.budgetbuddy.utils;

import android.graphics.Color;

public final class ColorUtils {
    private ColorUtils() {}

    public static final int FOOD      = Color.parseColor("#4A7C7C");
    public static final int HOME      = Color.parseColor("#5C85D6");
    public static final int TRANSPORT = Color.parseColor("#E8A838");
    public static final int SCHOOL    = Color.parseColor("#9C6EBA");
    public static final int HEALTH    = Color.parseColor("#E05252");
    public static final int SHOPPING  = Color.parseColor("#3BAE8A");
    public static final int FUN       = Color.parseColor("#F08040");
    public static final int OTHER     = Color.parseColor("#9E9E9E");
    public static final int TRAVEL    = Color.parseColor("#4A90C0");
    public static final int PET       = Color.parseColor("#88B948");

    // ── UI / Tab colors ────────────────────────────────────────────────────
    public static final int TAB_ACTIVE_BG      = Color.parseColor("#4A7C7C");
    public static final int TAB_INACTIVE_BG    = Color.parseColor("#F0F0F0");
    public static final int TAB_ACTIVE_TEXT    = Color.WHITE;
    public static final int TAB_INACTIVE_TEXT  = Color.parseColor("#888888");

    // ── Budget progress colors ─────────────────────────────────────────────
    public static final int PROGRESS_OK        = Color.parseColor("#4A7C7C");
    public static final int PROGRESS_WARNING   = Color.parseColor("#FFA726");
    public static final int PROGRESS_EXCEEDED  = Color.parseColor("#E53935");

    // ── Text colors ────────────────────────────────────────────────────────
    public static final int TEXT_PRIMARY       = Color.parseColor("#1A1A1A");
    public static final int TEXT_SECONDARY     = Color.parseColor("#444444");
    public static final int TEXT_HINT          = Color.parseColor("#888888");

    // ── Misc ───────────────────────────────────────────────────────────────
    public static final int PIE_HOLE           = Color.WHITE;
    public static final int CATEGORY_FALLBACK  = Color.parseColor("#CCCCCC");
}
