package com.hiro_a.naruko.common;

public class MenuChatData {
    private String title;
    private int image;

    public String getString(){
        return title;
    }

    public int getInt(){
        return image;
    }

    public void setString(String title){
        this.title = title;
    }

    public void setInt(int image){
        this.image = image;
    }
}
