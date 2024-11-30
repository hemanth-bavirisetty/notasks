package com.example.notasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskManager.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DESCRIPTION = "description";
    private static final String COLUMN_TASK_DEADLINE = "deadline";
    private static final String COLUMN_TASK_PRIORITY = "priority";
    private static final String COLUMN_TASK_STATUS = "status";
    private static final String COLUMN_TASK_USER_ID = "user_id";
    private static final String COLUMN_TASK_GOOGLE_CALENDAR_EVENT_ID = "google_calendar_event_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TASK_TITLE + " TEXT,"
                + COLUMN_TASK_DESCRIPTION + " TEXT,"
                + COLUMN_TASK_DEADLINE + " TEXT,"
                + COLUMN_TASK_PRIORITY + " TEXT,"
                + COLUMN_TASK_STATUS + " TEXT,"
                + COLUMN_TASK_USER_ID + " INTEGER,"
                + COLUMN_TASK_GOOGLE_CALENDAR_EVENT_ID + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD},
                COLUMN_USER_EMAIL + "=?", new String[]{email}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            cursor.close();
        }
        return user;
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DEADLINE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(task.getDeadline()));
        values.put(COLUMN_TASK_PRIORITY, task.getPriority().toString());
        values.put(COLUMN_TASK_STATUS, task.getStatus().toString());
        values.put(COLUMN_TASK_USER_ID, Long.parseLong(task.getAssignedUserId()));
        values.put(COLUMN_TASK_GOOGLE_CALENDAR_EVENT_ID, task.getGoogleCalendarEventId());
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public List<Task> getUserTasks(long userId) {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_USER_ID + " = " + userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setDescription(cursor.getString(2));
                try {
                    task.setDeadline(dateFormat.parse(cursor.getString(3)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                task.setPriority(Task.Priority.valueOf(cursor.getString(4)));
                task.setStatus(Task.Status.valueOf(cursor.getString(5)));
                task.setAssignedUserId(String.valueOf(cursor.getLong(6)));
                task.setGoogleCalendarEventId(cursor.getString(7));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DEADLINE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(task.getDeadline()));
        values.put(COLUMN_TASK_PRIORITY, task.getPriority().toString());
        values.put(COLUMN_TASK_STATUS, task.getStatus().toString());
        values.put(COLUMN_TASK_GOOGLE_CALENDAR_EVENT_ID, task.getGoogleCalendarEventId());
        return db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
        db.close();
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        try {
            // Query the users table to find a matching email/password combination
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD},
                    COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                    new String[]{email, password},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User(
                    cursor.getLong(0),    // id
                    cursor.getString(1),   // name
                    cursor.getString(2),   // email
                    cursor.getString(3)    // password
                );
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        
        return user;
    }

    public long createUser(String email, String password, String name) {
        User user = new User(name, email, password);
        return addUser(user);
    }
}
