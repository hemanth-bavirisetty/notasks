package com.example.notasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {
    private final Context context;
    private final DatabaseHelper databaseHelper;

    public ReminderWorker(
        @NonNull Context context,
        @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get upcoming tasks
        List<Task> upcomingTasks = getUpcomingTasks();
        
        for (Task task : upcomingTasks) {
            // Show notification for each upcoming task
            String title = "Upcoming Task Reminder";
            String content = "Task \"" + task.getTitle() + "\" is due " + 
                           getRelativeDeadline(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                               .format(task.getDeadline()));
            
            NotificationUtils.showTaskNotification(context, title, content);
        }

        return Result.success();
    }

    private List<Task> getUpcomingTasks() {
        List<Task> upcomingTasks = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Get tasks due in the next 24 hours
        String[] columns = {
            "id", "title", "description", "deadline", "priority", "status"
        };
        
        String selection = "status != ? AND deadline BETWEEN ? AND ?";
        String[] selectionArgs = {
            Task.Status.COMPLETED.toString(),
            getCurrentTimestamp(),
            getTimestampPlusHours(24)
        };

        Cursor cursor = db.query("tasks", columns, selection, selectionArgs,
                null, null, "deadline ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex("id")));
                task.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                task.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                
                // Parse the deadline string to Date
                try {
                    String deadlineStr = cursor.getString(cursor.getColumnIndex("deadline"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date deadline = dateFormat.parse(deadlineStr);
                    task.setDeadline(deadline);
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue; // Skip this task if date parsing fails
                }
                
                task.setPriority(Task.Priority.valueOf(
                    cursor.getString(cursor.getColumnIndex("priority"))
                ));
                task.setStatus(Task.Status.valueOf(
                    cursor.getString(cursor.getColumnIndex("status"))
                ));
                upcomingTasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return upcomingTasks;
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(new Date());
    }

    private String getTimestampPlusHours(int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(calendar.getTime());
    }

    private String getRelativeDeadline(String deadline) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date deadlineDate = sdf.parse(deadline);
            Date now = new Date();

            long diffInMillis = deadlineDate.getTime() - now.getTime();
            long diffHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            if (diffHours < 1) {
                return "in less than an hour";
            } else if (diffHours == 1) {
                return "in 1 hour";
            } else if (diffHours < 24) {
                return "in " + diffHours + " hours";
            } else {
                return "tomorrow";
            }
        } catch (ParseException e) {
            return "soon";
        }
    }
}

