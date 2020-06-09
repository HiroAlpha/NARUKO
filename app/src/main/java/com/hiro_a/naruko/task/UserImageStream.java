package com.hiro_a.naruko.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.hiro_a.naruko.activity.ActivitySelectLogin;

import java.io.InputStream;
import java.net.URL;

public class UserImageStream extends AsyncTask<String , String, InputStream> {
    String TAG = "NARUKO_DEBUG @ UserImageStream";

    ActivitySelectLogin activity;

    public UserImageStream(ActivitySelectLogin activity){
        super();
        this.activity = activity;
    }

    @Override
    protected InputStream doInBackground(String... imageUrlString) {
        InputStream stream = null;

        try{
            URL imageUrl = new URL(imageUrlString[0]);
            stream = imageUrl.openStream();

            Log.w(TAG, "*** UserImageCheck ***");
            for (int i=0;i<imageUrlString.length;i++){
                Log.w(TAG, imageUrlString[i]);
            }
            Log.w(TAG, "---------------------------------");

        }catch (Exception e){
            Log.w(TAG, "ERROR: Getting Stream", e);
            Log.w(TAG, "---------------------------------");
        }


        return stream;
    }

    @Override
    protected void onPostExecute(InputStream inputStream) {
        activity.UploadUserImage(inputStream);
    }
}
