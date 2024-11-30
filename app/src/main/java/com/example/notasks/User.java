package com.example.notasks;

public class User {
    private long id;
    private String email;
    private String name;
    private String profileImage;
    private String password;

    public User() {
    }

    public User(long id, String email, String name, String profileImage) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
    }

    public User(long id, String email, String name, String profileImage, String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.password = password;
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}

