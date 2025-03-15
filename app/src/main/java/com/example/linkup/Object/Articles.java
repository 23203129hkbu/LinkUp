package com.example.linkup.Object;

public class Articles {
    String articleID,UID,username,privacy,headline,content,date,time,imageURL;

    public Articles() {
    }

    public Articles(String articleID, String UID, String username, String privacy, String headline, String content, String date, String time, String imageURL) {
        this.articleID = articleID;
        this.UID = UID;
        this.username = username;
        this.privacy = privacy;
        this.headline = headline;
        this.content = content;
        this.date = date;
        this.time = time;
        this.imageURL = imageURL;
    }

    public String getArticleID() {
        return articleID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
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

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public void setTime(String dateTime) {
        this.time = time;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
