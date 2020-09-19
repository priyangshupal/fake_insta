package com.example.fakeinsta.Models;

public class Post {
    private String postid, description, postimage, publisher;

    public Post() {}

    public Post(String postid, String description, String postimage, String publisher) {
        this.postid = postid;
        this.description = description;
        this.postimage = postimage;
        this.publisher = publisher;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
