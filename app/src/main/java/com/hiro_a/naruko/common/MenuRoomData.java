package com.hiro_a.naruko.common;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

public class MenuRoomData {
    private String title;
    private String id;
    private StorageReference image;

    public String getTitle(){
        return title;
    }

    public String getid(){
        return id;
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

    public void setImage(StorageReference image){
        this.image = image;
    }
}
