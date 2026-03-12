package me.nethma.bookdiary.utils;

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
        Context ctx = getApplicationContext();
        NotificationHelper.sendDailyQuoteNotification(ctx);
        NotificationStore.seedQuote(ctx);
        return Result.success();
    }
}

