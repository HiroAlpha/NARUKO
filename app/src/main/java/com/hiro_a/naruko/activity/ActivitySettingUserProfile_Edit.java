package com.hiro_a.naruko.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.MakeStoargeUri;
import com.hiro_a.naruko.view.CustomImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettingUserProfile_Edit extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "NARUKO_DEBUG @ ActivitySettingUserProfile_edit";
    private Context context;

    private String userId;
    private String userName_original;
    private String userName_new;
    private String userColor_original;
    private String userColor_new;
    private boolean imageChanged = false;
    private Uri imageDirectryUri;

    FrameLayout frameLayout_userColor;
    CircleImageView imageView_userImage;
    EditText editText_userName;
    private PopupWindow cropImagePopup;

    private DocumentReference userRef;
    private StorageReference storageRefarence;

    private final int STRAGEACCESSFRAMEWORK_REQUEST_CODE = 42;
    private final Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile_edit);

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
        userName_original = userInfo.getUserName(context);
        boolean userImageIs = userInfo.getUserImageIs(context);
        long userImageUpdateTime = userInfo.getUserImageUpdateTime(context);
        userColor_original = userInfo.getUserColor(context);
        userColor_new = userColor_original;
        Log.d(TAG, "*** User_Info ***");
        Log.d(TAG, "UserName: " + userName_original);
        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "UserImageIs: " + userImageIs);
        Log.d(TAG, "UserImageUpdateTime: " + userImageUpdateTime);
        Log.d(TAG, "UserColor: " + userColor_original);
        Log.d(TAG, "---------------------------------");

        //ユーザーカラー表示スペース
        frameLayout_userColor = findViewById(R.id.profileEdit_layout_color);
        //背景色
        frameLayout_userColor.setBackgroundResource(getResources().getIdentifier("color"+userColor_original, "color", getPackageName()));

        //ユーザーカラー変更ボタン
        ImageView imageView_editColor = findViewById(R.id.profileEdit_imageView_editColor);
        imageView_editColor.setOnClickListener(this);

        //ユーザー画像表示スペース
        imageView_userImage = findViewById(R.id.profileEdit_imageView_userImage);
        //枠線色
        imageView_userImage.setBorderColor(getResources().getColor(getResources().getIdentifier("color"+userColor_original+"Light", "color", getPackageName())));
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
                    .into(imageView_userImage);
        }else { //ユーザー画像がない場合
            //デフォルト画像を表示
            imageView_userImage.setImageResource(R.drawable.ic_launcher_background);
        }

        //ユーザー画像変更ボタン
        ImageView imageView_editImage = findViewById(R.id.profileEdit_imageView_editImage);
        imageView_editImage.setOnClickListener(this);

        //ユーザー名変更スペース
        editText_userName = findViewById(R.id.profileEdit_editText_userName);
        editText_userName.setText(userName_original);

        //*** Firebase ***
        //Firestore
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        //ユーザーレファレンス
        userRef = firebaseFirestore.collection("users").document(userId);
        //ストレージレファレンス
        storageRefarence = FirebaseStorage.getInstance().getReference();
    }

    //アクションバーメニューを読み込み
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_profile_menu_save, menu);

        return true;
    }

    //メニュークリック時
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //戻るボタン
            case android.R.id.home:
                //ユーザー情報アクティビティに戻る
                Intent profile = new Intent(ActivitySettingUserProfile_Edit.this, ActivitySettingUserProfile.class);
                profile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(profile);
                return true;

            //保存ボタン
            case R.id.profile_menuItem_save:
                //書き込み内容に不備がない場合
                if (formChecker()){
                    //変更されたデータを保存
                    changeUserInfo();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //ユーザーカラー変更ボタン
            case R.id.profileEdit_imageView_editColor:
                //ユーザーカラー
                String[] color = {
                        "Yuuna",
                        "Tougou",
                        "Huu",
                        "Itsuki",
                        "Karin"
                };
                //ランダムな色に変更
                Random random = new Random();
                userColor_new = color[random.nextInt(5)];

                //背景色変更
                frameLayout_userColor.setBackgroundResource(getResources().getIdentifier("color"+userColor_new, "color", getPackageName()));
                //枠線色変更
                imageView_userImage.setBorderColor(getResources().getColor(getResources().getIdentifier("color"+userColor_new+"Light", "color", getPackageName())));

                break;

            //ユーザー画像変更ボタン
            case R.id.profileEdit_imageView_editImage:
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
                LinearLayout linearLayout = findViewById(R.id.profileEdit_layout_main);

                //画像切り取り画面表示・画像保存先Uri取得
                createCropView(linearLayout, imageUri, (int) screenHeight);
            }
        }
    }

    //フォーム内容確認
    private boolean formChecker(){
        //フォーム内容判定の仮設定
        boolean check = true;

        //ユーザー名が空の場合
        if (TextUtils.isEmpty(editText_userName.getText().toString())){
            //エラーメッセージ
            editText_userName.setError("ユーザー名が入力されていません");
            //判定false
            check = false;
        }

        return check;
    }

    //ユーザー情報を変更しFirestoreに送信
    public void changeUserInfo(){
        //プログレスダイアログ
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("ユーザー情報更新中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //SharedPreferences
        final SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = userData.edit();

        //送信内容
        final Map<String, Object> userInfo = new HashMap<>();

        //更新日時
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String userLastUpdated = SD.format(new Date());
        editor.putString("UserLastUpdated", userLastUpdated);
        userInfo.put("UserLastUpdated", userLastUpdated);

        //ユーザー画像が変更されていた場合
        if (imageChanged){
            //sharedPrefarencesの変更項目にユーザー画像の有無を追加
            editor.putBoolean("UserImageIs", true);

            //送信内容にユーザー画像の有無を追加
            userInfo.put("UserImageIs", true);

            Log.d(TAG, "UserImage Changed");
        }

        //ユーザー名が変更されていた場合
        userName_new = editText_userName.getText().toString();
        if (!userName_original.equals(userName_new)){
            //sharedPrefarencesの変更項目にユーザー名を追加
            editor.putString("UserName", userName_new);

            //送信内容にユーザー名を追加
            userInfo.put("UserName", userName_new);

            Log.d(TAG, "UserName Changed");
        }

        //ユーザーカラーが変更されていた場合
        if (!userColor_original.equals(userColor_new)){
            //sharedPrefarencesの変更項目にユーザー名を追加
            editor.putString("UserColor", userColor_new);

            //送信内容にユーザー名を追加
            userInfo.put("UserColor", userColor_new);

            Log.d(TAG, "UserColor Changed");
        }

        //どれかが変更されていた場合
        if (!userColor_original.equals(userColor_new) || !userName_original.equals(userName_new) || imageChanged){
            //Firestoreに送信
            userRef.set(userInfo, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void done) {
                    //画像が変更されていた場合
                    if (imageChanged){
                        //画像のパス作成
                        final StorageReference uploadImageRef = storageRefarence.child("Images/UserImages/" + userId + ".jpg");

                        //Firestorageに画像を送信
                        UploadTask uploadTask = uploadImageRef.putFile(imageDirectryUri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //メタデータ取得
                                uploadImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        //作成日時を取得
                                        long createdDatetime = storageMetadata.getCreationTimeMillis();

                                        //sharedPrefarencesの変更項目に画像更新日時を追加
                                        editor.putLong("UserImageUpdateTime", createdDatetime);
                                        //sharedPrefarences更新
                                        editor.apply();

                                        imageChanged = false;

                                        //更新完了トースト
                                        Toast.makeText(context, "更新完了", Toast.LENGTH_SHORT).show();

                                        //ユーザー情報アクティビティに戻る
                                        Intent profile = new Intent(ActivitySettingUserProfile_Edit.this, ActivitySettingUserProfile.class);
                                        profile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(profile);

                                        Log.d(TAG, "SUCSESS: Updating User");
                                        Log.d(TAG, "---------------------------------");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "ERROR: Getting MetaData", e);
                                        Log.d(TAG, "---------------------------------");
                                    }
                                });

                                Log.d(TAG, "SCSESS adding UserImage to Database");
                                Log.d(TAG, "---------------------------------");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "ERROR: adding UserImage to Database", e);
                                Log.d(TAG, "---------------------------------");

                                //送信失敗時ダイアログ表示
                                new AlertDialog.Builder(context)
                                        .setTitle("エラー")
                                        .setMessage("ユーザー画像の更新に失敗しました。")
                                        .show();
                            }
                        });

                        progressDialog.dismiss();
                    } else {
                        //sharedPrefarences更新
                        editor.apply();

                        //ユーザー情報アクティビティに戻る
                        Intent profile = new Intent(ActivitySettingUserProfile_Edit.this, ActivitySettingUserProfile.class);
                        profile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(profile);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "ERROR: Updating User", e);
                    Log.w(TAG, "---------------------------------");

                    //送信失敗時ダイアログ表示
                    new AlertDialog.Builder(context)
                            .setTitle("エラー")
                            .setMessage("ユーザー情報の更新に失敗しました。")
                            .show();
                }
            });
        }

        progressDialog.dismiss();
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
            imageView_userImage.setImageURI(outputUri);

            //結果のUriをグローバル変数に入れる
            imageDirectryUri = outputUri;

            //画像変更の有無をtrueに
            imageChanged = true;
        }

        @Override public void onError(Throwable e) {

        }
    };
}
