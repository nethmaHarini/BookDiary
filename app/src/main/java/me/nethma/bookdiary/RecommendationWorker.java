package me.nethma.bookdiary;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * WorkManager Worker that fires the new book recommendations notification.
 */
public class RecommendationWorker extends Worker {

    public RecommendationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper.sendRecommendationNotification(getApplicationContext());
        return Result.success();
    }
}

