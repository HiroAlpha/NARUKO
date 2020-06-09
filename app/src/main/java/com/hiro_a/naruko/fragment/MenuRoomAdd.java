package com.hiro_a.naruko.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.Hash;
import com.hiro_a.naruko.task.MakeStoargeUri;
import com.hiro_a.naruko.view.CustomButton;
import com.hiro_a.naruko.view.CustomImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuRoomAdd extends Fragment implements View.OnClickListener {
    private String TAG = "NARUKO_DEBUG @ MenuRoomCreate";
    private Context context;

    private Boolean imageIs = false;
    private Boolean passwordIs = false;
    private Uri imageDirectryUri;

    private LinearLayout linearLayout;
    private CircleImageView roomImagePreview;
    private ImageView roomImageChange;
    private TextView roomAddTitle;
    private EditText roomNameEdittext;
    private EditText passwordEditText;
    private CheckBox passwordCheckBox;

    private FragmentManager manager;
    private PopupWindow cropImagePopup;

    private StorageReference mStorageRefernce;

    private int STRAGEACCESSFRAMEWORK_REQUEST_CODE = 42;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_room_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //コンテキスト
        context = getActivity();

        //メインビュー


        roomAddTitle = (TextView) view.findViewById(R.id.fRoomCreate_textView_title);
        roomImagePreview = (CircleImageView) view.findViewById(R.id.fRoomCreate_imageView_userIcon);
        roomImageChange = (ImageView) view.findViewById(R.id.fRoomCreate_imageView_changeImage);
        roomImageChange.setOnClickListener(this);

        roomNameEdittext = (EditText) view.findViewById(R.id.fRoomCreate_editText_roomname);
        passwordEditText = (EditText) view.findViewById(R.id.fRoomCreate_editText_password);
        passwordEditText.setEnabled(false);

        //チェックボックス
        passwordCheckBox = (CheckBox) view.findViewById(R.id.fRoomCreate_check_password);
        passwordCheckBox.setOnClickListener(this);

        //ボタン
        CustomButton roomAddButton = (CustomButton) view.findViewById(R.id.fRoomCreate_view_roomCreate);
        roomAddButton.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#808080")));
        roomAddButton.setOnClickListener(this);

        //CropImageView表示場所
        linearLayout = view.findViewById(R.id.fRoomCreate_layout_main);

        manager = getFragmentManager();

        mStorageRefernce = FirebaseStorage.getInstance().getReference();
    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.fRoomCreate_imageView_changeImage:
                //ストレージアクセスフレームワーク
                Intent strageAccessFramework = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                strageAccessFramework.addCategory(Intent.CATEGORY_OPENABLE);    //開くことのできるファイルに限定
                strageAccessFramework.setType("image/*");   //画像ファイルに限定
                startActivityForResult(strageAccessFramework, STRAGEACCESSFRAMEWORK_REQUEST_CODE);
                break;

            case R.id.fRoomCreate_check_password:
                if (passwordCheckBox.isChecked()) {
                    passwordIs = true;
                    passwordEditText.setEnabled(true);
                } else {
                    passwordIs = false;
                    passwordEditText.setEnabled(false);
                }
                break;

            case R.id.fRoomCreate_view_roomCreate:
                if (formChecker()) {
                    ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("チャットルーム作成中...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();

                    createRoom(progressDialog);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STRAGEACCESSFRAMEWORK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();    //ファイルのUriを取得

                //DeviceInfoから画面情報を取得
                DeviceInfo userInfo = new DeviceInfo();
                float screenHeight = userInfo.getScreenHeight(context);

                //画像切り取り画面表示・画像保存先Uri取得
                createCropView(linearLayout, imageUri, (int) screenHeight);
            }
        }
    }

    //画像Uri保存
    public void saveImageDirectroy(Uri imageDirectory){
        this.imageDirectryUri = imageDirectory;
    }

    //入力チェック
    private boolean formChecker(){
        boolean check = true;

        String name = roomNameEdittext.getText().toString();
        if (TextUtils.isEmpty(name)){
            roomNameEdittext.setError("部屋名が入力されていません");
            check = false;
        }

        if (passwordCheckBox.isChecked()){
            String password = passwordEditText.getText().toString();
            if (TextUtils.isEmpty(password)){
                passwordEditText.setError("パスワードが入力されていません");
                check = false;
            }
        }

        return check;
    }

    //部屋作成
    public void createRoom(final ProgressDialog progressDialog){
        if (imageDirectryUri!=null){
            imageIs = true;
        }

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirebaseDatabase = FirebaseFirestore.getInstance();
        CollectionReference roomRef = mFirebaseDatabase.collection("rooms");

        //日付
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String time = SD.format(new Date()).toString();

        //ユーザーID, 部屋名
        final String userId = mFirebaseAuth.getUid();
        final String roomName = roomNameEdittext.getText().toString();

        Map<String, Object> newRoom = new HashMap<>();
        newRoom.put("datetime", time);
        newRoom.put("creatorId", userId);
        newRoom.put("roomName", roomName);
        newRoom.put("passwordIs", passwordIs);
        newRoom.put("imageIs", imageIs);

        if (passwordIs){
            String hashed_password = new Hash().doHash(passwordEditText.getText().toString());
            newRoom.put("password", hashed_password);
        }

        roomRef.add(newRoom).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String docName = documentReference.getId().toString();
                StorageReference uploadImageRef = mStorageRefernce.child("Images/RoomImages/" + docName + ".jpg");
                UploadTask uploadTask = uploadImageRef.putFile(imageDirectryUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //ルームメニューへ
                        Fragment fragmentChat = new MenuRoom();
                        FragmentTransaction transactionToChat = manager.beginTransaction();
                        transactionToChat.setCustomAnimations(
                                R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                        transactionToChat.replace(R.id.menu_layout_fragmentContainer, fragmentChat, "FRAG_MENU_ROOM");
                        transactionToChat.commit();

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.w(TAG, "Error adding RoomImage to Database", e);
                        Log.w(TAG, "---------------------------------");
                    }
                });

                Log.d(TAG, "SCSESS adding Room to Database");
                Log.d(TAG, "RoomName: " + roomName);
                Log.d(TAG, "RoomID: " + docName);
                Log.d(TAG, "CreatorID: " + userId);
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

                Log.w(TAG, "Error adding Room to Database", e);
                Log.w(TAG, "---------------------------------");
            }
        });
    }

    //画像切り取りビュー表示
    public void createCropView(View parentView, final Uri imageUri, int screenHeight) {
        final Uri imageDirectoryUri = new MakeStoargeUri().makeNewUri(context);

        //CropImageViewウィンドウ
        cropImagePopup = new PopupWindow(context);
        View view = this.getLayoutInflater().inflate(R.layout.fragment_menu_room_create_popup, null);
        cropImagePopup.setContentView(view);
        cropImagePopup.setOutsideTouchable(true);
        cropImagePopup.setFocusable(true);

        //横幅
        cropImagePopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        //高さ
        cropImagePopup.setHeight((screenHeight / 3) * 2);

        //位置
        cropImagePopup.showAtLocation(parentView, Gravity.CENTER, 0, (screenHeight / 2) + (screenHeight / 3));

        //CropImageView本体
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

        CustomImageView cropButton = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropFinish);
        cropButton.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.crop(imageUri).outputHeight(300).outputMaxWidth(300).execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {
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
            String path = outputUri.getPath();
            Log.d(TAG, path);

            cropImagePopup.dismiss();
            roomImagePreview.setImageURI(outputUri);

            imageDirectryUri = outputUri;
        }

        @Override public void onError(Throwable e) {

        }
    };
}
