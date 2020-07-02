package com.hiro_a.naruko.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.view.RecyclerView.ProfileView.LinearLayoutAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettingUserProfile extends AppCompatActivity {
    private String TAG = "NARUKO_DEBUG @ ActivitySettingUserProfile";
    private Context context;

    private String userId;
    private String userEmail;
    private boolean userEmailVerified = false;

    private AppBarLayout topbar;
    private TextView title_UserImage;
    private CircleImageView image_UserImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        //コンテキスト
        context = getApplicationContext();

        //アクションバー
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            //戻るボタン追加
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            //取得失敗時ダイアログ表示
            new AlertDialog.Builder(context)
                    .setTitle("エラー")
                    .setMessage("画面情報の取得に失敗しました。")
                    .show();
        }

        //DeviceInfoからユーザー情報を取得
        DeviceInfo userInfo = new DeviceInfo();
        userId = userInfo.getUserId(context);
        String userName = userInfo.getUserName(context);
        userEmail = userInfo.getUserEmail(context);
        userEmailVerified = userInfo.getUserEmailVerified(context);
        boolean userImageIs = userInfo.getUserImageIs(context);
        long userImageUpdateTime = userInfo.getUserImageUpdateTime(context);
        String userColor = userInfo.getUserColor(context);
        Log.d(TAG, "*** User_Info ***");
        Log.d(TAG, "UserName: " + userName);
        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "UserEmail: " + userEmail);
        Log.d(TAG, "UserEmailVerified: " + userEmailVerified);
        Log.d(TAG, "UserImageIs: " + userImageIs);
        Log.d(TAG, "UserImageUpdateTime: " + userImageUpdateTime);
        Log.d(TAG, "UserColor: " + userColor);
        Log.d(TAG, "---------------------------------");

        //背景アップバー
        topbar = (AppBarLayout) findViewById(R.id.profile_appBar_topBar);
        //背景色
        topbar.setBackgroundResource(getResources().getIdentifier("color"+userColor, "color", getPackageName()));

        //ユーザー名表示スペース
        title_UserImage = (TextView)findViewById(R.id.profile_textView_userName);
        title_UserImage.setText(userName);

        //ユーザー画像表示スペース
        image_UserImage = (CircleImageView) findViewById(R.id.profile_imageView_userIcon);
        //枠線色
        image_UserImage.setBorderColor(getResources().getColor(getResources().getIdentifier("color"+userColor+"Light", "color", getPackageName())));
        if (userImageIs){   //ユーザー画像がある場合
            //ユーザー画像パス作成
            String userImage = "Images/UserImages/" + userId + ".jpg";

            //Firebasestorageへのパスを作成
            StorageReference imageStorgeRefarence = FirebaseStorage.getInstance().getReference().child(userImage);

            //Glideを使って表示
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(imageStorgeRefarence)
                    .signature(new StringSignature(String.valueOf(userImageUpdateTime)))
                    .into(image_UserImage);
        }else { //ユーザー画像がない場合
            //デフォルト画像を表示
            image_UserImage.setImageResource(R.drawable.ic_launcher_background);
        }

        settingRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //アクションバーメニューを読み込み
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_profile_menu_edit, menu);

        return true;
    }

    //メニュークリック時
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //戻るボタン
            case android.R.id.home:
                //ユーザー情報編集アクティビティに移行
                Intent settingList = new Intent(ActivitySettingUserProfile.this, ActivitySettingList.class);
                settingList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingList);

                finish();
                return true;

            //編集ボタン
            case R.id.profile_menuItem_edit:
                //ユーザー情報編集アクティビティに移行
                Intent editProfile = new Intent(ActivitySettingUserProfile.this, ActivitySettingUserProfile_Edit.class);
                editProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(editProfile);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //RecyclerView生成
    public void settingRecyclerView(){
        //情報タイトルリスト
        List<String> titleData = new ArrayList<>(Arrays.asList(
                "ユーザーID", "メールアドレス", "メールアドレス認証"
        ));

        //情報リスト
        List<String> userData = new ArrayList<>();

        //ユーザーID
        userData.add(userId);

        //メールアドレス
        if (userEmail == null){ //メールアドレスが設定されていない場合
            userData.add("メールアドレスが登録されていません");
        } else {    //メールアドレスが設定されている場合
            userData.add(userEmail);
        }
        //メールアドレス認証
        if (userEmailVerified){ //認証されている場合
            userData.add("認証済み");
        } else {    //認証されていない場合
            userData.add("認証されていません");
        }

        //RecyclerView設定
        final RecyclerView settingProfileRecyclerView = (RecyclerView)findViewById(R.id.profile_recyclerView_setting);

        //Adapter
        RecyclerView.Adapter adapter = new LinearLayoutAdapter(titleData, userData){
            @Override
            protected void onMenuClicked(@NonNull int position){
                super.onMenuClicked(position);

            }
        };
        settingProfileRecyclerView.setAdapter(adapter);

        //LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ActivitySettingUserProfile.this);
        settingProfileRecyclerView.setLayoutManager(layoutManager);

        //間の線
        RecyclerView.ItemDecoration devideLine = new DividerItemDecoration(ActivitySettingUserProfile.this, DividerItemDecoration.VERTICAL);
        settingProfileRecyclerView.addItemDecoration(devideLine);

        settingProfileRecyclerView.setHasFixedSize(true);
    }
}
