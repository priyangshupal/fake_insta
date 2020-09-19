package com.example.fakeinsta.Models;

public class Story {
    private String imageUrl, storyid, userid;
    private long timestart, timeend;

    public Story() {}

    public Story(String imageUrl, String storyid, String userid, long timestart, long timeend) {
        this.imageUrl = imageUrl;
        this.storyid = storyid;
        this.userid = userid;
        this.timestart = timestart;
        this.timeend = timeend;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getTimestart() {
        return timestart;
    }

    public void setTimestart(long timestart) {
        this.timestart = timestart;
    }

    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {
        this.timeend = timeend;
    }
}
