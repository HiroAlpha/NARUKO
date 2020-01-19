package com.hiro_a.naruko.common;

import android.net.Uri;

public class MenuFriendData {
    private String friendImage;
    private String friendName;
    private String friendId;

    public String getFriendName() {
        return friendName;
    }

    public String getFriendImage() {
        return friendImage;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setFriendImage(String friendImage) {
        this.friendImage = friendImage;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
