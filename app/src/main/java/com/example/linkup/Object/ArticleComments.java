package com.example.linkup.Object;

public class ArticleComments {
    String UID,username,comment,date,time,imageURL;

    public ArticleComments() {
    }

    public ArticleComments(String UID, String username, String comment, String date, String time, String imageURL) {
        this.UID = UID;
        this.username = username;
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.imageURL = imageURL;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }


}
