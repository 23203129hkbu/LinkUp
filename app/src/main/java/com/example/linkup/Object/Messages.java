package com.example.linkup.Object;

// ✅
public class Messages {
    String content, UID, type;
    long timestamp;

    public Messages() {
    }

    public Messages(String content, String UID, String type, long timestamp) {
        this.content = content;
        this.UID = UID;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
