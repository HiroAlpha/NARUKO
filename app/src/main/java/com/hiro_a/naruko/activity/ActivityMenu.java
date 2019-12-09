package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuChatData;
import com.hiro_a.naruko.item.MenuItem;
import com.hiro_a.naruko.view.MenuRecyclerView.MenuAdapter;
import com.hiro_a.naruko.view.MenuRecyclerView.MenuLayoutManger;

import java.util.ArrayList;
import java.util.List;

public class ActivityMenu extends AppCompatActivity implements View.OnClickListener{
    int screenWidth, screenHeight;
    boolean popout = false;

    TextView mFlowerImage;
    ImageView mOverlayColor;
    MenuItem mFriendButton, mNarukoButton, mSettingButton;

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

        mFriendButton = (MenuItem) findViewById(R.id.friendButton);
        mFriendButton.setOnClickListener(this);

        mNarukoButton = (MenuItem) findViewById(R.id.chatButton);
        mNarukoButton.setOnClickListener(this);

        mSettingButton = (MenuItem) findViewById(R.id.settingButton);
        mSettingButton.setOnClickListener(this);

        RecyclerView menuChatRecyclerView = (RecyclerView)findViewById(R.id.menuChatRecyclerView);
        MenuAdapter adapter = new MenuAdapter(this.createDataset()){
            @Override
            protected void onMenuClicked(@NonNull String title){
                super.onMenuClicked(title);
                Toast.makeText(ActivityMenu.this, title, Toast.LENGTH_SHORT).show();
                Intent chat = new Intent(ActivityMenu.this, ActivityChat.class);
                startActivity(chat);
            }
        };

        MenuLayoutManger layoutManager = new MenuLayoutManger();

        menuChatRecyclerView.setHasFixedSize(true);
        menuChatRecyclerView.setLayoutManager(layoutManager);
        menuChatRecyclerView.setAdapter(adapter);

    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.friendButton:
                goBackAnimation();
                popout = !popout;
                break;

            case R.id.chatButton:
                goBackAnimation();
                popout = !popout;
                break;

            case R.id.settingButton:
                //Toast.makeText(this, "clicked!", Toast.LENGTH_LONG).show();
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

    private List<MenuChatData> createDataset(){
        List<MenuChatData> dataList = new ArrayList<>();
        for (int i=0; i<3; i++){
            MenuChatData data = new MenuChatData();
            data.setInt(R.drawable.ic_launcher_background);
            data.setString("グループ" + i);

            dataList.add(data);
        }
        return dataList;
    }

    //メニューを出す
    private void popupAnimation(){
        View[] viewList_all = new View[]{
                mFlowerImage, mFriendButton, mNarukoButton, mSettingButton
        };

        View[] viewList_button = new View[]{
                mNarukoButton, mFriendButton, mSettingButton
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

            float endGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float endGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", 0f, endGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -(float)(screenHeight/3), -endGridY );

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
                mFlowerImage, mFriendButton, mNarukoButton, mSettingButton
        };

        View[] viewList_button = new View[]{
                mNarukoButton, mFriendButton, mSettingButton
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

            float endGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float endGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(float)(screenHeight/3));

            //X, Yを0からendGridへ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", endGridX, 0f );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -endGridY, -(float)(screenHeight/3));

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
}
