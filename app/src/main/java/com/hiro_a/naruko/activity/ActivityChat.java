package com.hiro_a.naruko.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hiro_a.naruko.common.Message;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_history;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_impassive;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_userIcon_Line;
import com.hiro_a.naruko.view.ChatView.ChatCanvasView_userIcon_outerCircle;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ActivityChat extends AppCompatActivity implements View.OnClickListener{
    int statusBarHeight;
    int screenWidth, screenHeight;
    int menuAnimLength;
    int userColor = Color.rgb(255,192,203);
    boolean menuPos = true;
    Point[] userGrid = new Point[5];

    EditText mMessageText;
    ImageView mSendMessageButton;
    ImageView mMenuSlideButton;
    CircularImageView userImageView01, userImageView02, userImageView03;

    ChatCanvasView canvasView;
    ChatCanvasView_history canvasViewHistory;
    ChatCanvasView_impassive canvasViewImpassive;
    ChatCanvasView_userIcon_outerCircle canvasViewUserIconOuterCircle;
    ChatCanvasView_userIcon_Line canvasViewUserIconLine;

    View menuView;

    FirebaseAuth mFirebaseAuth;
    DatabaseReference mFirebaseDatabaseRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "anzu_font.ttf"); //フォント

        //ウィンドウサイズ取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        //ユーザーアイコン座標(右上から反時計回り)
        int topLeftUserX = (int)(screenWidth-convertDp2Px(100, this));
        int topLeftUserY = (int)((screenHeight/2)-convertDp2Px(100, this));
        userGrid[0] = new Point(topLeftUserX, topLeftUserY);

        int topMiddleUserX = (int)((screenWidth/2)-convertDp2Px(50, this));
        int topMiddleUserY = (int)((screenHeight/2)-convertDp2Px(40, this));
        userGrid[1] = new Point(topMiddleUserX, topMiddleUserY);

        int topRightUserX = 0;
        int topRightUserY = (int)((screenHeight/2)-convertDp2Px(-20, this));
        userGrid[2] = new Point(topRightUserX, topRightUserY);

        //入力メニュー移動幅
        menuAnimLength = -(screenWidth/2)+20;

        //ユーザーアイコン
        userImageView01 = (CircularImageView) findViewById(R.id.userImageView01);
        userImageView01.setBorderColor(userColor);
        userImageView01.setImageResource(R.drawable.ic_launcher_background);
        userImageView01.setX(topLeftUserX);
        userImageView01.setY(topLeftUserY);

        userImageView02 = (CircularImageView) findViewById(R.id.userImageView02);
        userImageView02.setBorderColor(userColor);
        userImageView02.setImageResource(R.drawable.ic_launcher_background);
        userImageView02.setX(topMiddleUserX);
        userImageView02.setY(topMiddleUserY);

        userImageView03 = (CircularImageView) findViewById(R.id.userImageView03);
        userImageView03.setBorderColor(userColor);
        userImageView03.setImageResource(R.drawable.ic_launcher_background);
        userImageView03.setX(topRightUserX);
        userImageView03.setY(topRightUserY);
        userImageView03.setVisibility(View.GONE);


        //メッセージフォーム
        mMessageText = (EditText)findViewById(R.id.messageText);
        mMessageText.setTypeface(typeface);
        mMessageText.setWidth(screenWidth-20);

        mSendMessageButton = (ImageView) findViewById(R.id.btn_send);
        mSendMessageButton.setOnClickListener(this);

        mMenuSlideButton = (ImageView) findViewById(R.id.btn_slide);
        mMenuSlideButton.setOnClickListener(this);

        //アニメーション用View
        canvasView = (ChatCanvasView)findViewById(R.id.canvasView);
        canvasViewHistory = (ChatCanvasView_history)findViewById(R.id.canvasView_history);
        canvasViewImpassive = (ChatCanvasView_impassive) findViewById(R.id.canvasView_impassive);
//        surfaceView = (SurfaceView)findViewById(R.id.canvasView_users);
//        canvasViewUsers = new ChatCanvasView_users(this, surfaceView);
        canvasViewUserIconOuterCircle = (ChatCanvasView_userIcon_outerCircle)findViewById(R.id.canvasView_userIcon_outerCircle);
        canvasViewUserIconLine = (ChatCanvasView_userIcon_Line)findViewById(R.id.canvasView_usersLine);

        //入力メニュー
        menuView = findViewById(R.id.chat_ui);

        //アニメーション
        //viewFloating();

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();

        mFirebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabaseRef.child("Message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String name = (String)dataSnapshot.child("name").getValue();
                String text = (String)dataSnapshot.child("text").getValue();
                if (!TextUtils.isEmpty(text)){
                    Point grid = userGrid[0];

                    if (!userId.equals(name)){
                        grid = userGrid[1];
                    }

                    //canvasViewに文字列を送信
                    (canvasView).getMessage(text);
                    viewRotate();

                    //canvasViewHistoryに文字列を送信
                    canvasViewHistory.getMessage(text);

                    //白線
                    (canvasViewUserIconLine).getUserGrid(grid);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        (canvasViewUserIconOuterCircle).getUserGrid(userGrid[0]);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_send:
                if (!(TextUtils.isEmpty(mMessageText.getText().toString()))){
                    sendMessage();
                    /*
                    //canvasViewに文字列を送信
                    (canvasView).getMessage(mMessageText.getText().toString());
                    viewRotate();

                    //canvasViewHistoryに文字列を送信
                    canvasViewHistory.getMessage(mMessageText.getText().toString());
                    mMessageText.setText("");

                    //
                    (canvasViewUserIconLine).getUserGrid(userGrid);

                     */
                }
                break;
            case R.id.btn_slide:
                viewSlide();
                break;

        }
    }

    private void viewFloating(){
        //ユーザーアイコン上下アニメーション
//        ObjectAnimator iconFloatingUp = ObjectAnimator.ofFloat(userImageView01,"translationY", topLeftUserX-8, topLeftUserY+8);
//        iconFloatingUp.setDuration(1000);
//        iconFloatingUp.setRepeatCount(ObjectAnimator.INFINITE);
//        iconFloatingUp.setRepeatMode(ObjectAnimator.REVERSE);
//        iconFloatingUp.start();
    }

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

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public void sendMessage(){
        String text = mMessageText.getText().toString();

        Message message = new Message(text, userId);
        mFirebaseDatabaseRef.child("Message").push().setValue(message);
        mMessageText.setText("");
    }
}
