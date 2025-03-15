package com.example.linkup.Object;

// Data Object (Users) for firebase
public class Users {
    String UID, username, imageURI, privacy;

    public Users() {
    }

    // for sign up -> firebase
    public Users(String UID, String username, String imageURI, String privacy) {
        this.UID = UID;
        this.username = username;
        this.imageURI = imageURI;
        this.privacy = privacy;
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

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

}
