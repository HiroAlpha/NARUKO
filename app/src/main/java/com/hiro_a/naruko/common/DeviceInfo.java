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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class DeviceInfo {
    String TAG = "NARUKO_DEBUG @ DeviceInfo";

    public void setDeviceInfo(Context context, final String loginProvider){
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
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        FirebaseFirestore mFirebaseDatabase = FirebaseFirestore.getInstance();

        //ユーザーID
        final String userId = user.getUid();

        //UserReference
        DocumentReference userRef = mFirebaseDatabase.collection("users").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        //作成日時
                        String userCreated = document.getString("UserCreated");

                        //最終更新日時
                        String userLastUpdated = document.getString("UserLastUpdated");

                        //メールアドレス
                        String userEmail = user.getEmail();
                        if (userEmail==null){
                            userEmail = "";
                        }

                        //メールアドレス認証
                        boolean userEmailVerified = user.isEmailVerified();

                        //ユーザー名
                        String userName = document.getString("UserName");

                        //ユーザー画像の有無
                        boolean userImageIs = document.getBoolean("UserImageIs");

                        //ユーザーカラー
                        String userColor = document.getString("UserColor");

                        //ユーザー情報
                        editor.putString("LoginProvider", loginProvider);
                        editor.putString("UserCreated", userCreated);
                        editor.putString("UserLastUpdated", userLastUpdated);
                        editor.putString("UserName", userName);
                        editor.putString("UserId", userId);
                        editor.putString("UserEmail", userEmail);
                        editor.putBoolean("UserEmailVerified", userEmailVerified);
                        editor.putBoolean("UserImageIs", userImageIs);
                        editor.putString("UserColor", userColor);
                        editor.apply();

                        Log.d(TAG, "***Device_Info***");
                        Log.d(TAG, "ScreenWidth: " + screenSize.x);
                        Log.d(TAG, "ScreenHeight: " + screenSize.y);
                        Log.d(TAG, "LoginProvider: " + loginProvider);
                        Log.d(TAG, "UserCreated: " + userCreated);
                        Log.d(TAG, "UserLastUpdated: " + userLastUpdated);
                        Log.d(TAG, "UserName: " + userName);
                        Log.d(TAG, "UserId: " + userId);
                        Log.d(TAG, "UserEmail: " + userEmail);
                        Log.d(TAG, "UserEmailVerified: " + userEmailVerified);
                        Log.d(TAG, "UserImageIs: " + userImageIs);
                        Log.d(TAG, "UserColor: " + userColor);
                        Log.d(TAG, "---------------------------------");
                    } else {
                        Log.w(TAG, "ERROR: Document Not Found");
                        Log.w(TAG, "---------------------------------");
                    }
                } else {
                    Log.w(TAG, "ERROR: Unknown", task.getException());
                    Log.w(TAG, "---------------------------------");
                }
            }
        });
    }

    public String getUserName(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getString("UserName", "N/A");
    }

    public String getUserId(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getString("UserId", "N/A");
    }

    public String getUserEmail(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getString("UserEmail", "N/A");
    }

    public boolean getUserEmailVerified(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getBoolean("UserEmailVerified", false);
    }

    public boolean getUserImageIs(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getBoolean("UserImageIs", false);
    }

    public long getUserImageUpdateTime(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getLong("UserImageUpdateTime", 0);
    }

    public String getUserColor(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getString("UserColor", "Yuuna");
    }

    public ArrayList<String> getUserFavRooms(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        String json = userData.getString("UserFavRooms", null);
        ArrayList<String> favRooms = new ArrayList<>();
        if (json != null){
            try{
                JSONArray jsonArray = new JSONArray(json);
                for (int i=0;i<jsonArray.length();i++){
                    String favRoom = jsonArray.optString(i);
                    favRooms.add(favRoom);
                }

            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        return favRooms;
    }

    public float getScreenWidth(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getFloat("ScreenWidth", 0f);
    }

    public float getScreenHeight(Context context){
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        return userData.getFloat("ScreenHeight", 0f);
    }
}
