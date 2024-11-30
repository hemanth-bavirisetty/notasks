package com.example.notasks;

import android.content.Context;
import android.content.SharedPreferences;

public class GamificationManager {

    private static final String PREF_NAME = "GamificationPrefs";
    private static final String KEY_POINTS = "points";
    private static final String KEY_STREAK = "streak";

    private SharedPreferences prefs;

    public GamificationManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addPoints(int points) {
        int currentPoints = getPoints();
        prefs.edit().putInt(KEY_POINTS, currentPoints + points).apply();
    }

    public int getPoints() {
        return prefs.getInt(KEY_POINTS, 0);
    }

    public void incrementStreak() {
        int currentStreak = getStreak();
        prefs.edit().putInt(KEY_STREAK, currentStreak + 1).apply();
    }

    public void resetStreak() {
        prefs.edit().putInt(KEY_STREAK, 0).apply();
    }

    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public void taskCompleted(Task task) {
        // Award points based on task priority
        switch (task.getPriority()) {
            case HIGH:
                addPoints(100);
                break;
            case MEDIUM:
                addPoints(50);
                break;
            case LOW:
                addPoints(25);
                break;
        }

        // Increment streak
        incrementStreak();
    }

    public String getNextAchievement() {
        int points = getPoints();
        if (points < 1000) {
            return "Reach 1000 points";
        } else if (points < 5000) {
            return "Reach 5000 points";
        } else {
            return "Maintain a 7-day streak";
        }
    }
}

