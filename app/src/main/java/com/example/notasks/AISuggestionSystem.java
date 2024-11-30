package com.example.notasks;

import java.util.Date;
import java.util.List;

public class AISuggestionSystem {

    public static Task.Priority suggestPriority(Task task, List<Task> userTasks) {
        // TODO: Implement AI logic to suggest priority based on task details and user history
        return Task.Priority.MEDIUM;
    }

    public static Date suggestDeadline(Task task, List<Task> userTasks) {
        // TODO: Implement AI logic to suggest deadline based on task complexity and user performance
        return new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000); // 1 week from now
    }

    public static List<String> generateInsights(List<Task> userTasks) {
        // TODO: Implement AI logic to generate insights based on user's task management patterns
        return List.of(
            "You complete 80% of your high-priority tasks on time.",
            "Your productivity peaks on Wednesdays.",
            "Consider breaking down large tasks into smaller subtasks for better management."
        );
    }
}

