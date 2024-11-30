package com.example.notasks;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private int id;
    private String title;
    private String description;
    private Date deadline;
    private Priority priority;
    private Status status;

    private String assignedUserId;
    private String googleCalendarEventId;

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        YET_TO_START, IN_PROGRESS, COMPLETED, ON_HOLD
    }

    // Constructor
    public Task(int id, String title, String description, Date deadline,
                Priority priority, Status status, String assignedUserId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.assignedUserId = assignedUserId;
    }

    // Add default constructor
    public Task() {
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getGoogleCalendarEventId() {
        return googleCalendarEventId;
    }

    public void setGoogleCalendarEventId(String googleCalendarEventId) {
        this.googleCalendarEventId = googleCalendarEventId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", deadline=" + deadline +
                ", priority=" + priority +
                ", status=" + status +
                ", assignedUserId=" + assignedUserId +
                ", googleCalendarEventId=" + googleCalendarEventId +
                '}';
    }
}

