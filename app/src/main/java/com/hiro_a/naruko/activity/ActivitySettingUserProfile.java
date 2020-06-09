package com.hiro_a.naruko.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.MakeStoargeUri;
import com.hiro_a.naruko.view.CustomImageView;
import com.hiro_a.naruko.view.RecyclerView.ProfileView.LinearLayoutAdapter;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettingUserProfile extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "NARUKO_DEBUG @ ActivitySettingUserProfile";
    private Context context;

    private String userId;
    private String userEmail;
    private boolean userEmailVerified = false;
    private boolean imageChanged = false;
    private Uri imageDirectryUri;

    private TextView title_UserImage;
    private CircleImageView image_UserImage;
    private PopupWindow cropImagePopup;

    private CollectionReference userRef;

    private StorageReference storageRefarence;

    private final int STRAGEACCESSFRAMEWORK_REQUEST_CODE = 42;
    private final Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        Log.d(TAG, "*** User_Info ***");
        Log.d(TAG, "UserName: " + userName);
        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "UserEmail: " + userEmail);
        Log.d(TAG, "UserEmailVerified: " + userEmailVerified);
        Log.d(TAG, "userImageIs: " + userImageIs);
        Log.d(TAG, "---------------------------------");

        //ユーザー名表示スペース
        title_UserImage = (TextView)findViewById(R.id.profile_textView_userName);
        title_UserImage.setText(userName);
        title_UserImage.setOnClickListener(this);

        //ユーザー画像表示スペース
        image_UserImage = (CircleImageView) findViewById(R.id.profile_imageView_userIcon);
        if (userImageIs){   //ユーザー画像がある場合
            //ユーザー画像パス作成
            String userImage = "Images/UserImages/" + userId + ".jpg";

            //Firebasestorageへのパスを作成
            StorageReference imageStorgeRefarence = FirebaseStorage.getInstance().getReference().child(userImage);

            //Glideを使って表示
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(imageStorgeRefarence)
                    .into(image_UserImage);
        }else { //ユーザー画像がない場合
            //デフォルト画像を表示
            image_UserImage.setImageResource(R.drawable.ic_launcher_background);
        }

        //画像変更ボタン
        ImageView image_ImageChanger = (ImageView) findViewById(R.id.profile_imageView_chageImage);
        image_ImageChanger.setOnClickListener(this);

        //*** Firebase ***
        //Firestore
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        //ユーザーレファレンス
        userRef = firebaseFirestore.collection("users");
        //ストレージレファレンス
        storageRefarence = FirebaseStorage.getInstance().getReference();

        settingRecyclerView();
    }

    //アクションバーメニューを読み込み
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_profile_menu, menu);

        return true;
    }

    //メニュークリック時
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //戻るボタン
            case R.id.home:
                //アクティビティを終了して戻る
                finish();
                return true;

            //保存ボタン
            case R.id.profile_menuItem_save:
                //変更されたデータを保存
                //画像が変更されていた場合
                if (imageChanged){
                    changeUserImage();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //ユーザー名表示スペース
            case R.id.profile_textView_userName:
                //ユーザー名変更
                changeUserName();
                break;

             //ユーザー画像変更ボタン
            case R.id.profile_imageView_chageImage:
                //ストレージアクセスフレームワーク
                Intent strageAccessFramework = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                //開くことのできるファイルに限定
                strageAccessFramework.addCategory(Intent.CATEGORY_OPENABLE);
                //画像ファイルに限定
                strageAccessFramework.setType("image/*");
                //ストレージアクセス開始
                startActivityForResult(strageAccessFramework, STRAGEACCESSFRAMEWORK_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //ストレージアクセスフレームワークからの返り値だった場合
        if (requestCode == STRAGEACCESSFRAMEWORK_REQUEST_CODE) {
            //データがnullでない場合
            if (data != null) {
                //ファイルのUriを取得
                Uri imageUri = data.getData();

                //DeviceInfoから画面情報を取得
                DeviceInfo userInfo = new DeviceInfo();
                float screenHeight = userInfo.getScreenHeight(context);

                //画像切り取り画面表示場所
                CoordinatorLayout coordinatorLayout = findViewById(R.id.profile_layout_main);

                //画像切り取り画面表示・画像保存先Uri取得
                createCropView(coordinatorLayout, imageUri, (int) screenHeight);
            }
        }
    }

    //ユーザー名変更
    private void changeUserName(){

    }

    //ユーザー画像をFirestoreに送信
    public void changeUserImage(){
        Log.d(TAG, this.imageDirectryUri.toString());
        Log.d(TAG, "---------------------------------");

        //プログレスダイアログ
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("ユーザー画像更新中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //画像のパス作成
        StorageReference uploadImageRef = storageRefarence.child("Images/UserImages/" + userId + ".jpg");

        //Firestoreに送信
        UploadTask uploadTask = uploadImageRef.putFile(imageDirectryUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                progressDialog.dismiss();

                Log.d(TAG, "SCSESS adding UserImage to Database");
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

                Log.w(TAG, "Error adding UserImage to Database", e);
                Log.w(TAG, "---------------------------------");
            }
        });
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

    //-------以下切り取りビュー設定-------
    //画像切り取りビュー表示
    public void createCropView(View parentView, final Uri imageUri, int screenHeight) {
        final Uri imageDirectoryUri = new MakeStoargeUri().makeNewUri(context);

        //CropImageViewウィンドウのビュー
        View view = this.getLayoutInflater().inflate(R.layout.fragment_menu_room_create_popup, null);

        //CropImageViewウィンドウ設定
        cropImagePopup = new PopupWindow(context);
        cropImagePopup.setContentView(view);
        cropImagePopup.setOutsideTouchable(true);
        cropImagePopup.setFocusable(true);

        //横幅
        cropImagePopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        //高さ
        cropImagePopup.setHeight((screenHeight / 3) * 2);

        //位置
        cropImagePopup.showAtLocation(parentView, Gravity.CENTER, 0, (screenHeight / 2) + (screenHeight / 3));

        //CropImageView
        final com.isseiaoki.simplecropview.CropImageView cropImageView = (com.isseiaoki.simplecropview.CropImageView) view.findViewById(R.id.menu_roomRegister_cropImage);
        cropImageView.load(imageUri).execute(mLoadCallback);

        //画像時計方向回転
        CustomImageView cropRotateRight = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropRotateRight);
        cropRotateRight.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
        cropRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_90D);
            }
        });

        //画像反時計方向回転
        CustomImageView cropRotateLeft = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropRotateLeft);
        cropRotateLeft.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
        cropRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_M90D);
            }
        });

        //切り取りボタン
        CustomImageView cropButton = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropFinish);
        cropButton.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.crop(imageUri).outputHeight(300).outputMaxWidth(300).execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {
                        //画像を切り取り
                        cropImageView.save(cropped)
                                .compressFormat(mCompressFormat)
                                .execute(imageDirectoryUri, mSaveCallback);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }
        });
    }

    //↓以下コールバック
    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override public void onSuccess() {
        }

        @Override public void onError(Throwable e) {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override public void onSuccess(Uri outputUri) {
            //CropImageViewウィンドウを閉じる
            cropImagePopup.dismiss();

            //切り取った画像をプレビューImageViewに表示
            image_UserImage.setImageURI(outputUri);

            //結果のUriをグローバル変数に入れる
            imageDirectryUri = outputUri;

            //画像変更の有無をtrueに
            imageChanged = true;
        }

        @Override public void onError(Throwable e) {

        }
    };
}
