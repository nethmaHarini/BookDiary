package me.nethma.bookdiary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages user login session using SharedPreferences.
 * Call saveSession() on login, clearSession() on logout,
 * and isLoggedIn() to check at startup.
 */
public class SessionManager {

    private static final String PREF_NAME         = "bookdiary_session";
    private static final String KEY_LOGGED_IN     = "is_logged_in";
    private static final String KEY_USER_ID       = "user_id";
    private static final String KEY_USERNAME      = "username";
    private static final String KEY_EMAIL         = "email";
    private static final String KEY_PHOTO_URL     = "photo_url";
    private static final String KEY_TOPICS_DONE   = "topics_selected";
    private static final String KEY_TOPICS        = "selected_topics";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Save session after successful login */
    public void saveSession(int userId, String username, String email, String photoUrl) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PHOTO_URL, photoUrl != null ? photoUrl : "")
                .apply();
    }

    /** Clear session on logout */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /** Check if a user is currently logged in */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public int getUserId()      { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getEmail()    { return prefs.getString(KEY_EMAIL, ""); }
    public String getPhotoUrl() { return prefs.getString(KEY_PHOTO_URL, ""); }

    /** Save the topics the user selected during onboarding */
    public void saveTopics(List<String> topics) {
        String joined = String.join(",", topics);
        prefs.edit()
                .putBoolean(KEY_TOPICS_DONE, true)
                .putString(KEY_TOPICS, joined)
                .apply();
    }

    /** Returns true if the user has already completed topic selection */
    public boolean hasSelectedTopics() {
        return prefs.getBoolean(KEY_TOPICS_DONE, false);
    }

    /** Returns the list of selected topics, or empty list if none saved */
    public List<String> getSelectedTopics() {
        String raw = prefs.getString(KEY_TOPICS, "");
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }
}

