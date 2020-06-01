package com.hiro_a.naruko.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeviceInfo {
    String TAG = "NARUKO_DEBUG @ DeviceInfo";

    public void setDeviceInfo(Context context){
        //SharedPreferences
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = userData.edit();

        //getScreenSize
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        final Point screenSize = new Point();
        disp.getSize(screenSize);
        editor.putFloat("ScreenWidth", screenSize.x);
        editor.putFloat("ScreenHeight", screenSize.y);

        //Firebase
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirebaseDatabase = FirebaseFirestore.getInstance();

        //UserId
        final String userId = mFirebaseAuth.getCurrentUser().getUid();

        //UserReference
        DocumentReference userRef = mFirebaseDatabase.collection("users").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String userName = document.getString("userName");
                        String userImageUri = document.getString("userImage");

                        editor.putString("UserName", userName);
                        editor.putString("UserId", userId);
                        editor.putString("UserImage", userImageUri);
                        editor.apply();

                        Log.d(TAG, "***Device_Info***");
                        Log.d(TAG, "ScreenWidth: " + screenSize.x);
                        Log.d(TAG, "ScreenHeight: " + screenSize.y);
                        Log.d(TAG, "UserName: " + userName);
                        Log.d(TAG, "UserId: " + userId);
                        Log.d(TAG, "UserImage: " + userImageUri);
                        Log.d(TAG, "---------------------------------");
                    } else {
                        Log.w(TAG, "ERROR: Document Not Found");
                        Log.d(TAG, "---------------------------------");
                    }
                } else {
                    Log.w(TAG, "ERROR: Unknown", task.getException());
                    Log.d(TAG, "---------------------------------");
                }
            }
        });
    }

    public String getUserName(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String userName = userData.getString("UserName", "N/A");

        return userName;
    }

    public String getUserId(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String userId = userData.getString("UserId", "N/A");

        return userId;
    }

    public String getUserImage(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String userImage = userData.getString("UserImage", "N/A");

        return userImage;
    }

    public float getScreenWidth(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        float screenWidth = userData.getFloat("ScreenWidth", 0f);

        return screenWidth;
    }

    public float getScreenHeight(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        float screenHeight = userData.getFloat("ScreenHeight", 0f);

        return screenHeight;
    }
}
