package com.hiro_a.naruko.common;

import android.view.View;

public class FriendId {
    private String friendId;
    private View view;
    private int friendArraySize;
    private int counter;

    public String getFriendId() {
        return friendId;
    }

    public int getCounter() {
        return counter;
    }

    public int getFriendArraySize() {
        return friendArraySize;
    }

    public View getView() {
        return view;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setFriendArraySize(int friendArraySize) {
        this.friendArraySize = friendArraySize;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
