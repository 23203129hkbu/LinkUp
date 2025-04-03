package com.example.linkup.Object;

// ✅
public class ArticleComments {
    String UID,comment,date;

    public ArticleComments() {
    }

    public ArticleComments(String UID, String username, String comment, String date, String time, String imageURL) {
        this.UID = UID;
        this.comment = comment;
        this.date = date;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
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

}
