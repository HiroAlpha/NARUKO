package com.hiro_a.naruko.common;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

public class MenuRoomData {
    private String title;
    private String id;
    private String createdTime;
    private String encodedPassword;
    private StorageReference image;

    public String getTitle(){
        return title;
    }

    public String getId(){
        return id;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getPassword() {
        return encodedPassword;
    }

    public StorageReference getImage(){
        return image;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setPassword(String password){
        this.encodedPassword = password;
    }

    public void setImage(StorageReference image){
        this.image = image;
    }
}
