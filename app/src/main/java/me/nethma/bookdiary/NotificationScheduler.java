package me.nethma.bookdiary;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Central place to schedule / cancel all periodic WorkManager tasks for notifications.
 */
public class NotificationScheduler {

    private static final String WORK_READING_REMINDER  = "work_reading_reminder";
    private static final String WORK_RECOMMENDATION    = "work_recommendation";
    private static final String WORK_DAILY_QUOTE       = "work_daily_quote";

    /** Call this once at login / app start and from BootReceiver. */
    public static void scheduleAll(Context context) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        scheduleReadingReminder(context, prefs.isReadingRemindersEnabled());
        scheduleRecommendations(context, prefs.isNewRecommendationsEnabled());
        scheduleDailyQuote(context, prefs.isDailyQuotesEnabled());
    }

    public static void scheduleReadingReminder(Context context, boolean enable) {
        WorkManager wm = WorkManager.getInstance(context);
        if (enable) {
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    ReadingReminderWorker.class, 1, TimeUnit.DAYS)
                    .setInitialDelay(computeInitialDelay(context), TimeUnit.MILLISECONDS)
                    .build();
            wm.enqueueUniquePeriodicWork(
                    WORK_READING_REMINDER,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    req);
        } else {
            wm.cancelUniqueWork(WORK_READING_REMINDER);
        }
    }

    public static void scheduleRecommendations(Context context, boolean enable) {
        WorkManager wm = WorkManager.getInstance(context);
        if (enable) {
            // Fire roughly every 3 days
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    RecommendationWorker.class, 3, TimeUnit.DAYS)
                    .build();
            wm.enqueueUniquePeriodicWork(
                    WORK_RECOMMENDATION,
                    ExistingPeriodicWorkPolicy.KEEP,
                    req);
        } else {
            wm.cancelUniqueWork(WORK_RECOMMENDATION);
        }
    }

    public static void scheduleDailyQuote(Context context, boolean enable) {
        WorkManager wm = WorkManager.getInstance(context);
        if (enable) {
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    DailyQuoteWorker.class, 1, TimeUnit.DAYS)
                    .build();
            wm.enqueueUniquePeriodicWork(
                    WORK_DAILY_QUOTE,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    req);
        } else {
            wm.cancelUniqueWork(WORK_DAILY_QUOTE);
        }
    }

    /**
     * Returns delay in milliseconds until the user's preferred reading hour today (or tomorrow).
     */
    private static long computeInitialDelay(Context context) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        int targetHour = prefs.getReadingReminderHour();

        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar target = (java.util.Calendar) now.clone();
        target.set(java.util.Calendar.HOUR_OF_DAY, targetHour);
        target.set(java.util.Calendar.MINUTE, 0);
        target.set(java.util.Calendar.SECOND, 0);
        target.set(java.util.Calendar.MILLISECOND, 0);

        if (target.before(now)) {
            // Already past today's time — schedule for tomorrow
            target.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}

