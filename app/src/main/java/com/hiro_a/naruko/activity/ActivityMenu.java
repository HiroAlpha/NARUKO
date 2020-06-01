package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.fragment.menuFriend;
import com.hiro_a.naruko.fragment.menuRoom;
import com.hiro_a.naruko.fragment.menuRoomAdd;
import com.hiro_a.naruko.item.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ActivityMenu extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    Context context;
    String TAG = "NARUKO_DEBUG @ ActivityMenu";

    int recyCount = 0;
    float screenWidth, screenHeight;
    boolean popout = false;
    boolean popoutFriend = false;
    boolean popoutRoom = false;

    ImageView image_Center, image_Overlay;
    MenuItem button_Room, button_Room_Create, button_Room_Fav;
    MenuItem button_Friend, button_Friend_Search;
    MenuItem button_setting;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = getApplicationContext();

        //getWindowWidth
        DeviceInfo userInfo = new DeviceInfo();
        screenWidth = userInfo.getScreenWidth(context);
        screenHeight = userInfo.getScreenHeight(context);

        //BackGround_Black_Trance
        image_Overlay = (ImageView)findViewById(R.id.overlayColor);
        image_Overlay.setVisibility(View.GONE);
        image_Overlay.setOnClickListener(this);

        //Center_Image
        image_Center = (ImageView) findViewById(R.id.flowerImage);
        image_Center.setOnClickListener(this);

        //Button_Room
        button_Room = (MenuItem) findViewById(R.id.roomButton);
        button_Room.setOnClickListener(this);
        button_Room.setOnLongClickListener(this);
        button_Room.setOnTouchListener(this);

        button_Room_Create = (MenuItem) findViewById(R.id.roomAddButton);
        button_Room_Create.setVisibility(View.GONE);
        button_Room_Create.setOnClickListener(this);
        button_Room_Create.setOnTouchListener(this);

        button_Room_Fav = (MenuItem) findViewById(R.id.roomFavButton);
        button_Room_Fav.setVisibility(View.GONE);
        button_Room_Fav.setOnClickListener(this);
        button_Room_Fav.setOnTouchListener(this);

        //Button_Friend
        button_Friend = (MenuItem) findViewById(R.id.friendButton);
        button_Friend.setOnClickListener(this);
        button_Friend.setOnLongClickListener(this);
        button_Friend.setOnTouchListener(this);

        button_Friend_Search = (MenuItem) findViewById(R.id.friendAddButton);
        button_Friend_Search.setVisibility(View.GONE);
        button_Friend_Search.setOnClickListener(this);
        button_Friend_Search.setOnTouchListener(this);

        //Button_Setting
        button_setting = (MenuItem) findViewById(R.id.settingButton);
        button_setting.setOnClickListener(this);
        button_setting.setOnTouchListener(this);

        //FragmentManager
        fragmentManager = getSupportFragmentManager();
        fragmentChanger("FRAG_MENU_ROOM");
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.overlayColor:
                goBackAnimation();
                break;

            case R.id.friendButton:
                fragmentChanger("FRAG_MENU_FRIEND");
                goBackAnimation();
                break;

            case R.id.roomButton:
                fragmentChanger("FRAG_MENU_ROOM");
                goBackAnimation();
                break;

            case R.id.roomAddButton:
                fragmentChanger("FRAG_MENU_ROOM_ADD");
                goBackAnimation();
                break;

            case R.id.settingButton:
                //toSetting
                Intent setting = new Intent(ActivityMenu.this, ActivitySetting.class);
                startActivity(setting);

                goBackAnimation();
                break;

            case R.id.flowerImage:
                image_Overlay.setVisibility(View.VISIBLE);

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
                button_Friend.setEnabled(false);

                if (popoutFriend){
                    subMenuGoBackAnimation(view, classId);
                }else {
                    subMenuPopupAnimation(view);
                }
                break;

            case R.id.roomButton:
                button_Room.setEnabled(false);

                if (popoutRoom){
                    subMenuGoBackAnimation(view, classId);
                }else {
                    subMenuPopupAnimation(view);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int defaultButtonColor;

        switch (view.getId()){
            case R.id.friendButton:
            case R.id.friendAddButton:
                defaultButtonColor = Color.parseColor("#f35959");
                break;

            case R.id.roomButton:
            case R.id.roomAddButton:
            case R.id.roomFavButton:
                defaultButtonColor = Color.parseColor("#b6b8e7");
                break;

            case R.id.settingButton:
                defaultButtonColor = Color.parseColor("#655177");
                break;

                default:
                    defaultButtonColor = Color.parseColor("#FFFFFF");
                    break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float[] hsv = new float[3];
                Color.colorToHSV(defaultButtonColor, hsv);
                hsv[2] -= 0.2f;
                view.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));

                break;
            case MotionEvent.ACTION_UP:
                view.setBackgroundTintList(ColorStateList.valueOf(defaultButtonColor));
                break;
        }
        return false;
    }

    //Change_Fragment
    public void fragmentChanger(String fragmentId){
        switch (fragmentId){
            case "FRAG_MENU_FRIEND":
                Fragment fragmentFriend = new menuFriend();
                FragmentTransaction transactionToFriend = fragmentManager.beginTransaction();
                transactionToFriend.setCustomAnimations(
                        R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                transactionToFriend.replace(R.id.menu_fragment, fragmentFriend, "FRAG_MENU_FRIEND");
                transactionToFriend.commit();
                break;

            case "FRAG_MENU_ROOM":
                Fragment fragmentChat = new menuRoom();
                FragmentTransaction transactionToChat = fragmentManager.beginTransaction();
                if (recyCount!=0) {
                    transactionToChat.setCustomAnimations(
                            R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);

                }
                transactionToChat.replace(R.id.menu_fragment, fragmentChat, "FRAG_MENU_ROOM");
                transactionToChat.commit();

                recyCount++;
                break;

            case "FRAG_MENU_ROOM_ADD":
                Fragment fragmentRoomAdd = new menuRoomAdd();
                FragmentTransaction transactionToRoomAdd = fragmentManager.beginTransaction();
                transactionToRoomAdd.setCustomAnimations(
                        R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                transactionToRoomAdd.replace(R.id.menu_fragment, fragmentRoomAdd, "FRAG_MENU_ROOM_ADD");
                transactionToRoomAdd.commit();
                break;
        }
    }

    //Check_Fragment on Screen
    public String checkFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragmentFriend = fragmentManager.findFragmentByTag("FRAG_MENU_FRIEND");
        Fragment fragmentRoom = fragmentManager.findFragmentByTag("FRAG_MENU_ROOM");
        Fragment fragmentRoomAdd = fragmentManager.findFragmentByTag("FRAG_MENU_ROOM_ADD");

        if (fragmentFriend != null && fragmentFriend.isVisible()){
            return "FRAG_MENU_FRIEND";
        }

        if (fragmentRoom != null && fragmentRoom.isVisible()){
            return "FRAG_MENU_ROOM";
        }

        if (fragmentRoom != null && fragmentRoomAdd.isVisible()){
            return "FRAG_MENU_ROOM_ADD";
        }

        return "FRAG_NOT_VISIBLE";
    }

    //------Animation From here------

    //メニューを出す
    private void popupAnimation(){
        View[] viewList_all = new View[]{
                image_Center, button_Friend, button_Room, button_setting
        };

        View[] viewList_button = new View[]{
                button_Room, button_Friend, button_setting
        };

        float distance = 300;
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
                image_Center.setEnabled(true);
                image_Overlay.setEnabled(true);
                button_Friend.setEnabled(true);
                button_Room.setEnabled(true);
                button_setting.setEnabled(true);

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
        image_Center.setEnabled(false);
        image_Overlay.setEnabled(false);
        button_Friend.setEnabled(false);
        button_Room.setEnabled(false);
        button_setting.setEnabled(false);

        String classId = "goBackAnimation";
        if (popoutRoom){
            subMenuGoBackAnimation(button_Room, classId);
            return;
        } else if (popoutFriend){
            subMenuGoBackAnimation(button_Friend, classId);
            return;
        }

        View[] viewList_all = new View[]{
                image_Center, button_Friend, button_Room, button_setting
        };

        View[] viewList_button = new View[]{
                button_Room, button_Friend, button_setting
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
                image_Center.setEnabled(true);
                image_Overlay.setEnabled(true);
                button_Friend.setEnabled(true);
                button_Room.setEnabled(true);
                button_setting.setEnabled(true);

                image_Overlay.setVisibility(View.GONE);

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
                viewList_subButton.add(button_Room_Create);
                viewList_subButton.add(button_Room_Fav);

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
                viewList_subButton.add(button_Friend_Search);

                for (int i=0;i<viewList_subButton.size();i++){
                    viewList_subButton.get(i).setX(lastButtonEndGridX);
                    viewList_subButton.get(i).setY(lastButtonEndGridY);
                    viewList_subButton.get(i).setVisibility(View.VISIBLE);
                }

                popoutFriend = !popoutFriend;
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
                button_Room.setEnabled(true);
                button_Friend.setEnabled(true);
                button_setting.setEnabled(true);
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
                viewList_subButton.add(button_Room_Create);
                viewList_subButton.add(button_Room_Fav);

                popoutRoom = !popoutRoom;
                break;

            case R.id.friendButton:
                mainButtonDegree = 210f;    //親viewの角度

                //サブメニューリスト
                viewList_subButton.add(button_Friend_Search);

                popoutFriend = !popoutFriend;
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

                button_Room.setEnabled(true);
                button_Friend.setEnabled(true);

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
}
