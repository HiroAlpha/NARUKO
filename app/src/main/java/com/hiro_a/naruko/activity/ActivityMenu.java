package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hiro_a.naruko.Fragment.menuChat;
import com.hiro_a.naruko.Fragment.menuFriend;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.item.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ActivityMenu extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{
    int screenWidth, screenHeight;
    boolean popout = false;

    TextView mFlowerImage;
    ImageView mOverlayColor;
    MenuItem mFriendButton, mRoomButton, mSettingButton;
    MenuItem mRoomAddButton, mRoomFavButton;

    FragmentManager fragmentManager;

    String TAG = "NARUKO_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //ウィンドウサイズ取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        mOverlayColor = (ImageView)findViewById(R.id.overlayColor);
        mOverlayColor.setVisibility(View.GONE);

        mFlowerImage = (TextView)findViewById(R.id.flowerImage);
        mFlowerImage.setOnClickListener(this);

        //フレンド系
        mFriendButton = (MenuItem) findViewById(R.id.friendButton);
        mFriendButton.setOnClickListener(this);

        //グループ系
        mRoomButton = (MenuItem) findViewById(R.id.roomButton);
        mRoomButton.setOnClickListener(this);
        mRoomButton.setOnLongClickListener(this);

        mRoomAddButton = (MenuItem) findViewById(R.id.roomAddButton);
        mRoomAddButton.setOnClickListener(this);

        mRoomFavButton = (MenuItem) findViewById(R.id.roomFavButton);
        mRoomFavButton.setOnClickListener(this);

        //設定系
        mSettingButton = (MenuItem) findViewById(R.id.settingButton);
        mSettingButton.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();

        fragmentChanger(1002);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.friendButton:
                fragmentChanger(1001);
                goBackAnimation();
                popout = !popout;
                break;

            case R.id.roomButton:
                fragmentChanger(1002);
                goBackAnimation();
                popout = !popout;
                break;

            case R.id.settingButton:
                goBackAnimation();
                popout = !popout;
                break;

            case R.id.flowerImage:
                mFlowerImage.setEnabled(false);
                mOverlayColor.setVisibility(View.VISIBLE);

                if (popout){
                    goBackAnimation();
                }
                if (!popout){
                    popupAnimation();
                }
                popout = !popout;
                break;
        }
    }

    @Override
    public boolean onLongClick(View view){
        switch (view.getId()) {
            case R.id.friendButton:
                break;

            case R.id.roomButton:
                subMenuScaleAnimation(view);
                subMenuPopupAnimation(view);
                break;
        }
        return true;
    }

    //Fragment変更
    public void fragmentChanger(int buttonId){
        switch (buttonId){
            case 1001:
                Fragment fragmentFriend = new menuFriend();
                FragmentTransaction transactionToFriend = fragmentManager.beginTransaction();
                transactionToFriend.replace(R.id.fragmentContainer, fragmentFriend);
                transactionToFriend.addToBackStack(null);
                transactionToFriend.commit();
                break;

            case 1002:
                Fragment fragmentChat = new menuChat();
                FragmentTransaction transactionToChat = fragmentManager.beginTransaction();
                transactionToChat.replace(R.id.fragmentContainer, fragmentChat);
                transactionToChat.addToBackStack(null);
                transactionToChat.commit();
                break;
        }
    }

    //メニューを出す
    private void popupAnimation(){
        View[] viewList_all = new View[]{
                mFlowerImage, mFriendButton, mRoomButton, mSettingButton
        };

        View[] viewList_button = new View[]{
                mRoomButton, mFriendButton, mSettingButton
        };

        int distance = 300;
        int viewNum = viewList_button.length;
        List<Animator> animatorList_toCenter = new ArrayList<Animator>();
        List<Animator> animatorList_button = new ArrayList<Animator>();

        //花アニメーション
        ObjectAnimator[] toCenterAnim = new ObjectAnimator[viewNum+1];
        for (int i=0;i<viewNum+1;i++){
            View target = viewList_all[i];

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = holderX = PropertyValuesHolder.ofFloat( "translationY", 0f, -(float)(screenHeight/3));

            toCenterAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX);
            toCenterAnim[i].setDuration(700);
            animatorList_toCenter.add(toCenterAnim[i]);
        }
        AnimatorSet toCenterSet = new AnimatorSet();
        toCenterSet.playTogether(animatorList_toCenter);
        toCenterSet.start();

        //ボタンアニメーション
        ObjectAnimator[] buttonAnim = new ObjectAnimator[viewNum];
        for (int i=0;i<viewNum;i++){

            float degree = (i * 360 / viewNum)+90;
            View target = viewList_button[i];

            float mainButtonEndGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float mainButtonEndGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));
            Log.d(TAG, target.toString()+":"+degree);

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", 0f, mainButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -(float)(screenHeight/3), -mainButtonEndGridY );

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(350);
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.setStartDelay(700);
        buttonSet.playSequentially(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFlowerImage.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        buttonSet.start();
    }

    //メニューをしまう
    private void goBackAnimation(){
        View[] viewList_all = new View[]{
                mFlowerImage, mFriendButton, mRoomButton, mSettingButton
        };

        View[] viewList_button = new View[]{
                mRoomButton, mFriendButton, mSettingButton
        };

        int distance = 300;
        int viewNum = viewList_button.length;
        List<Animator> animatorList_toCenter = new ArrayList<Animator>();
        List<Animator> animatorList_button = new ArrayList<Animator>();

        //ボタンアニメーション
        ObjectAnimator[] buttonAnim = new ObjectAnimator[viewNum];
        for (int i=0;i<viewNum;i++){

            float degree = (i * 360 / viewNum)+90;
            View target = viewList_button[i];

            float mainButtonEndGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float mainButtonEndGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", mainButtonEndGridX, 0f );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -mainButtonEndGridY, -(float)(screenHeight/3));

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(500);
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playTogether(animatorList_button);
        buttonSet.start();

        //花アニメーション
        ObjectAnimator[] toCenterAnim = new ObjectAnimator[viewNum+1];
        for (int i=0;i<viewNum+1;i++){
            View target = viewList_all[i];

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = holderX = PropertyValuesHolder.ofFloat( "translationY", -(float)(screenHeight/3), 0f);

            toCenterAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX);
            toCenterAnim[i].setDuration(700);
            animatorList_toCenter.add(toCenterAnim[i]);
        }
        AnimatorSet toCenterSet = new AnimatorSet();
        toCenterSet.setStartDelay(500);
        toCenterSet.playTogether(animatorList_toCenter);
        toCenterSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFlowerImage.setEnabled(true);
                mOverlayColor.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        toCenterSet.start();
    }

    //サブメニューを出す
    private void subMenuPopupAnimation(View view){
        float mainButtonDegree = 0;
        switch (view.getId()) {
            case R.id.roomButton:
                mainButtonDegree = 90f;
                break;

            case R.id.friendButton:
                mainButtonDegree = 210f;
                break;

            case R.id.settingButton:
                mainButtonDegree = 330f;
                break;
        }

        //親ボタン位置
        float mainButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(mainButtonDegree)));
        float mainButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(mainButtonDegree))+(float)(screenHeight/3));

        View[] viewList_subButton = new View[]{
                mRoomAddButton, mRoomFavButton
        };

        int distance = 200;
        int viewNum = viewList_subButton.length;
        List<Animator> animatorList_toCenter = new ArrayList<Animator>();
        List<Animator> animatorList_button = new ArrayList<Animator>();

        //ボタンアニメーション
        ObjectAnimator[] buttonAnim = new ObjectAnimator[viewNum];
        for (int i=0;i<viewNum;i++){

            float degree = (i * 180 / viewNum)+45;
            View target = viewList_subButton[i];

            //サブボタン最終地点
            float subButtonEndGridX = (float)(distance * Math.cos(Math.toRadians(degree))+mainButtonEndGridX);
            float subButtonEndGridY = (float)(distance * Math.sin(Math.toRadians(degree))+mainButtonEndGridY);

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", mainButtonEndGridX, subButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -mainButtonEndGridY, -subButtonEndGridY);

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(500);
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playTogether(animatorList_button);
        buttonSet.start();
    }

    //サブメニュー縮小&画像変更
    public void subMenuScaleAnimation(View view){
        View target = view;

        PropertyValuesHolder scaleToX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.8f);
        PropertyValuesHolder scaleToY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.8f);

        ObjectAnimator buttonScaleAnim = new ObjectAnimator();
        buttonScaleAnim = ObjectAnimator.ofPropertyValuesHolder(target, scaleToX, scaleToY);
        buttonScaleAnim.setDuration(100);
        buttonScaleAnim.start();
    }
}
