package com.hiro_a.naruko.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

public class MakeStoargeUri {
    private String TAG = "NARUKO_DEBUG @ CropImageView";

    //部屋画像保存Uri
    public Uri makeNewUri(Context context){
        String fileName = "c_image.jpg";

        File dataDir = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM, "NARUKO");
        if(!dataDir.exists()){
            dataDir.mkdirs();
            Log.d(TAG, "SUCSESS CREATING DCIM/NARUKO");
        }
        File filePath = new File(dataDir,fileName);
        Uri trialImageUri = Uri.fromFile(filePath);

        ContentValues values = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        values.put(MediaStore.Images.Media.DATA, Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + "NARUKO/" +fileName);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return trialImageUri;
    }
}
