package me.nethma.bookdiary.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import me.nethma.bookdiary.MainActivity;
import me.nethma.bookdiary.R;

/**
 * Centralised helper for creating notification channels and posting notifications.
 */
public class NotificationHelper {

    // Channel IDs
    public static final String CHANNEL_RECOMMENDATIONS = "channel_recommendations";
    public static final String CHANNEL_REMINDERS       = "channel_reminders";
    public static final String CHANNEL_QUOTES          = "channel_quotes";
    public static final String CHANNEL_UPDATES         = "channel_updates";

    // Notification IDs
    public static final int NOTIF_ID_RECOMMENDATION = 1001;
    public static final int NOTIF_ID_REMINDER       = 1002;
    public static final int NOTIF_ID_QUOTE          = 1003;
    public static final int NOTIF_ID_UPDATE         = 1004;

    /** Call once at app startup (e.g., in MainActivity or Application class) */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);

            NotificationChannel chRec = new NotificationChannel(
                    CHANNEL_RECOMMENDATIONS,
                    "New Recommendations",
                    NotificationManager.IMPORTANCE_DEFAULT);
            chRec.setDescription("Book recommendations based on your reading taste");
            nm.createNotificationChannel(chRec);

            NotificationChannel chRem = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Reading Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            chRem.setDescription("Daily reminders to keep up your reading streak");
            nm.createNotificationChannel(chRem);

            NotificationChannel chQ = new NotificationChannel(
                    CHANNEL_QUOTES,
                    "Daily Quotes",
                    NotificationManager.IMPORTANCE_LOW);
            chQ.setDescription("Inspiring literary quotes delivered daily");
            nm.createNotificationChannel(chQ);

            NotificationChannel chUpd = new NotificationChannel(
                    CHANNEL_UPDATES,
                    "App Updates",
                    NotificationManager.IMPORTANCE_LOW);
            chUpd.setDescription("News about new features and improvements");
            nm.createNotificationChannel(chUpd);
        }
    }

    /** Post a new recommendation notification */
    public static void sendRecommendationNotification(Context context) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        if (!prefs.isNewRecommendationsEnabled()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RECOMMENDATIONS)
                .setSmallIcon(R.drawable.ic_book_logo)
                .setContentTitle("📚 New Book Recommendations")
                .setContentText("We found new books that match your reading taste! Check them out.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("We found new books that match your reading taste! Head to the Home screen to see your personalised picks."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_RECOMMENDATION, builder.build());
        } catch (SecurityException e) {
            // Permission not granted — silently skip
        }
    }

    /** Post a daily reading reminder notification */
    public static void sendReadingReminderNotification(Context context) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        if (!prefs.isReadingRemindersEnabled()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_book_logo)
                .setContentTitle("📖 Time to Read!")
                .setContentText("Don't break your reading streak — open BookDiary and log today's session.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't break your reading streak! Open BookDiary, pick up where you left off, and keep your literary journey going strong."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_REMINDER, builder.build());
        } catch (SecurityException e) {
            // Permission not granted — silently skip
        }
    }

    /** Post a daily quote notification */
    public static void sendDailyQuoteNotification(Context context) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        if (!prefs.isDailyQuotesEnabled()) return;

        String[] quotes = {
                "\"A reader lives a thousand lives before he dies.\" — George R.R. Martin",
                "\"Not all those who wander are lost.\" — J.R.R. Tolkien",
                "\"It is never too late to be what you might have been.\" — George Eliot",
                "\"So many books, so little time.\" — Frank Zappa",
                "\"One must always be careful of books.\" — Cassandra Clare",
                "\"A book is a dream that you hold in your hand.\" — Neil Gaiman",
                "\"Reading is to the mind what exercise is to the body.\" — Joseph Addison",
                "\"Books are a uniquely portable magic.\" — Stephen King",
                "\"The more you read, the more things you will know.\" — Dr. Seuss",
                "\"Today a reader, tomorrow a leader.\" — Margaret Fuller"
        };
        int idx = (int) (System.currentTimeMillis() / 86400000L % quotes.length);
        String quote = quotes[idx];

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_QUOTES)
                .setSmallIcon(R.drawable.ic_book_logo)
                .setContentTitle("✨ Literary Quote of the Day")
                .setContentText(quote)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(quote))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_QUOTE, builder.build());
        } catch (SecurityException e) {
            // Permission not granted — silently skip
        }
    }

    /** Post an app update notification */
    public static void sendAppUpdateNotification(Context context, String updateText) {
        NotificationPrefsManager prefs = new NotificationPrefsManager(context);
        if (!prefs.isAppUpdatesEnabled()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 3, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_UPDATES)
                .setSmallIcon(R.drawable.ic_book_logo)
                .setContentTitle("🚀 BookDiary Update")
                .setContentText(updateText != null ? updateText : "New features are available. Tap to explore!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_UPDATE, builder.build());
        } catch (SecurityException e) {
            // Permission not granted — silently skip
        }
    }
}

