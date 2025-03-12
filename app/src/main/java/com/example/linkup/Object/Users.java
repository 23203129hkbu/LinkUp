package com.example.linkup.Object;

// Data Object (Users) for firebase
public class Users {
    String userId;

    public Users() {
    }

    // for sign up -> firebase
    public Users(String avatar, String userId, String username, String introduction) {
        this.userId = userId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
