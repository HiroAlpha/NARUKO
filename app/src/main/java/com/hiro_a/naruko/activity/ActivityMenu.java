package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
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
    boolean popoutFriend = false;
    boolean popoutRoom = false;
    boolean popoutSetting = false;

    TextView mFlowerImage;
    ImageView mOverlayColor;
    MenuItem mFriendButton, mRoomButton, mSettingButton;
    MenuItem mRoomAddButton, mRoomFavButton, mFriendAddButton;

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
        mOverlayColor.setOnClickListener(this);

        mFlowerImage = (TextView)findViewById(R.id.flowerImage);
        mFlowerImage.setOnClickListener(this);

        //フレンド系
        mFriendButton = (MenuItem) findViewById(R.id.friendButton);
        mFriendButton.setOnClickListener(this);
        mFriendButton.setOnLongClickListener(this);

        mFriendAddButton = (MenuItem) findViewById(R.id.friendAddButton);
        mFriendAddButton.setVisibility(View.GONE);
        mFriendAddButton.setOnClickListener(this);

        //グループ系
        mRoomButton = (MenuItem) findViewById(R.id.roomButton);
        mRoomButton.setOnClickListener(this);
        mRoomButton.setOnLongClickListener(this);

        mRoomAddButton = (MenuItem) findViewById(R.id.roomAddButton);
        mRoomAddButton.setVisibility(View.GONE);
        mRoomAddButton.setOnClickListener(this);

        mRoomFavButton = (MenuItem) findViewById(R.id.roomFavButton);
        mRoomFavButton.setVisibility(View.GONE);
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
            case R.id.overlayColor:
                goBackAnimation();
                break;

            case R.id.friendButton:
                fragmentChanger(1001);
                goBackAnimation();
                break;

            case R.id.roomButton:
                fragmentChanger(1002);
                goBackAnimation();
                break;

            case R.id.settingButton:
                goBackAnimation();
                break;

            case R.id.flowerImage:
                mOverlayColor.setVisibility(View.VISIBLE);

                if (popout){
                    goBackAnimation();
                }
                if (!popout){
                    popupAnimation();
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view){
        String classId = "onLongClick";
        switch (view.getId()) {
            case R.id.friendButton:
                mFriendButton.setEnabled(false);

                if (popoutFriend){
                    subMenuGoBackAnimation(view, classId);
                }else {
                    subMenuPopupAnimation(view);
                }
                break;

            case R.id.roomButton:
                mRoomButton.setEnabled(false);

                if (popoutRoom){
                    subMenuGoBackAnimation(view, classId);
                }else {
                    subMenuPopupAnimation(view);
                }
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
                mOverlayColor.setEnabled(true);
                mFriendButton.setEnabled(true);
                mRoomButton.setEnabled(true);
                mSettingButton.setEnabled(true);

                popout = true;
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
        mFlowerImage.setEnabled(false);
        mOverlayColor.setEnabled(false);
        mFriendButton.setEnabled(false);
        mRoomButton.setEnabled(false);
        mSettingButton.setEnabled(false);

        String classId = "goBackAnimation";
        if (popoutRoom){
            subMenuGoBackAnimation(mRoomButton, classId);
            return;
        } else if (popoutFriend){
            subMenuGoBackAnimation(mFriendButton, classId);
            return;
        } else if (popoutSetting){
            subMenuGoBackAnimation(mSettingButton, classId);
            return;
        }

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
                mOverlayColor.setEnabled(true);
                mFriendButton.setEnabled(true);
                mRoomButton.setEnabled(true);
                mSettingButton.setEnabled(true);

                mOverlayColor.setVisibility(View.GONE);

                popout = false;
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

        //親ボタン位置
        float lastButtonEndGridX = 0;
        float lastButtonEndGridY = 0;

        //サブメニュー
        List<View> viewList_subButton = new ArrayList<View>();

        //view判定
        switch (view.getId()) {
            case R.id.roomButton:
                mainButtonDegree = 90f; //親viewの角度

                //親ボタン位置決定
                lastButtonEndGridX = (float) (300 * Math.cos(Math.toRadians(mainButtonDegree)));
                lastButtonEndGridY = (float) (300 * Math.sin(Math.toRadians(mainButtonDegree)) + (float) (screenHeight / 3));

                //サブメニューリスト
                viewList_subButton.add(mRoomAddButton);
                viewList_subButton.add(mRoomFavButton);

                for (int i=0;i<viewList_subButton.size();i++){
                    viewList_subButton.get(i).setX(lastButtonEndGridX);
                    viewList_subButton.get(i).setY(-lastButtonEndGridY);
                    viewList_subButton.get(i).setVisibility(View.VISIBLE);
                }

                popoutRoom = !popoutRoom;
                break;

            case R.id.friendButton:
                mainButtonDegree = 210f;    //親viewの角度

                //親ボタン位置決定
                lastButtonEndGridX = (float) (300 * Math.cos(Math.toRadians(mainButtonDegree)));
                lastButtonEndGridY = (float) (300 * Math.sin(Math.toRadians(mainButtonDegree)) + (float) (screenHeight / 3));

                //サブメニューリスト
                viewList_subButton.add(mFriendAddButton);

                for (int i=0;i<viewList_subButton.size();i++){
                    viewList_subButton.get(i).setX(lastButtonEndGridX);
                    viewList_subButton.get(i).setY(lastButtonEndGridY);
                    viewList_subButton.get(i).setVisibility(View.VISIBLE);
                }

                popoutFriend = !popoutFriend;
                break;

            case R.id.settingButton:
                mainButtonDegree = 330f;    //親viewの角度

                //親ボタン位置決定
                lastButtonEndGridX = (float) (300 * Math.cos(Math.toRadians(mainButtonDegree)));
                lastButtonEndGridY = (float) (300 * Math.sin(Math.toRadians(mainButtonDegree)) + (float) (screenHeight / 3));

                popoutSetting = !popoutSetting;
                break;
        }

        //ボタンアニメーション
        int viewNum = viewList_subButton.size();    //サブメニュー数
        List<Animator> animatorList_toCenter = new ArrayList<Animator>();
        List<Animator> animatorList_button = new ArrayList<Animator>();
        ObjectAnimator[] buttonAnim = new ObjectAnimator[viewNum];
        for (int i=0;i<viewNum;i++){

            float degree = mainButtonDegree + 30*(i+1);
            View target = viewList_subButton.get(i);


            //サブボタン最終地点
            float subButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(degree)));
            float subButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", lastButtonEndGridX, subButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -lastButtonEndGridY, -subButtonEndGridY);

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(250);
            animatorList_button.add(buttonAnim[i]);

            //次のボタン用
            lastButtonEndGridX = subButtonEndGridX;
            lastButtonEndGridY = subButtonEndGridY;
        }

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playSequentially(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mRoomButton.setEnabled(true);
                mFriendButton.setEnabled(true);
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

    //サブメニューをしまう
    private void subMenuGoBackAnimation(View view, final String classId){
        float mainButtonDegree = 0;

        //サブメニュー
        final List<View> viewList_subButton = new ArrayList<View>();

        switch (view.getId()) {
            case R.id.roomButton:
                mainButtonDegree = 90f; //親viewの角度

                //サブメニューリスト
                viewList_subButton.add(mRoomAddButton);
                viewList_subButton.add(mRoomFavButton);

                popoutRoom = !popoutRoom;
                break;

            case R.id.friendButton:
                mainButtonDegree = 210f;    //親viewの角度

                //サブメニューリスト
                viewList_subButton.add(mFriendAddButton);

                popoutFriend = !popoutFriend;
                break;

            case R.id.settingButton:
                mainButtonDegree = 330f;    //親viewの角度

                popoutSetting = !popoutSetting;
                break;
        }

        //親ボタン位置
        float mainButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(mainButtonDegree)));
        float mainButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(mainButtonDegree))+(float)(screenHeight/3));

        //ボタンアニメーション
        int viewNum = viewList_subButton.size();
        List<Animator> animatorList_toCenter = new ArrayList<Animator>();
        List<Animator> animatorList_button = new ArrayList<Animator>();

        ObjectAnimator[] buttonAnim = new ObjectAnimator[viewNum];
        for (int i=0;i<viewNum;i++){

            float degree = mainButtonDegree + 30*(i+1);
            View target = viewList_subButton.get(i);

            //サブボタン最終地点
            float subButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(degree)));
            float subButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));

            //X, YをEndGridから0へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", subButtonEndGridX, mainButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -subButtonEndGridY, -mainButtonEndGridY);

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(250 + (100*i));
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playTogether(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                for (int i=0;i<viewList_subButton.size();i++){
                    viewList_subButton.get(i).setVisibility(View.GONE);
                }

                mRoomButton.setEnabled(true);
                mFriendButton.setEnabled(true);

                if (classId.equals("goBackAnimation")){
                    goBackAnimation();
                }
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

    /*
    //サブメニュー縮小&画像変更
    public void subMenuScaleAnimation(View view){
        boolean subPopout = false;
        View target = view;

        switch (view.getId()) {
            case R.id.roomButton:
                subPopout = popoutRoom;
                break;

            case R.id.friendButton:
                subPopout = popoutFriend;
                break;

            case R.id.settingButton:
                subPopout = popoutSetting;
                break;
        }

        PropertyValuesHolder scaleToX = null;
        PropertyValuesHolder scaleToY = null;
        if (subPopout){
            scaleToX = PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1.0f);
            scaleToY = PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1.0f);
        }

        if (!subPopout){
            scaleToX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.8f);
            scaleToY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.8f);
        }

        ObjectAnimator buttonScaleAnim = new ObjectAnimator();
        buttonScaleAnim = ObjectAnimator.ofPropertyValuesHolder(target, scaleToX, scaleToY);
        buttonScaleAnim.setDuration(100);
        buttonScaleAnim.start();
    }

     */
}
