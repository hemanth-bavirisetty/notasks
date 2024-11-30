package com.example.notasks;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import javax.xml.transform.Result;
import java.text.ParseException;

public class DeadlineWorker extends Worker {
    private final Context context;
    private final DatabaseHelper databaseHelper;

    public DeadlineWorker(
        @NonNull Context context,
        @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get overdue tasks
        List<Task> overdueTasks = getOverdueTasks();
        
        for (Task task : overdueTasks) {
            // Show notification for each overdue task
            String title = "Task Overdue";
            String content = "Task \"" + task.getTitle() + "\" is now overdue!";
            
            NotificationUtils.showTaskNotification(context, title, content);
            
            // Update task status to overdue
            updateTaskStatus(task.getId(), Task.Status.ON_HOLD);
        }

        return Result.success();
    }

    private List<Task> getOverdueTasks() {
        List<Task> overdueTasks = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] columns = {
            "id", "title", "description", "deadline", "priority", "status"
        };
        
        String selection = "status = ? AND deadline < ?";
        String[] selectionArgs = {
            Task.Status.IN_PROGRESS.toString(),
            getCurrentTimestamp()
        };

        Cursor cursor = db.query("tasks", columns, selection, selectionArgs,
                null, null, "deadline ASC");

        if (cursor != null && cursor.moveToFirst()) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int descIndex = cursor.getColumnIndexOrThrow("description");
                int deadlineIndex = cursor.getColumnIndexOrThrow("deadline");
                int priorityIndex = cursor.getColumnIndexOrThrow("priority");
                int statusIndex = cursor.getColumnIndexOrThrow("status");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(idIndex));
                    task.setTitle(cursor.getString(titleIndex));
                    task.setDescription(cursor.getString(descIndex));
                    
                    // Parse deadline string to Date
                    String deadlineStr = cursor.getString(deadlineIndex);
                    try {
                        Date deadline = dateFormat.parse(deadlineStr);
                        task.setDeadline(deadline);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue; // Skip this task if date parsing fails
                    }

                    task.setPriority(Task.Priority.valueOf(
                        cursor.getString(priorityIndex)
                    ));
                    task.setStatus(Task.Status.valueOf(
                        cursor.getString(statusIndex)
                    ));
                    overdueTasks.add(task);
                } while (cursor.moveToNext());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        db.close();
        return overdueTasks;
    }

    private void updateTaskStatus(long taskId, Task.Status status) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status.toString());
        values.put("updated_at", getCurrentTimestamp());

        db.update("tasks", values, "id = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(new Date());
    }
}

