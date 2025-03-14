package com.example.linkup.Object;

// Data Object (Users) for firebase
public class Users {
    String UID, username, imageURI;

    public Users() {
    }

    // for sign up -> firebase
    public Users(String UID, String username, String imageURI, String privacy) {
        this.UID = UID;
        this.username = username;
        this.imageURI = imageURI;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getImageURI() {
        return imageURI;
    }

}
