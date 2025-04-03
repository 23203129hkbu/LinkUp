package com.example.linkup.Object;

import java.io.Serializable;

// ✅
// Data Object (Users) for firebase
public class Users implements Serializable {
    String UID, username, avatarURL, privacy, website, introduction;

    public Users(String UID, String username, String avatarURL, String privacy, String website, String introduction) {
        this.UID = UID;
        this.username = username;
        this.avatarURL = avatarURL;
        this.privacy = privacy;
        this.website = website;
        this.introduction = introduction;
    }

    public Users() {
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

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
