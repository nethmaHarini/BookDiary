package me.nethma.bookdiary;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import me.nethma.bookdiary.utils.NotificationHelper;
import me.nethma.bookdiary.utils.NotificationPrefsManager;
import me.nethma.bookdiary.utils.NotificationScheduler;

public class NotificationSettingsActivity extends AppCompatActivity {

    private NotificationPrefsManager prefsManager;

    private SwitchCompat switchRecommendations;
    private SwitchCompat switchReminders;
    private SwitchCompat switchDailyQuotes;
    private SwitchCompat switchAppUpdates;

    private View rowReminderTime;
    private TextView tvReminderTime;
    private View bannerPermission;

    // Android 13+ POST_NOTIFICATIONS permission launcher
    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    bannerPermission.setVisibility(View.GONE);
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                    NotificationScheduler.scheduleAll(this);
                } else {
                    Toast.makeText(this,
                            "Permission denied. Enable it from system settings.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Create notification channels (idempotent)
        NotificationHelper.createNotificationChannels(this);

        prefsManager = new NotificationPrefsManager(this);

        // --- View references ---
        switchRecommendations = findViewById(R.id.switch_recommendations);
        switchReminders       = findViewById(R.id.switch_reminders);
        switchDailyQuotes     = findViewById(R.id.switch_daily_quotes);
        switchAppUpdates      = findViewById(R.id.switch_app_updates);
        rowReminderTime       = findViewById(R.id.row_reminder_time);
        tvReminderTime        = findViewById(R.id.tv_reminder_time);
        bannerPermission      = findViewById(R.id.banner_permission);

        // --- Load saved preferences ---
        switchRecommendations.setChecked(prefsManager.isNewRecommendationsEnabled());
        switchReminders.setChecked(prefsManager.isReadingRemindersEnabled());
        switchDailyQuotes.setChecked(prefsManager.isDailyQuotesEnabled());
        switchAppUpdates.setChecked(prefsManager.isAppUpdatesEnabled());
        updateReminderTimeLabel(prefsManager.getReadingReminderHour());
        updateReminderTimeRowVisibility(prefsManager.isReadingRemindersEnabled());

        // --- Permission check ---
        checkNotificationPermission();

        // --- Listeners ---
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        switchRecommendations.setOnCheckedChangeListener((btn, checked) -> {
            prefsManager.setNewRecommendationsEnabled(checked);
            NotificationScheduler.scheduleRecommendations(this, checked);
            showToggleToast("New Recommendations", checked);
        });

        switchReminders.setOnCheckedChangeListener((btn, checked) -> {
            prefsManager.setReadingRemindersEnabled(checked);
            NotificationScheduler.scheduleReadingReminder(this, checked);
            updateReminderTimeRowVisibility(checked);
            showToggleToast("Reading Reminders", checked);
        });

        switchDailyQuotes.setOnCheckedChangeListener((btn, checked) -> {
            prefsManager.setDailyQuotesEnabled(checked);
            NotificationScheduler.scheduleDailyQuote(this, checked);
            showToggleToast("Daily Quotes", checked);
        });

        switchAppUpdates.setOnCheckedChangeListener((btn, checked) -> {
            prefsManager.setAppUpdatesEnabled(checked);
            showToggleToast("App Updates", checked);
        });

        rowReminderTime.setOnClickListener(v -> showTimePicker());

        // Test buttons
        findViewById(R.id.btn_test_reminder).setOnClickListener(v -> {
            if (!hasNotifPermission()) {
                requestNotifPermission();
                return;
            }
            NotificationHelper.sendReadingReminderNotification(this);
            Toast.makeText(this, "Reading reminder sent!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_test_recommendation).setOnClickListener(v -> {
            if (!hasNotifPermission()) {
                requestNotifPermission();
                return;
            }
            NotificationHelper.sendRecommendationNotification(this);
            Toast.makeText(this, "Recommendation notification sent!", Toast.LENGTH_SHORT).show();
        });

        // Permission banner enable button
        findViewById(R.id.btn_grant_permission).setOnClickListener(v -> requestNotifPermission());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            bannerPermission.setVisibility(granted ? View.GONE : View.VISIBLE);
        }
        // Below Android 13, permission is not needed at runtime
    }

    private boolean hasNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // User has denied before — open system settings
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void showTimePicker() {
        int currentHour = prefsManager.getReadingReminderHour();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            prefsManager.setReadingReminderHour(hourOfDay);
            updateReminderTimeLabel(hourOfDay);
            // Reschedule with new time
            if (prefsManager.isReadingRemindersEnabled()) {
                NotificationScheduler.scheduleReadingReminder(this, true);
            }
            Toast.makeText(this,
                    "Reminder set for " + formatHour(hourOfDay),
                    Toast.LENGTH_SHORT).show();
        }, currentHour, 0, false).show();
    }

    private void updateReminderTimeLabel(int hour) {
        tvReminderTime.setText(formatHour(hour));
    }

    private String formatHour(int hour) {
        String amPm = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%d:00 %s", displayHour, amPm);
    }

    private void updateReminderTimeRowVisibility(boolean remindersOn) {
        rowReminderTime.setVisibility(remindersOn ? View.VISIBLE : View.GONE);
    }

    private void showToggleToast(String name, boolean enabled) {
        Toast.makeText(this,
                name + (enabled ? " enabled" : " disabled"),
                Toast.LENGTH_SHORT).show();
    }
}




