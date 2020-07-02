package com.hiro_a.naruko.common;

public class NarukoMessageData {
    String postedTime;
    String globalIp;
    String userName;
    String userId;
    String userImage;
    String userColor;
    String message;

    public void setPostedTime(String postedTime) {
        this.postedTime = postedTime;
    }

    public void setGlobalIp(String globalIp) {
        this.globalIp = globalIp;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public void setUserColor(String userColor) {
        this.userColor = userColor;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public String getGlobalIp() {
        return globalIp;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getUserColor() {
        return userColor;
    }

    public String getMessage() {
        return message;
    }
}
