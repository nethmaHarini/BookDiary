package me.nethma.bookdiary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persists in-app notification history in SharedPreferences as a JSON array.
 * Max 50 entries are kept (oldest trimmed).
 */
public class NotificationStore {

    private static final String PREF_NAME   = "bookdiary_notif_store";
    private static final String KEY_NOTIFS  = "notifications";
    private static final int    MAX_ENTRIES = 50;

    private final SharedPreferences prefs;

    public NotificationStore(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public void addNotification(NotificationItem.Type type, String title, String message) {
        List<NotificationItem> items = loadAll();
        String id = UUID.randomUUID().toString();
        items.add(0, new NotificationItem(id, type, title, message,
                System.currentTimeMillis(), false));
        // Trim to max
        if (items.size() > MAX_ENTRIES) items = items.subList(0, MAX_ENTRIES);
        saveAll(items);
    }

    public void markAllRead() {
        List<NotificationItem> items = loadAll();
        for (NotificationItem item : items) item.setRead(true);
        saveAll(items);
    }

    public void markRead(String id) {
        List<NotificationItem> items = loadAll();
        for (NotificationItem item : items) {
            if (item.getId().equals(id)) { item.setRead(true); break; }
        }
        saveAll(items);
    }

    public void clearAll() {
        prefs.edit().remove(KEY_NOTIFS).apply();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<NotificationItem> loadAll() {
        List<NotificationItem> result = new ArrayList<>();
        String json = prefs.getString(KEY_NOTIFS, null);
        if (json == null) return result;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                NotificationItem item = new NotificationItem(
                        obj.getString("id"),
                        NotificationItem.Type.valueOf(obj.getString("type")),
                        obj.getString("title"),
                        obj.getString("message"),
                        obj.getLong("timestamp"),
                        obj.getBoolean("read")
                );
                result.add(item);
            }
        } catch (Exception e) {
            // Corrupted — reset
            prefs.edit().remove(KEY_NOTIFS).apply();
        }
        return result;
    }

    public int getUnreadCount() {
        int count = 0;
        for (NotificationItem item : loadAll()) {
            if (!item.isRead()) count++;
        }
        return count;
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    private void saveAll(List<NotificationItem> items) {
        try {
            JSONArray arr = new JSONArray();
            for (NotificationItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("id",        item.getId());
                obj.put("type",      item.getType().name());
                obj.put("title",     item.getTitle());
                obj.put("message",   item.getMessage());
                obj.put("timestamp", item.getTimestamp());
                obj.put("read",      item.isRead());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_NOTIFS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Seed helpers (called by workers) ──────────────────────────────────────

    public static void seedRecommendation(Context context) {
        new NotificationStore(context).addNotification(
                NotificationItem.Type.RECOMMENDATION,
                "📚 New Book Recommendations",
                "We found new books that match your reading taste! Head to Home to see your personalised picks."
        );
    }

    public static void seedReminder(Context context) {
        new NotificationStore(context).addNotification(
                NotificationItem.Type.REMINDER,
                "📖 Time to Read!",
                "Don't break your reading streak — open BookDiary and log today's session."
        );
    }

    public static void seedQuote(Context context) {
        String[] quotes = {
                "\"A reader lives a thousand lives before he dies.\" — George R.R. Martin",
                "\"So many books, so little time.\" — Frank Zappa",
                "\"A book is a dream that you hold in your hand.\" — Neil Gaiman",
                "\"Reading is to the mind what exercise is to the body.\" — Joseph Addison",
                "\"Books are a uniquely portable magic.\" — Stephen King",
                "\"Today a reader, tomorrow a leader.\" — Margaret Fuller",
                "\"Not all those who wander are lost.\" — J.R.R. Tolkien",
                "\"It is never too late to be what you might have been.\" — George Eliot",
                "\"The more you read, the more things you will know.\" — Dr. Seuss",
                "\"One must always be careful of books.\" — Cassandra Clare"
        };
        int idx = (int) (System.currentTimeMillis() / 86_400_000L % quotes.length);
        new NotificationStore(context).addNotification(
                NotificationItem.Type.QUOTE,
                "✨ Literary Quote of the Day",
                quotes[idx]
        );
    }

    public static void seedUpdate(Context context, String message) {
        new NotificationStore(context).addNotification(
                NotificationItem.Type.UPDATE,
                "🚀 BookDiary Update",
                message != null ? message : "New features are available. Tap to explore!"
        );
    }
}

