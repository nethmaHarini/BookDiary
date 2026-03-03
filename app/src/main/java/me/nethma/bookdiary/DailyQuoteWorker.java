package me.nethma.bookdiary;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * WorkManager Worker that fires the daily inspirational quote notification.
 */
public class DailyQuoteWorker extends Worker {

    public DailyQuoteWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper.sendDailyQuoteNotification(getApplicationContext());
        return Result.success();
    }
}

