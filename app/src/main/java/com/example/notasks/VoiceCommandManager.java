package com.example.notasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceCommandManager {
    private final Context context;
    private final SpeechRecognizer speechRecognizer;
    private final DatabaseHelper databaseHelper;
    private OnVoiceCommandListener listener;

    public interface OnVoiceCommandListener {
        void onTaskCreated(Task task);
        void onTaskUpdated(Task task);
        void onTaskCompleted(long taskId);
        void onError(String message);
    }

    public VoiceCommandManager(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        setupSpeechRecognizer();
    }

    public void setOnVoiceCommandListener(OnVoiceCommandListener listener) {
        this.listener = listener;
    }

    private void setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "Audio recording error";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "Client side error";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "Insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "Network error";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "Network timeout";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "No match found";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "Recognition service busy";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "Server error";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "No speech input";
                        break;
                    default:
                        message = "Unknown error";
                        break;
                }
                if (listener != null) {
                    listener.onError(message);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                );
                if (matches != null && !matches.isEmpty()) {
                    processVoiceCommand(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command...");

        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public void destroy() {
        speechRecognizer.destroy();
    }

    private void processVoiceCommand(String command) {
        command = command.toLowerCase();

        // Create task command
        Pattern createPattern = Pattern.compile(
                "create (task|new task) (called |titled |named )?(.+?) (due|by) (.+)"
        );
        Matcher createMatcher = createPattern.matcher(command);

        // Complete task command
        Pattern completePattern = Pattern.compile(
                "complete (task|the task) (called |titled |named )?(.+)"
        );
        Matcher completeMatcher = completePattern.matcher(command);

        if (createMatcher.find()) {
            String title = createMatcher.group(3);
            String deadlineStr = createMatcher.group(5);

            try {
                Date deadline = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(parseDateFromVoice(deadlineStr));
                Task task = new Task();
                task.setTitle(title);
                task.setStatus(Task.Status.YET_TO_START);
                task.setPriority(Task.Priority.MEDIUM); // Default priority
                task.setDeadline(deadline);

                long taskId = databaseHelper.addTask(task);
                if (taskId != -1) {
                    task.setId((int) taskId);
                    if (listener != null) {
                        listener.onTaskCreated(task);
                    }
                }
            } catch (ParseException e) {
                if (listener != null) {
                    listener.onError("Could not understand the deadline. Please try again.");
                }
            }
        } else if (completeMatcher.find()) {
            String title = completeMatcher.group(3);
            Task task = findTaskByTitle(title);

            if (task != null) {
                task.setStatus(Task.Status.COMPLETED);
                int updated = databaseHelper.updateTask(task);
                if (updated > 0 && listener != null) {
                    listener.onTaskCompleted(task.getId());
                }
            } else if (listener != null) {
                listener.onError("Could not find a task with that title.");
            }
        } else if (listener != null) {
            listener.onError("Command not recognized. Please try again.");
        }
    }

    private String parseDateFromVoice(String dateStr) throws ParseException {
        dateStr = dateStr.toLowerCase();
        Calendar calendar = Calendar.getInstance();

        if (dateStr.contains("today")) {
            // Use current date
        } else if (dateStr.contains("tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } else if (dateStr.contains("next week")) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        } else {
            // Try to parse specific date
            SimpleDateFormat[] formats = {
                    new SimpleDateFormat("MMMM d", Locale.getDefault()),
                    new SimpleDateFormat("MMMM d yyyy", Locale.getDefault()),
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("MM/dd", Locale.getDefault())
            };

            Date parsedDate = null;
            for (SimpleDateFormat format : formats) {
                try {
                    parsedDate = format.parse(dateStr);
                    break;
                } catch (ParseException e) {
                    // Try next format
                }
            }

            if (parsedDate != null) {
                calendar.setTime(parsedDate);
                // If year wasn't specified, use current year
                if (dateStr.matches(".*(\\d{4}).*")) {
                    calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                }
            } else {
                throw new ParseException("Could not parse date: " + dateStr, 0);
            }
        }

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(calendar.getTime());
    }

    private Task findTaskByTitle(String title) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Task task = null;

        String[] columns = {
                "id", "title", "description", "deadline", "priority", "status"
        };
        String selection = "LOWER(title) LIKE ?";
        String[] selectionArgs = {"%" + title.toLowerCase() + "%"};

        Cursor cursor = db.query("tasks", columns, selection, selectionArgs,
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                task = new Task();
                task.setId((int) cursor.getLong(cursor.getColumnIndex("id")));
                task.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                task.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                
                // Parse the deadline string to Date
                String deadlineStr = cursor.getString(cursor.getColumnIndex("deadline"));
                Date deadline = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(deadlineStr);
                task.setDeadline(deadline);
                
                task.setPriority(Task.Priority.valueOf(
                        cursor.getString(cursor.getColumnIndex("priority"))
                ));
                task.setStatus(Task.Status.valueOf(
                        cursor.getString(cursor.getColumnIndex("status"))
                ));
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        db.close();
        return task;
    }
}

