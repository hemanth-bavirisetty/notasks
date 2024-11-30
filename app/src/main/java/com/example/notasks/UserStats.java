package com.example.notasks;

public class UserStats {
    private int points;
    private int tasksCompleted;
    private int streak;

    public UserStats() {
        this.points = 0;
        this.tasksCompleted = 0;
        this.streak = 0;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }
}

