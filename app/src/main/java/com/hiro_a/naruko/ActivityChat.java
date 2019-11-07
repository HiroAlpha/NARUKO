package com.hiro_a.naruko;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ActivityChat extends AppCompatActivity implements View.OnClickListener{
    int statusBarHeight;
    int screenWidth, screenHeight;
    float userImagePosX, userImagePosY;
    int menuAnimLength;
    int userColor = Color.rgb(255,192,203);
    boolean menuPos = true;
    Point userGrid;

    EditText mMessageText;
    ImageView mSendMessageButton;
    ImageView mMenuSlideButton;
    CircularImageView userImageView;

    CanvasView canvasView;
    CanvasView_history canvasViewHistory;
    CanvasView_impassive canvasViewImpassive;
    CanvasView_userIcon_outerCircle canvasViewUserIconOuterCircle;
    CanvasView_userIcon_Line canvasViewUserIconLine;

    View menuView;

    FirebaseAuth mFirebaseAuth;
    DatabaseReference mFirebaseDatabaseRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ウィンドウサイズ取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        //ユーザーアイコン座標
        userImagePosX = screenWidth-convertDp2Px(120, this);
        userImagePosY = (screenHeight/2)-convertDp2Px(60, this);
        //userGrid = new Point(userImagePosX, userImagePosY);

        //入力メニュー移動幅
        menuAnimLength = -(screenWidth/2)+20;

        //ユーザーアイコン
        userImageView = (CircularImageView) findViewById(R.id.userImageView);
        userImageView.setBorderColor(userColor);
        userImageView.setImageResource(R.drawable.gyuki);
        userImageView.setX(userImagePosX);
        userImageView.setY(userImagePosY);

        //メッセージフォーム
        mMessageText = (EditText)findViewById(R.id.messageText);
        mMessageText.setWidth(screenWidth-20);

        mSendMessageButton = (ImageView) findViewById(R.id.btn_send);
        mSendMessageButton.setOnClickListener(this);

        mMenuSlideButton = (ImageView) findViewById(R.id.btn_slide);
        mMenuSlideButton.setOnClickListener(this);

        //アニメーション用View
        canvasView = (CanvasView)findViewById(R.id.canvasView);
        canvasViewHistory = (CanvasView_history)findViewById(R.id.canvasView_history);
        canvasViewImpassive = (CanvasView_impassive) findViewById(R.id.canvasView_impassive);
//        surfaceView = (SurfaceView)findViewById(R.id.canvasView_users);
//        canvasViewUsers = new CanvasView_users(this, surfaceView);
        canvasViewUserIconOuterCircle = (CanvasView_userIcon_outerCircle)findViewById(R.id.canvasView_userIcon_outerCircle);
        canvasViewUserIconLine = (CanvasView_userIcon_Line)findViewById(R.id.canvasView_usersLine);

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
                String text = (String)dataSnapshot.child("text").getValue();
                //canvasViewに文字列を送信
                (canvasView).getMessage(text);
                viewRotate();

                //canvasViewHistoryに文字列を送信
                canvasViewHistory.getMessage(text);

                //白線
                (canvasViewUserIconLine).getUserGrid(userGrid);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //ステータスバーサイズ取得
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        statusBarHeight = rect.top;

        //UserImageViewの座標生成
        getViewGrid();
    }

    private void getViewGrid(){
        int[] viewGrid = new int[2];
        userImageView.getLocationOnScreen(viewGrid);
        userGrid = new Point(viewGrid[0], viewGrid[1]-statusBarHeight);

        //canvasViewに文字列を送信
        (canvasViewUserIconOuterCircle).getUserGrid(userGrid);
        //Toast.makeText(this, userGrid.x+":"+userGrid.y, Toast.LENGTH_SHORT).show();
    }

    private void viewFloating(){
        //ユーザーアイコン上下アニメーション
        ObjectAnimator iconFloatingUp = ObjectAnimator.ofFloat(userImageView,"translationY", userImagePosY-8, userImagePosY+8);
        iconFloatingUp.setDuration(1000);
        iconFloatingUp.setRepeatCount(ObjectAnimator.INFINITE);
        iconFloatingUp.setRepeatMode(ObjectAnimator.REVERSE);
        iconFloatingUp.start();
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
