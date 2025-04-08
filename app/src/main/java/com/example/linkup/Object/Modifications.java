package com.example.linkup.Object;

public class Modifications {
    String eventId,content;
    Boolean read;

    public Modifications() {
    }

    public Modifications(String eventId, String content, Boolean read) {
        this.eventId = eventId;
        this.content = content;
        this.read = read;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

}

