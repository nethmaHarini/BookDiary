package me.nethma.bookdiary.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * WorkManager Worker that fires the daily reading reminder notification.
 */
public class ReadingReminderWorker extends Worker {

    public ReadingReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper.sendReadingReminderNotification(getApplicationContext());
        return Result.success();
    }
}

