package me.nethma.bookdiary.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages all notification-related preferences using SharedPreferences.
 */
public class NotificationPrefsManager {

    private static final String PREF_NAME = "bookdiary_notification_prefs";

    // Keys
    public static final String KEY_NEW_RECOMMENDATIONS = "notif_new_recommendations";
    public static final String KEY_READING_REMINDERS   = "notif_reading_reminders";
    public static final String KEY_DAILY_QUOTES        = "notif_daily_quotes";
    public static final String KEY_APP_UPDATES         = "notif_app_updates";
    public static final String KEY_READING_TIME        = "notif_reading_time"; // hour of day (0-23)

    private final SharedPreferences prefs;

    public NotificationPrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNewRecommendationsEnabled() {
        return prefs.getBoolean(KEY_NEW_RECOMMENDATIONS, true);
    }

    public void setNewRecommendationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NEW_RECOMMENDATIONS, enabled).apply();
    }

    public boolean isReadingRemindersEnabled() {
        return prefs.getBoolean(KEY_READING_REMINDERS, true);
    }

    public void setReadingRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_READING_REMINDERS, enabled).apply();
    }

    public boolean isDailyQuotesEnabled() {
        return prefs.getBoolean(KEY_DAILY_QUOTES, false);
    }

    public void setDailyQuotesEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DAILY_QUOTES, enabled).apply();
    }

    public boolean isAppUpdatesEnabled() {
        return prefs.getBoolean(KEY_APP_UPDATES, true);
    }

    public void setAppUpdatesEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_APP_UPDATES, enabled).apply();
    }

    /** Returns the scheduled reading reminder hour (default 20 = 8 PM). */
    public int getReadingReminderHour() {
        return prefs.getInt(KEY_READING_TIME, 20);
    }

    public void setReadingReminderHour(int hour) {
        prefs.edit().putInt(KEY_READING_TIME, hour).apply();
    }
}

