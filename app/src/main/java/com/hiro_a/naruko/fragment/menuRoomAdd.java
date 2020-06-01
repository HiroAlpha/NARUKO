package com.hiro_a.naruko.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.PassEncodeTask;
import com.hiro_a.naruko.view.CustomButton;
import com.hiro_a.naruko.view.CustomImageView;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.WINDOW_SERVICE;

public class menuRoomAdd extends Fragment implements View.OnClickListener {
    private CircleImageView roomImagePreview;
    private ImageView roomImageChange;
    private TextView roomAddTitle;
    private EditText roomNameEdittext;
    private EditText passwordEditText;
    private CheckBox passwordCheckBox;

    private FragmentManager manager;
    private PopupWindow cropImagePopup;

    private StorageReference mStorageRefernce;

    private Boolean imageIs = false;
    private Boolean passwordIs = false;
    private Uri trialImageUri;

    private String TAG = "NARUKO_DEBUG";
    private int STRAGEACCESSFRAMEWORK_REQUEST_CODE = 42;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_room_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomAddTitle = (TextView) view.findViewById(R.id.menu_roomRegister_title);
        roomImagePreview = (CircleImageView) view.findViewById(R.id.menu_roomRegister_imageView);
        roomImageChange = (ImageView) view.findViewById(R.id.menu_roomRegister_changeImage);
        roomImageChange.setOnClickListener(this);

        roomNameEdittext = (EditText) view.findViewById(R.id.menu_roomRegister_roomName);
        passwordEditText = (EditText) view.findViewById(R.id.menu_roomRegister_password);
        passwordEditText.setEnabled(false);

        //チェックボックス
        passwordCheckBox = (CheckBox) view.findViewById(R.id.menu_roomRegister_passwordCheckBox);
        passwordCheckBox.setOnClickListener(this);

        //ボタン
        CustomButton roomAddButton = (CustomButton) view.findViewById(R.id.menu_roomRegister_button);
        roomAddButton.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#808080")));
        roomAddButton.setOnClickListener(this);

        manager = getFragmentManager();

        mStorageRefernce = FirebaseStorage.getInstance().getReference();
    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.menu_roomRegister_changeImage:
                //ストレージアクセスフレームワーク
                Intent strageAccessFramework = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                strageAccessFramework.addCategory(Intent.CATEGORY_OPENABLE);    //開くことのできるファイルに限定
                strageAccessFramework.setType("image/*");   //画像ファイルに限定
                startActivityForResult(strageAccessFramework, STRAGEACCESSFRAMEWORK_REQUEST_CODE);
                break;

            case R.id.menu_roomRegister_passwordCheckBox:
                if (passwordCheckBox.isChecked()) {
                    passwordIs = true;
                    passwordEditText.setEnabled(true);
                } else {
                    passwordIs = false;
                    passwordEditText.setEnabled(false);
                }
                break;

            case R.id.menu_roomRegister_button:
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
                final Uri imageUri = data.getData();    //ファイルのUriを取得

                //ウィンドウサイズ取得
                WindowManager wm = (WindowManager)getActivity().getSystemService(WINDOW_SERVICE);
                Display disp = wm.getDefaultDisplay();
                Point size = new Point();
                disp.getSize(size);

                int screenHeight = size.y;

                cropImagePopup = new PopupWindow(getActivity());
                View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_menu_room_add_popup, null);
                cropImagePopup.setContentView(view);
                cropImagePopup.setOutsideTouchable(true);
                cropImagePopup.setFocusable(true);

                cropImagePopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                cropImagePopup.setHeight((screenHeight / 3) * 2);

                cropImagePopup.showAsDropDown(roomAddTitle);

                final CropImageView cropImageView = (CropImageView) view.findViewById(R.id.menu_roomRegister_cropImage);
                cropImageView.load(imageUri).execute(mLoadCallback);

                //時計（右回り）
                CustomImageView cropRotateRight = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropRotateRight);
                cropRotateRight.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
                cropRotateRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                    }
                });

                //反時計（左回り）
                CustomImageView cropRotateLeft = (CustomImageView) view.findViewById(R.id.menu_roomRegister_cropRotateLeft);
                cropRotateLeft.setOnTouchListener(new ButtonColorChangeTask(Color.parseColor("#FFFFFF")));
                cropRotateLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
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
                                        .execute(makeNewUri(), mSaveCallback);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
                    }
                });
            }
        }
    }

    //部屋画像保存Uri
    public Uri makeNewUri(){
        String fileName = "TrialImage.jpg";

        File dataDir = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM, "NARUKO");
        if(!dataDir.exists()){
            dataDir.mkdirs();
            Log.d(TAG, "SUCSESS CREATING DCIM/NARUKO");
        }
        File filePath = new File(dataDir,fileName);
        trialImageUri = Uri.fromFile(filePath);

        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContext().getContentResolver();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        values.put(MediaStore.Images.Media.DATA, Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + "NARUKO/" +fileName);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return trialImageUri;
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
            String passwordString = new PassEncodeTask().encode(getString(R.string.ENC_KEY), passwordEditText.getText().toString(), "BLOWFISH");
            newRoom.put("password", passwordString);
        }

        roomRef.add(newRoom).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String docName = documentReference.getId().toString();
                StorageReference uploadImageRef = mStorageRefernce.child("Images/RoomImages/" + docName + ".jpg");
                UploadTask uploadTask = uploadImageRef.putFile(trialImageUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //ルームメニューへ
                        Fragment fragmentChat = new menuRoom();
                        FragmentTransaction transactionToChat = manager.beginTransaction();
                        transactionToChat.setCustomAnimations(
                                R.anim.fragment_slide_in_back, R.anim.fragment_slide_out_front);
                        transactionToChat.replace(R.id.menu_fragment, fragmentChat, "FRAG_MENU_ROOM");
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

            imageIs = true;
        }

        @Override public void onError(Throwable e) {

        }
    };
}
