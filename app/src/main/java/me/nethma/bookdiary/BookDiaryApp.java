package me.nethma.bookdiary;

import android.app.Application;

import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * Custom Application class.
 * Called before any Activity is created — the perfect place to apply
 * the saved night mode so every screen reflects the user's theme choice.
 */
public class BookDiaryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply saved theme (Light / Dark / System) globally.
        // AppCompatDelegate.setDefaultNightMode() affects ALL activities.
        ThemePrefsManager.applyTheme(this);
    }
}

