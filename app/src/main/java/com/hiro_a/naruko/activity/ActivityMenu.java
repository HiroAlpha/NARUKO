package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.fragment.MenuFriend;
import com.hiro_a.naruko.fragment.MenuRoom;
import com.hiro_a.naruko.fragment.MenuRoomAdd;
import com.hiro_a.naruko.item.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ActivityMenu extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    private String TAG = "NARUKO_DEBUG @ ActivityMenu";

    private int recyCount = 0;
    private float screenHeight;
    private boolean mainMenu_out = false;
    private boolean friendMenu_out = false;
    private boolean roomMenu_out = false;

    private ImageView image_Center, image_Overlay;
    private MenuItem button_Room, button_Room_Create, button_Room_Fav;
    private MenuItem button_Friend, button_Friend_Search;
    private MenuItem button_setting;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //コンテキスト
        Context context = getApplicationContext();

        //DeviceInfoから画面情報を取得
        DeviceInfo userInfo = new DeviceInfo();
        screenHeight = userInfo.getScreenHeight(context);

        //半透明背景
        image_Overlay = findViewById(R.id.menu_imageView_background);
        image_Overlay.setVisibility(View.GONE);
        image_Overlay.setOnClickListener(this);

        //中央の画像
        image_Center = findViewById(R.id.menu_imageView_center);
        image_Center.setOnClickListener(this);

        //ルーム関連ボタン
        button_Room = findViewById(R.id.menu_view_room);
        button_Room.setOnClickListener(this);
        button_Room.setOnLongClickListener(this);
        button_Room.setOnTouchListener(this);

        button_Room_Create = findViewById(R.id.menu_view_room_create);
        button_Room_Create.setVisibility(View.GONE);
        button_Room_Create.setOnClickListener(this);
        button_Room_Create.setOnTouchListener(this);

        button_Room_Fav = findViewById(R.id.menu_view_room_favorit);
        button_Room_Fav.setVisibility(View.GONE);
        button_Room_Fav.setOnClickListener(this);
        button_Room_Fav.setOnTouchListener(this);

        //フレンド関連ボタン
        button_Friend = findViewById(R.id.menu_view_friend);
        button_Friend.setOnClickListener(this);
        button_Friend.setOnLongClickListener(this);
        button_Friend.setOnTouchListener(this);

        button_Friend_Search = findViewById(R.id.menu_view_friend_add);
        button_Friend_Search.setVisibility(View.GONE);
        button_Friend_Search.setOnClickListener(this);
        button_Friend_Search.setOnTouchListener(this);

        //設定関連ボタン
        button_setting = findViewById(R.id.menu_view_setting);
        button_setting.setOnClickListener(this);
        button_setting.setOnTouchListener(this);

        //フラグメントマネージャー
        fragmentManager = getSupportFragmentManager();
        fragmentChanger("FRAG_MENU_ROOM");
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            //半透明背景
            case R.id.menu_imageView_background:
                //収納アニメーション
                goBackAnimation();
                break;

            //お気に入りルームボタン
            case R.id.menu_view_room:
                //お気に入りルーム画面に切り替え
                fragmentChanger("FRAG_MENU_ROOM");

                //収納アニメーション
                goBackAnimation();
                break;

            //ルーム作成ボタン
            case R.id.menu_view_room_create:
                //ルーム作成画面に切り替え
                fragmentChanger("FRAG_MENU_ROOM_CREATE");

                //収納アニメーション
                goBackAnimation();
                break;

            //フレンドボタン
            case R.id.menu_view_friend:
                //フレンド画面に切り替え
                fragmentChanger("FRAG_MENU_FRIEND");

                //収納アニメーション
                goBackAnimation();
                break;

            //設定ボタン
            case R.id.menu_view_setting:
                //設定アクティビティに移行
                Intent setting = new Intent(ActivityMenu.this, ActivitySettingList.class);
                startActivity(setting);

                //収納アニメーション
                goBackAnimation();
                break;

            //中央の画像
            case R.id.menu_imageView_center:
                //半透明背景を表示
                image_Overlay.setVisibility(View.VISIBLE);

                if (mainMenu_out){    //メニューが展開されている場合
                    //収納アニメーション
                    goBackAnimation();
                }
                if (!mainMenu_out){   //メニューが収納されている場合
                    //展開アニメーション
                    popupAnimation();
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view){
        Log.w(TAG, "onLongClick");
        //クラスID
        String classId = "onLongClick";
        switch (view.getId()) {
            //フレンドボタン
            case R.id.menu_view_friend:
                if (friendMenu_out){  //フレンドのサブボタンが展開されている場合
                    //サブメニュー収納アニメーション
                    subMenuGoBackAnimation(view, classId);
                }else { //フレンドのサブボタンが収納されている場合
                    //サブメニュー展開アニメーション
                    subMenuPopupAnimation(view);
                }
                break;

            //お気に入りルームボタン
            case R.id.menu_view_room:
                Log.w(TAG, "@ room");
                Log.w(TAG, "---------------------------------");
                if (roomMenu_out){    //ルームのサブボタンが展開されている場合
                    //サブメニュー収納アニメーション
                    subMenuGoBackAnimation(view, classId);
                }else { //ルームのサブボタンが収納されている場合
                    //サブメニュー展開アニメーション
                    subMenuPopupAnimation(view);
                }
                break;
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //タッチされたボタンの色
        int defaultButtonColor = Color.parseColor("#b6b8e7");
        switch (view.getId()){
            //ルーム関連ボタン
            case R.id.menu_view_room:
            case R.id.menu_view_room_create:
            case R.id.menu_view_room_favorit:
                //色を設定
                defaultButtonColor = Color.parseColor("#b6b8e7");
                break;

            //フレンド関連ボタン
            case R.id.menu_view_friend:
            case R.id.menu_view_friend_add:
                //色を設定
                defaultButtonColor = Color.parseColor("#f35959");
                break;

            //設定関連ボタン
            case R.id.menu_view_setting:
                //色を設定
                defaultButtonColor = Color.parseColor("#655177");
                break;
        }

        switch (event.getAction()) {
            //触れている場合
            case MotionEvent.ACTION_DOWN:
                //色を少し暗くする
                float[] hsv = new float[3];
                Color.colorToHSV(defaultButtonColor, hsv);
                hsv[2] -= 0.2f;
                view.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));

                break;

            //離された場合
            case MotionEvent.ACTION_UP:
                //元の色に戻す
                view.setBackgroundTintList(ColorStateList.valueOf(defaultButtonColor));
                break;
        }
        return false;
    }

    //画面切り替え
    private void fragmentChanger(String fragmentId){
        switch (fragmentId){
            //フレンド画面に切り替え
            case "FRAG_MENU_FRIEND":
                Fragment fragmentFriend = new MenuFriend();

                //切替アニメーション
                FragmentTransaction transactionToFriend = fragmentManager.beginTransaction();
                transactionToFriend.setCustomAnimations(
                        R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                transactionToFriend.replace(R.id.menu_layout_fragmentContainer, fragmentFriend, "FRAG_MENU_FRIEND");
                transactionToFriend.commit();
                break;

            //お気に入りルーム画面に切り替え
            case "FRAG_MENU_ROOM":
                Fragment fragmentChat = new MenuRoom();

                //切替アニメーション
                FragmentTransaction transactionToChat = fragmentManager.beginTransaction();
                //表示2回目以降はアニメーションを実行
                if (recyCount!=0) {
                    transactionToChat.setCustomAnimations(
                            R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);

                }
                transactionToChat.replace(R.id.menu_layout_fragmentContainer, fragmentChat, "FRAG_MENU_ROOM");
                transactionToChat.commit();

                recyCount++;
                break;

            //ルーム作成画面に切り替え
            case "FRAG_MENU_ROOM_CREATE":
                Fragment fragmentRoomAdd = new MenuRoomAdd();

                //切替アニメーション
                FragmentTransaction transactionToRoomAdd = fragmentManager.beginTransaction();
                transactionToRoomAdd.setCustomAnimations(
                        R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                transactionToRoomAdd.replace(R.id.menu_layout_fragmentContainer, fragmentRoomAdd, "FRAG_MENU_ROOM_ADD");
                transactionToRoomAdd.commit();
                break;
        }
    }

    //------以下アニメーション------

    //メニューを出す
    private void popupAnimation(){
        //中央の画像を無効化
        image_Center.setEnabled(false);
        //半透明背景を無効化
        image_Overlay.setEnabled(false);
        //フレンドボタンを無効化
        button_Friend.setEnabled(false);
        //ルームボタンを無効化
        button_Room.setEnabled(false);
        //設定ボタンを無効化
        button_setting.setEnabled(false);

        //全ビュー
        View[] viewList_all = new View[]{
                image_Center, button_Friend, button_Room, button_setting
        };

        //切替ボタンビュー
        View[] viewList_button = new View[]{
                button_Room, button_Friend, button_setting
        };

        //中心からの移動距離
        float distance = 300;
        //全ビューの個数
        int all_viewNum = viewList_all.length;
        //ボタンビューの個数
        int button_viewNum = viewList_button.length;
        //アニメーターリスト
        List<Animator> animatorList_toCenter = new ArrayList<>();
        List<Animator> animatorList_button = new ArrayList<>();

        //中央への移動アニメーション
        ObjectAnimator[] toCenterAnim = new ObjectAnimator[all_viewNum];
        for (int i=0;i<all_viewNum;i++){
            //対象のビュー
            View target = viewList_all[i];

            //Yを0から画面の下から1/3へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationY", 0f, -(screenHeight/3));

            toCenterAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX);
            toCenterAnim[i].setDuration(700);
            animatorList_toCenter.add(toCenterAnim[i]);
        }
        AnimatorSet toCenterSet = new AnimatorSet();
        //全ビューを同時にアニメーション
        toCenterSet.playTogether(animatorList_toCenter);
        toCenterSet.start();

        //切替ボタンの拡散アニメーション
        ObjectAnimator[] buttonAnim = new ObjectAnimator[button_viewNum];
        for (int i=0;i<button_viewNum;i++){
            //90度を0としてボタンの角度
            int degree = (i * 360 / button_viewNum)+90;
            //対象のビュー
            View target = viewList_button[i];

            //ボタンの最終位置（指定した角度に指定距離分移動させたもの）
            float mainButtonEndGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float mainButtonEndGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(screenHeight/3));

            //X, Yを0から最終位置へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", 0f, mainButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -(screenHeight/3), -mainButtonEndGridY );

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(350);
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        //中央への移動アニメーション分だけディレイ
        buttonSet.setStartDelay(700);
        //ビューを順番にアニメーション
        buttonSet.playSequentially(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //アニメーション終了時
                //半透明背景の無効化を解除
                image_Overlay.setEnabled(true);
                //中央の画像の無効化を解除
                image_Center.setEnabled(true);
                //ルームボタンの無効化を解除
                button_Room.setEnabled(true);
                //フレンドボタンの無効化を解除
                button_Friend.setEnabled(true);
                //設定ボタンの無効化を解除
                button_setting.setEnabled(true);

                //メニューの展開をtrueに
                mainMenu_out = true;
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

    //メニュー収納アニメーション
    private void goBackAnimation(){
        //中央の画像を無効化
        image_Center.setEnabled(false);
        //半透明背景を無効化
        image_Overlay.setEnabled(false);
        //フレンドボタンを無効化
        button_Friend.setEnabled(false);
        //ルームボタンを無効化
        button_Room.setEnabled(false);
        //設定ボタンを無効化
        button_setting.setEnabled(false);

        //クラスID
        String classId = "goBackAnimation";

        if (roomMenu_out){    //ルームボタンのサブボタンが展開されていた場合
            //サブボタン収納アニメーション（ルームボタン）
            subMenuGoBackAnimation(button_Room, classId);

            //メニュー収納アニメーションをスキップ
            return;
        } else if (friendMenu_out){   //フレンドボタンのサブボタンが展開されていた場合
            //サブボタン収納アニメーション（フレンドボタン）
            subMenuGoBackAnimation(button_Friend, classId);

            //メニュー収納アニメーションをスキップ
            return;
        }

        //全ビュー
        View[] viewList_all = new View[]{
                image_Center, button_Friend, button_Room, button_setting
        };

        //切替ボタンビュー
        View[] viewList_button = new View[]{
                button_Room, button_Friend, button_setting
        };

        //中心からの移動距離
        float distance = 300;
        //全ビューの個数
        int all_viewNum = viewList_all.length;
        //ボタンビューの個数
        int button_viewNum = viewList_button.length;
        //アニメーターリスト
        List<Animator> animatorList_toCenter = new ArrayList<>();
        List<Animator> animatorList_button = new ArrayList<>();

        //切替ボタン収納アニメーション
        ObjectAnimator[] buttonAnim = new ObjectAnimator[button_viewNum];
        for (int i=0;i<button_viewNum;i++){
            //90度を0としてボタンの角度
            int degree = (i * 360 / button_viewNum)+90;
            //対象ビュー
            View target = viewList_button[i];

            //ボタンの最終位置（指定した角度に指定距離分移動させたもの）の逆方向
            float mainButtonEndGridX = (float)(distance * Math.cos(Math.toRadians(degree)));
            float mainButtonEndGridY = (float)(distance * Math.sin(Math.toRadians(degree))+(screenHeight/3));

            //X, Yを最終位置から0へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", mainButtonEndGridX, 0f );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -mainButtonEndGridY, -(screenHeight/3));

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(500);
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        //全切替ボタンを同時に収納
        buttonSet.playTogether(animatorList_button);
        buttonSet.start();

        //中央から画面下への収納アニメーション
        ObjectAnimator[] toCenterAnim = new ObjectAnimator[all_viewNum];
        for (int i=0;i<all_viewNum;i++){
            //対象ビュー
            View target = viewList_all[i];

            //Yを画面の下から1/3から0へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationY", -(screenHeight/3), 0f);

            toCenterAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX);
            toCenterAnim[i].setDuration(700);
            animatorList_toCenter.add(toCenterAnim[i]);
        }
        AnimatorSet toCenterSet = new AnimatorSet();
        //切替ボタン収納アニメーションの分だけディレイ
        toCenterSet.setStartDelay(500);
        //全ビューを同時に収納
        toCenterSet.playTogether(animatorList_toCenter);
        toCenterSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //アニメーション終了時
                //半透明背景の無効化を解除
                image_Overlay.setEnabled(true);
                //中央の画像の無効化を解除
                image_Center.setEnabled(true);
                //ルームボタンの無効化を解除
                button_Room.setEnabled(true);
                //フレンドボタンの無効化を解除
                button_Friend.setEnabled(true);
                //設定ボタンの無効化を解除
                button_setting.setEnabled(true);

                //半透明背景を消去
                image_Overlay.setVisibility(View.GONE);

                //メニューの展開をfalseに
                mainMenu_out = false;
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

    //サブボタン展開アニメーション
    private void subMenuPopupAnimation(View view){
        //ルームボタンを無効化
        button_Room.setEnabled(false);
        //フレンドボタンを無効化
        button_Friend.setEnabled(false);
        //設定ボタンを無効化
        button_setting.setEnabled(false);

        //親ボタン角度
        float mainButtonDegree = 0;

        //親ボタン・前サブボタン位置
        float lastButtonEndGridX = 0;
        float lastButtonEndGridY = 0;

        //サブボタンリスト
        List<View> viewList_subButton = new ArrayList<>();

        //ビュー確認
        switch (view.getId()) {
            //ルームボタン
            case R.id.menu_view_room:
                //親ボタン角度を設定
                mainButtonDegree = 90f;

                //親ボタン位置を設定
                lastButtonEndGridX = (float) (300 * Math.cos(Math.toRadians(mainButtonDegree)));
                lastButtonEndGridY = (float) (300 * Math.sin(Math.toRadians(mainButtonDegree)) + (screenHeight / 3));

                //サブボタンをリストに追加
                viewList_subButton.add(button_Room_Create);
                viewList_subButton.add(button_Room_Fav);

                //サブボタンを設定
                for (int i=0;i<viewList_subButton.size();i++){
                    //サブボタンの位置を設定
                    viewList_subButton.get(i).setX(lastButtonEndGridX);
                    viewList_subButton.get(i).setY(-lastButtonEndGridY);

                    //サブボタンを表示
                    viewList_subButton.get(i).setVisibility(View.VISIBLE);
                }

                //ルームサブボタン展開true/falseを逆に
                roomMenu_out = !roomMenu_out;
                break;

            case R.id.menu_view_friend:
                //親ボタン角度を設定
                mainButtonDegree = 210f;

                //親ボタン位置を設定
                lastButtonEndGridX = (float) (300 * Math.cos(Math.toRadians(mainButtonDegree)));
                lastButtonEndGridY = (float) (300 * Math.sin(Math.toRadians(mainButtonDegree)) + (screenHeight / 3));

                //サブボタンをリストに追加
                viewList_subButton.add(button_Friend_Search);

                //サブボタンを設定
                for (int i=0;i<viewList_subButton.size();i++){
                    //サブボタンの位置を設定
                    viewList_subButton.get(i).setX(lastButtonEndGridX);
                    viewList_subButton.get(i).setY(lastButtonEndGridY);

                    //サブボタンを表示
                    viewList_subButton.get(i).setVisibility(View.VISIBLE);
                }

                //フレンドサブボタン展開true/falseを逆に
                friendMenu_out = !friendMenu_out;
                break;
        }

        //サブボタンの個数
        int button_viewNum = viewList_subButton.size();
        //アニメーターリスト
        List<Animator> animatorList_button = new ArrayList<>();

        ObjectAnimator[] buttonAnim = new ObjectAnimator[button_viewNum];
        for (int i=0;i<button_viewNum;i++){
            //サブボタン角度
            float degree = mainButtonDegree + 30*(i+1);
            //対象ビュー
            View target = viewList_subButton.get(i);


            //サブボタン最終地点
            float subButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(degree)));
            float subButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(degree))+(screenHeight/3));

            //X, Yを0から最終地点へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", lastButtonEndGridX, subButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -lastButtonEndGridY, -subButtonEndGridY);

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(250);
            animatorList_button.add(buttonAnim[i]);

            //次のボタン用に前サブボタンの位置を設定
            lastButtonEndGridX = subButtonEndGridX;
            lastButtonEndGridY = subButtonEndGridY;
        }

        AnimatorSet buttonSet = new AnimatorSet();
        //サブボタンを順々に展開
        buttonSet.playSequentially(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //アニメーション終了時
                //ルームボタンの無効化を解除
                button_Room.setEnabled(true);
                //フレンドボタンの無効化を解除
                button_Friend.setEnabled(true);
                //設定ボタンの無効化を解除
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

    //サブボタン収納アニメーション
    private void subMenuGoBackAnimation(View view, final String classId){
        //ルームボタンを無効化
        button_Room.setEnabled(false);
        //フレンドボタンを無効化
        button_Friend.setEnabled(false);
        //設定ボタンを無効化
        button_setting.setEnabled(false);

        //親ボタン角度
        float mainButtonDegree = 0;

        //サブボタンリスト
        final List<View> viewList_subButton = new ArrayList<>();

        switch (view.getId()) {
            //ルームボタン
            case R.id.menu_view_room:
                //親ボタン角度の設定
                mainButtonDegree = 90f;

                //サブボタンをリストに追加
                viewList_subButton.add(button_Room_Create);
                viewList_subButton.add(button_Room_Fav);

                //ルームサブボタン展開true/falseを逆に
                roomMenu_out = !roomMenu_out;
                break;

            case R.id.menu_view_friend:
                //親ボタン角度の設定
                mainButtonDegree = 210f;

                //サブボタンをリストに追加
                viewList_subButton.add(button_Friend_Search);

                //ルームサブボタン展開true/falseを逆に
                friendMenu_out = !friendMenu_out;
                break;
        }

        //親ボタン位置
        float mainButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(mainButtonDegree)));
        float mainButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(mainButtonDegree))+(screenHeight/3));

        //サブボタンの個数
        int button_viewNum = viewList_subButton.size();
        //アニメーターリスト
        List<Animator> animatorList_button = new ArrayList<>();

        ObjectAnimator[] buttonAnim = new ObjectAnimator[button_viewNum];
        for (int i=0;i<button_viewNum;i++){
            //サブボタン角度
            float degree = mainButtonDegree + 30*(i+1);
            //対象ビュー
            View target = viewList_subButton.get(i);

            //サブボタン最終地点
            float subButtonEndGridX = (float)(300 * Math.cos(Math.toRadians(degree)));
            float subButtonEndGridY = (float)(300 * Math.sin(Math.toRadians(degree))+(screenHeight/3));

            //X, Yを最終地点から親ボタンの場所へ
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat( "translationX", subButtonEndGridX, mainButtonEndGridX );
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat( "translationY", -subButtonEndGridY, -mainButtonEndGridY);

            buttonAnim[i] = ObjectAnimator.ofPropertyValuesHolder(target, holderX, holderY);
            buttonAnim[i].setDuration(250 + (100*i));
            animatorList_button.add(buttonAnim[i]);
        }

        AnimatorSet buttonSet = new AnimatorSet();
        //全サブボタンを同時に収納
        buttonSet.playTogether(animatorList_button);
        buttonSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //アニメーション終了時
                for (int i=0;i<viewList_subButton.size();i++){
                    //それぞれのサブボタンを消去
                    viewList_subButton.get(i).setVisibility(View.GONE);
                }

                //ルームボタンの無効化を解除
                button_Room.setEnabled(true);

                //フレンドボタンの無効化を解除
                button_Friend.setEnabled(true);

                //メニュー終了アニメーション中だった場合
                if (classId.equals("goBackAnimation")){
                    //メニュー終了アニメーションを再度実行
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
