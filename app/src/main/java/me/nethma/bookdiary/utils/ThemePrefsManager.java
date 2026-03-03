package me.nethma.bookdiary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages theme mode (Light / Dark / System) and accent colour preferences.
 */
public class ThemePrefsManager {

    private static final String PREF_NAME     = "bookdiary_theme_prefs";
    private static final String KEY_THEME_MODE  = "theme_mode";
    private static final String KEY_ACCENT_COLOR = "accent_color";

    // Theme mode constants
    public static final int MODE_LIGHT  = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_DARK   = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    // Accent colour constants (hex int values)
    public static final int ACCENT_OCEAN_BLUE    = 0xFF1152D4;
    public static final int ACCENT_ROYAL_PURPLE  = 0xFF7C3AED;
    public static final int ACCENT_EMERALD       = 0xFF10B981;
    public static final int ACCENT_SUNSET        = 0xFFF97316;

    private final SharedPreferences prefs;

    public ThemePrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Returns saved AppCompatDelegate night mode (default: MODE_SYSTEM = follow device). */
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    /** Returns saved accent colour as ARGB int (default: Ocean Blue). */
    public int getAccentColor() {
        return prefs.getInt(KEY_ACCENT_COLOR, ACCENT_OCEAN_BLUE);
    }

    public void setAccentColor(int color) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply();
    }

    /** Apply the saved night mode immediately — call once at app startup. */
    public static void applyTheme(Context context) {
        int mode = new ThemePrefsManager(context).getThemeMode();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /** Human-readable label for the given mode. */
    public static String modeLabel(int mode) {
        if (mode == MODE_LIGHT)  return "Light";
        if (mode == MODE_DARK)   return "Dark";
        return "System Default";
    }
}


