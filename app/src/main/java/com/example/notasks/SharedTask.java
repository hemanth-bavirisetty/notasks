package com.example.notasks;

public class SharedTask {
    private long id;
    private String title;
    private String description;
    private String sharedBy;
    private String sharedWith;

    // Constructor
    public SharedTask(long id, String title, String description, String sharedBy, String sharedWith) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sharedBy = sharedBy;
        this.sharedWith = sharedWith;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public String getSharedWith() {
        return sharedWith;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public void setSharedWith(String sharedWith) {
        this.sharedWith = sharedWith;
    }

    @Override
    public String toString() {
        return "SharedTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", sharedBy='" + sharedBy + '\'' +
                ", sharedWith='" + sharedWith + '\'' +
                '}';
    }
}

