package com.hiro_a.naruko.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.task.DownloadImageTask;
import com.hiro_a.naruko.view.RecyclerView.ProfileView.LinearLayoutAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettingUserProfile extends AppCompatActivity implements View.OnClickListener {
    String TAG = "NARUKO_DEBUG @ ActivitySettingUserProfile";

    private String userId;
    private String userEmail;
    private Uri userImage;
    boolean emailVerified;

    int STRAGEACCESSFRAMEWORK_REQUEST_CODE = 42;

    TextView title_UserImage;
    CircleImageView image_UserImage;
    ImageView image_ImageChanger;

    FirebaseUser user;

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference userRef;

    private StorageReference storageRefarence;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        //Back_Button active
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ユーザーId取得
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        userEmail = user.getEmail();
        userImage = user.getPhotoUrl();
        emailVerified = user.isEmailVerified();

        firebaseFirestore = FirebaseFirestore.getInstance();
        userRef = firebaseFirestore.collection("users");

        storageRefarence = FirebaseStorage.getInstance().getReference();

        title_UserImage = (TextView)findViewById(R.id.title_userProfile);
        title_UserImage.setOnClickListener(this);

        image_UserImage = (CircleImageView) findViewById(R.id.setting_userImageview);
        if (userImage==null){
            image_UserImage.setImageResource(R.drawable.ic_person_black_24dp);
        }else {
            if (userImage.toString().contains("pbs.twimg.com")){
                userImage = Uri.parse(userImage.toString().replace("_normal", ""));
            }
            new DownloadImageTask((ImageView)findViewById(R.id.setting_userImageview)).execute(userImage.toString());
        }

        image_ImageChanger = (ImageView)findViewById(R.id.setting_changeImage);
        image_ImageChanger.setOnClickListener(this);

        getUserData();
        settingRecyclerView();
    }

    //戻るボタン
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_userProfile:
                changeUserName();
                break;

            case R.id.setting_changeImage:
                //ストレージアクセスフレームワーク
                Intent strageAccessFramework = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                strageAccessFramework.addCategory(Intent.CATEGORY_OPENABLE);    //開くことのできるファイルに限定
                strageAccessFramework.setType("image/*");   //画像ファイルに限定
                startActivityForResult(strageAccessFramework, STRAGEACCESSFRAMEWORK_REQUEST_CODE);
                break;
        }
    }

    private void changeUserName(){

    }

    private void changeUserImage(final Uri imageUri){
        final String imageUriAtServer = "images/" + imageUri.getLastPathSegment();
        final StorageReference imageRef = storageRefarence.child(imageUriAtServer);
        final UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "SUCSESS Image uploaded to FirebaseStorage");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error uploading image to FirebaseStorge", e);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                //画像Url取得
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw  task.getException();
                        }

                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uploadedImageUri = task.getResult();

                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uploadedImageUri)
                                    .build();

                            Uri newImageUrl = user.getPhotoUrl();
                            if (newImageUrl.toString().contains("pbs.twimg.com")){
                                newImageUrl = Uri.parse(newImageUrl.toString().replace("_normal", ""));
                            }
                            new DownloadImageTask((ImageView)findViewById(R.id.setting_userImageview))
                                    .execute(newImageUrl.toString());

                            Log.d(TAG, "New Url: " + uploadedImageUri.toString());
                            Log.d(TAG, "New Url Check: " + newImageUrl.toString());
                            Log.d(TAG, "---------------------------------");
                        } else {
                            Log.w(TAG, "Error Getting Url from FirebaseStorage");
                            Log.w(TAG, "---------------------------------");
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STRAGEACCESSFRAMEWORK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();    //ファイルのUriを取得
                changeUserImage(imageUri);
            }
        }
    }

    //ユーザーデータ取得
    public void getUserData(){
        userRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()){
                        String userName = document.getString("userName");
                        title_UserImage.setText(userName);
                    }else {
                        Log.w(TAG, "No such document");
                    }
                }else {
                    Log.w(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //RecyclerView生成
    public void settingRecyclerView(){
        //リスト
        List<String> titleData = new ArrayList<>(Arrays.asList(
                "ユーザーID", "メールアドレス", "メールアドレス認証"
        ));

        //リスト
        List<String> userData = new ArrayList<>();
        userData.add(userId);   //ユーザーID
        //メールアドレス
        if (userEmail == null){
            userData.add("メールアドレスが登録されていません");
        } else {
            userData.add(userEmail);
        }
        //メールアドレス認証
        if (emailVerified){
            userData.add("認証済み");
        } else {
            userData.add("認証されていません");
        }

        //RecyclerView
        final RecyclerView settingProfileRecyclerView = (RecyclerView)findViewById(R.id.settingProfileRecyclerView);

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

        RecyclerView.ItemDecoration devideLine = new DividerItemDecoration(ActivitySettingUserProfile.this, DividerItemDecoration.VERTICAL);
        settingProfileRecyclerView.addItemDecoration(devideLine);

        settingProfileRecyclerView.setHasFixedSize(true);
    }
}
