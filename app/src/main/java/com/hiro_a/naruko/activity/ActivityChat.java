package com.hiro_a.naruko.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_history;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_impassive;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_userIcon_Line;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_userIcon_outerCircle;
import com.hiro_a.naruko.view.NarukoUserIconPopoutView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ActivityChat extends AppCompatActivity implements View.OnClickListener{
    String TAG = "NARUKO_DEBUG @ ActivityChat";
    Context context;

    int lastSpokeIconNum;
    float screenWidth, screenHeight;
    float menuAnimLength;
    String roomId;
    String userName_original;
    String userId_original;
    boolean menuPos = true;
    Point screenSize;

    ArrayList<String> userIdArray;

    RelativeLayout relativeLayout;

    EditText mMessageText;
    ImageView mSendMessageButton;
    ImageView mMenuSlideButton;

    ChatCanvasView canvasView;
    ChatCanvasView_history canvasViewHistory;
    ChatCanvasView_impassive canvasViewImpassive;
    ChatCanvasView_userIcon_outerCircle canvasViewUserIconOuterCircle;
    ChatCanvasView_userIcon_Line canvasViewUserIconLine;
    NarukoUserIconPopoutView narukoUserIconPopoutView;

    View menuView;

    FirebaseFirestore mFirebaseDatabase;
    CollectionReference messageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = getApplicationContext();

        userIdArray = new ArrayList<String>();
        Typeface typeface = Typeface.createFromAsset(getAssets(), "anzu_font.ttf"); //フォント

        //User_Info
        DeviceInfo userInfo = new DeviceInfo();
        userId_original = userInfo.getUserId(context);
        userName_original = userInfo.getUserName(context);
        Log.d(TAG, "*** User_Info ***");
        Log.d(TAG, "UserName: " + userName_original);
        Log.d(TAG, "UserId: " + userId_original);
        Log.d(TAG, "---------------------------------");

        //Room_Info
        Intent room = getIntent();
        roomId = room.getStringExtra("roomId");
        Log.d(TAG, "*** ChatRoom_Info ***");
        Log.d(TAG, "RoomId: " + roomId);
        Log.d(TAG, "---------------------------------");

        //getWindowWidth
        screenWidth = (int) userInfo.getScreenWidth(context);
        screenHeight = (int) userInfo.getScreenHeight(context);
        screenSize = new Point((int) screenWidth, (int) screenHeight);

        //入力メニュー移動幅
        menuAnimLength = -(screenWidth/2)+20;

        //User_Icon
        relativeLayout = (RelativeLayout) findViewById(R.id.narukoRelativeLayout);
        narukoUserIconPopoutView = (NarukoUserIconPopoutView)findViewById(R.id.userIcon_view);

        //Message_EditText
        mMessageText = (EditText)findViewById(R.id.messageText);
        mMessageText.setTypeface(typeface);
        mMessageText.setWidth((int) screenWidth-20);

        //Send_button
        mSendMessageButton = (ImageView) findViewById(R.id.btn_send);
        mSendMessageButton.setOnClickListener(this);

        //Slide_button
        mMenuSlideButton = (ImageView) findViewById(R.id.btn_slide);
        mMenuSlideButton.setOnClickListener(this);

        //Sub_button
        menuView = findViewById(R.id.chat_ui);

        //View for animation
        canvasView = (ChatCanvasView)findViewById(R.id.canvasView);
        canvasViewHistory = (ChatCanvasView_history)findViewById(R.id.canvasView_history);
        canvasViewImpassive = (ChatCanvasView_impassive) findViewById(R.id.canvasView_impassive);
        canvasViewUserIconLine = (ChatCanvasView_userIcon_Line)findViewById(R.id.canvasView_usersLine);

        //Firebase
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        messageRef = mFirebaseDatabase.collection("rooms").document(roomId).collection("messages");

        updateMessage();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //(canvasViewUserIconOuterCircle).getUserGrid(userGrid[0]);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_send:
                if (!(TextUtils.isEmpty(mMessageText.getText().toString()))){
                    sendMessage();
                }
                break;
            case R.id.btn_slide:
                viewSlide();
                break;

        }
    }

    //Send_message
    public void sendMessage(){
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String time = SD.format(new Date()).toString();
//        String globalIP = getPublicIPAddress();
        String globalIP = null;
        String text = mMessageText.getText().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("datetime", time);
        message.put("globalIP", globalIP);
        message.put("userId", userId_original);
        message.put("message", text);

        messageRef.add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "SUCSESS: Document Added");
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ERROR: Adding Document Failed", e);
                Log.w(TAG, "---------------------------------");
            }
        });
        mMessageText.setText("");
    }

    //Update_message
    public void updateMessage(){
        messageRef.orderBy("datetime", Query.Direction.ASCENDING).limit(6).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "ERROR: Listen Failed", e);
                    Log.w(TAG, "---------------------------------");
                    return;
                }

                for (DocumentChange document : snapshots.getDocumentChanges()) {
                    switch (document.getType()){
                        case ADDED:
                            String datetime = document.getDocument().getString("datetime");
                            String globalIP = document.getDocument().getString("globalIP");
                            String userId = document.getDocument().getString("userId");
                            String text = document.getDocument().getString("message");

                            Log.d(TAG, "*** Message_Info ***");
                            Log.d(TAG, "PostedTime: "+datetime);
                            Log.d(TAG, "GlobalIP: "+globalIP);
                            Log.d(TAG, "UserId: "+userId);
                            Log.d(TAG, "Message: "+text);
                            Log.d(TAG, "---------------------------------");

                            if (!TextUtils.isEmpty(text)) {

                                //Message => canvasView
                                (canvasView).getMessage(text);
                                viewRotate();

                                //Message => canvasViewHistory
                                canvasViewHistory.getMessage(text);

                                //Add User_Icon
                                if (!userIdArray.contains(userId)){
                                    userIdArray.add(userId);
                                    narukoUserIconPopoutView.addUserIcon(screenSize, relativeLayout, userName_original);
                                }

                                //White_line
                                lastSpokeIconNum = userIdArray.indexOf(userId);
                                narukoUserIconPopoutView.setLastSpeaker(canvasViewUserIconLine, lastSpokeIconNum);

                            }
                            break;

                        case REMOVED:
                            break;
                    }
                }

            }
        });
    }

    //Global_IP
    public String getPublicIPAddress(){
        String value = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> result = es.submit(new Callable<String>() {
            public String call() throws Exception {
                try {
                    URL url = new URL("http://whatismyip.akamai.com/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        urlConnection.disconnect();
                        return total.toString();
                    }finally {
                        urlConnection.disconnect();
                    }
                }catch (IOException e){
                    Log.e("Public IP: ",e.getMessage());
                }
                return null;
            }
        });
        try {
            value = result.get();
        } catch (Exception e) {
            Log.w(TAG, "ERROR: Exception Occured", e);
            Log.w(TAG, "---------------------------------");
        }
        es.shutdown();
        return value;
    }

    //------Animation From here------

    private void viewRotate(){
        //文字列回転アニメーション
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.view_rotation);    //アニメーションはR.anim.view_rotationから
        canvasView.startAnimation(rotate);

        //履歴回転アニメーション（ずらす）
        Animation rotate_instant = AnimationUtils.loadAnimation(this, R.anim.view_rotation_instant);    //アニメーションはR.anim.view_rotation_instantから
        canvasViewHistory.startAnimation(rotate_instant);
    }

    private void viewSlide(){
        //入力メニュースライドアニメーション
        if (menuPos){
            ObjectAnimator translate = ObjectAnimator.ofFloat(menuView, "translationX", 0, menuAnimLength);
            translate.setDuration(700);
            translate.start();
            menuPos = false;

        } else if (!menuPos){
            ObjectAnimator translate = ObjectAnimator.ofFloat(menuView, "translationX", menuAnimLength, 0);
            translate.setDuration(700);
            translate.start();
            menuPos = true;
        }
    }
}
