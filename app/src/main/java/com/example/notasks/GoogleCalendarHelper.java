package com.example.notasks;

import android.content.Context;
import android.os.AsyncTask;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarHelper {

    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    private Context context;
    private GoogleAccountCredential credential;
    private Calendar service;

    public GoogleCalendarHelper(Context context, String accountName) {
        this.context = context;
        credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(accountName);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        service = new Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("TaskManager")
                .build();
    }

    public void addTaskToCalendar(final Task task) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Event event = new Event()
                            .setSummary(task.getTitle())
                            .setDescription(task.getDescription());

                    DateTime startDateTime = new DateTime(task.getDeadline());
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime);
                    event.setStart(start);

                    DateTime endDateTime = new DateTime(task.getDeadline());
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime);
                    event.setEnd(end);

                    event = service.events().insert("primary", event).execute();
                    return event.getId();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String eventId) {
                if (eventId != null) {
                    task.setGoogleCalendarEventId(eventId);
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    dbHelper.updateTask(task);
                }
            }
        }.execute();
    }

    public void updateTaskInCalendar(final Task task) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Event event = service.events().get("primary", task.getGoogleCalendarEventId()).execute();
                    event.setSummary(task.getTitle())
                            .setDescription(task.getDescription());

                    DateTime startDateTime = new DateTime(task.getDeadline());
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime);
                    event.setStart(start);

                    DateTime endDateTime = new DateTime(task.getDeadline());
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime);
                    event.setEnd(end);

                    service.events().update("primary", event.getId(), event).execute();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }.execute();
    }

    public void deleteTaskFromCalendar(final String eventId) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    service.events().delete("primary", eventId).execute();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }.execute();
    }

    public interface CalendarEventsCallback {
        void onEventsLoaded(List<Event> events);
    }

    public void getCalendarEvents(final CalendarEventsCallback callback) {
        new AsyncTask<Void, Void, List<Event>>() {
            @Override
            protected List<Event> doInBackground(Void... params) {
                try {
                    DateTime now = new DateTime(System.currentTimeMillis());
                    List<Event> events = service.events().list("primary")
                            .setMaxResults(10)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()
                            .getItems();
                    return events;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Event> events) {
                if (callback != null) {
                    callback.onEventsLoaded(events);
                }
            }
        }.execute();
    }
}
