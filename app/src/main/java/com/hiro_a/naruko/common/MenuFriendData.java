package com.hiro_a.naruko.common;

public class MenuFriendData {
    private int friendImage;
    private String friendName;
    private String friendId;

    public String getFriendName() {
        return friendName;
    }

    public int getFriendImage() {
        return friendImage;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setFriendImage(int friendImage) {
        this.friendImage = friendImage;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
