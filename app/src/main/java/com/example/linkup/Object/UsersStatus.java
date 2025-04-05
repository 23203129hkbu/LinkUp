package com.example.linkup.Object;

public class UsersStatus {
    String UID, status;

    public UsersStatus() {
    }
    public UsersStatus(String UID, String status) {
        this.UID = UID;
        this.status = status;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
