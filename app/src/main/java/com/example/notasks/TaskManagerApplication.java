package com.example.notasks;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class TaskManagerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel
        NotificationUtils.createNotificationChannel(this);

        // Initialize WorkManager if notifications are enabled
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("notifications_enabled", true)) {
            initializeWorkManager();
        }
    }

    private void initializeWorkManager() {
        // Configure reminder work
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                1, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build();

        // Configure deadline work
        PeriodicWorkRequest deadlineWork = new PeriodicWorkRequest.Builder(
                DeadlineWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build();

        // Enqueue work requests
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "reminders",
                        ExistingPeriodicWorkPolicy.KEEP,
                        reminderWork);

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "deadlines",
                        ExistingPeriodicWorkPolicy.KEEP,
                        deadlineWork);
    }
}


