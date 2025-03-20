package com.example.linkup.Object;

import java.io.Serializable;

// article object directly through the Intent when setting the click listener for holder
public class Articles implements Serializable {
    String articleID,UID,headline,content,date,time;

    public Articles() {
    }

    public Articles(String articleID, String UID, String username, String privacy, String headline, String content, String date, String time, String imageURL) {
        this.articleID = articleID;
        this.UID = UID;
        this.headline = headline;
        this.content = content;
        this.date = date;
        this.time = time;
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

    public void setTime(String time) {
        this.time = time;
    }

}
