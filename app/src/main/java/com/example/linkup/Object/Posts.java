package com.example.linkup.Object;

public class Posts {

    String UID,postURL,date,time,type,description;

    public Posts() {
    }

    public Posts(String UID, String postURL, String date, String time, String type, String description) {
        this.UID = UID;
        this.postURL = postURL;
        this.date = date;
        this.time = time;
        this.type = type;
        this.description = description;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getPostURL() {
        return postURL;
    }

    public void setPostURL(String postURL) {
        this.postURL = postURL;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
