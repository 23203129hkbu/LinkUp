package com.example.linkup.Object;

// ✅
public class Posts {

    String postID, UID, postURL, date, type, description;

    public Posts() {
    }

    public Posts(String postID, String UID, String postURL, String date, String type, String description) {
        this.postID = postID;
        this.UID = UID;
        this.postURL = postURL;
        this.date = date;
        this.type = type;
        this.description = description;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
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
