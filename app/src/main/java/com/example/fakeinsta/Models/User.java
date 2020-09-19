package com.example.fakeinsta.Models;

public class User {
    private String imageUrl, fullname, username, id, bio;

    public User() {}

    public User(String imageUrl, String fullname, String username, String id, String bio) {
        this.imageUrl = imageUrl;
        this.fullname = fullname;
        this.username = username;
        this.id = id;
        this.bio = bio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}