package com.hiro_a.naruko.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.fragment.Dialog;
import com.hiro_a.naruko.fragment.LoginSelect;
import com.hiro_a.naruko.task.PermissionCheck;
import com.hiro_a.naruko.task.UserImageStream;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ActivitySelectLogin extends AppCompatActivity {
    private String TAG = "NARUKO_DEBUG @ ActivitySelectLogin";
    private Context context;

    private String userId;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFireStore;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //コンテキスト
        context = getApplicationContext();

        //アプリの権限を確認
        PermissionCheck permissionCheck = new PermissionCheck();
        permissionCheck.getPermission(context, this);

        //フラグメントマネージャー
        FragmentManager fragmentManager = getSupportFragmentManager();

        //ログインセレクトフラグメントを表示
        Fragment fragmentloginSelect = new LoginSelect();
        FragmentTransaction transactionToSelect = fragmentManager.beginTransaction();
        transactionToSelect.setCustomAnimations(
                R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_left,
                R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_rigt);
        transactionToSelect.replace(R.id.loginSelect_layout_fragmentContainter, fragmentloginSelect, "FRAG_LOGIN_SELECT");
        transactionToSelect.commit();

        //*** Firebase ***
        //FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        //Firestore
        firebaseFireStore = FirebaseFirestore.getInstance();
        //ストレージレファレンス
        storageReference = FirebaseStorage.getInstance().getReference();

        //アップデート情報取得
        getInfomation();
    }

    //アップデート情報取得
    private void getInfomation(){
        //お知らせ情報
        DocumentReference updateRef = firebaseFireStore.collection("infomations").document("update");
        updateRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()){
                        String updateInfo = document.getString("Infomation");

                        Log.d(TAG, "UpdateInfo: " + updateInfo);
                        Log.d(TAG, "---------------------------------");

                        //アップデート情報テキスト
                        TextView textView_updateInfo = findViewById(R.id.loginSelect_textView_infomation);
                        textView_updateInfo.setText(updateInfo);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ERROR: getting update info", e);
                Log.d(TAG, "---------------------------------");
            }
        });
    }

    //メールアドレス・パスワードログイン
    public void loginWithEmail(String email, String password){
        //プログレスダイアログ表示
        progressDialog = new ProgressDialog(ActivitySelectLogin.this);
        progressDialog.setTitle("ログイン中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //ログイン
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "SUCSESS: Login with Email");
                    Log.d(TAG, "---------------------------------");

                    //メニュー画面アクティビティに移行
                    changeToMenu(getString(R.string.PROVIDER_KEY_EMAIL));
                }else {
                    Log.w(TAG, "ERROR: Login with Email", task.getException());
                    Log.d(TAG, "---------------------------------");
                }

                //プログレスダイアログ非表示
                progressDialog.dismiss();
            }
        });
    }

    //Twitterログイン
    public void loginWithTwitter(){
        //プログレスダイアログ表示
        progressDialog = new ProgressDialog(ActivitySelectLogin.this);
        progressDialog.setTitle("ログイン中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //プロバイダー
        final OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        /*
        このログイン方式をとると一度ウェブブラウザを開き、アクティビティが保留となるので
        保留中のアクティビティがないかどうかチェックする必要がある。
         */

        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();

        if (pendingResultTask != null) {    //保留中のアクティビティがある場合
            pendingResultTask.addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "SUCSESS: Handling Twitter login");
                                    Log.d(TAG, "---------------------------------");

                                    //プログレスダイアログ非表示
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "ERROR: Handling Twitter login", e);
                                    Log.w(TAG, "---------------------------------");

                                    //プログレスダイアログ非表示
                                    progressDialog.dismiss();
                                }
                            });
        } else {    //保留中のアクティビティがない場合
            //ログイン
            firebaseAuth.startActivityForSignInWithProvider(ActivitySelectLogin.this, provider.build()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //ログイン完了
                    Log.d(TAG, "SUCSESS: Login with Twitter");
                    Log.d(TAG, "---------------------------------");

                    //ユーザードキュメント確認
                    CheckDocument(authResult);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //ログイン失敗
                    Log.w(TAG, "ERROR: Login with Twitter", e);
                    Log.w(TAG, "---------------------------------");

                    //プログレスダイアログ非表示
                    progressDialog.dismiss();
                }
            });
        }
    }

    //ユーザードキュメント確認
    private void CheckDocument(final AuthResult authResult){
        //ユーザーフォルダパス
        final CollectionReference userRef = firebaseFireStore.collection("users");

        //ユーザーID取得
        userId = firebaseAuth.getCurrentUser().getUid();

        //ユーザーIDが既に存在するか確認
        userRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()){ //存在する場合
                        Log.d(TAG, "NOTICE: Account Exists, skipping user create...");
                        Log.d(TAG, "---------------------------------");

                        //アカウントを新規に作らずスキップ
                        changeToMenu(getString(R.string.PROVIDER_KEY_TWITTER));
                    } else {    //存在しない場合
                        //アカウント作成
                        twitterAccountSetting(authResult, userRef);
                    }
                }
            }
        });
    }

    //ユーザー情報をFirestoreに送信
    private void twitterAccountSetting(AuthResult authResult, CollectionReference userRef) {

        //ユーザー作成日時
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        final String userCreated = SD.format(new Date());

        //TwitterID
        final String twitterUserId = authResult.getAdditionalUserInfo().getUsername();

        //Twitterプロファイル
        final Map<String, Object> profile = authResult.getAdditionalUserInfo().getProfile();

        //Twitterユーザー名
        final String twitterUserName = profile.get("name").toString();

        //Twitterユーザー画像URL
        final String userImageUrlString = profile.get("profile_image_url").toString().replace("normal", "200x200");

        //Twitterユーザー画像をFirestorageに送信
        final UserImageStream asyncTask = new UserImageStream(ActivitySelectLogin.this);
        asyncTask.execute(userImageUrlString);

        //ユーザーカラー
        String[] color = {
                "Yuuna",
                "Tougou",
                "Huu",
                "Itsuki",
                "Karin"
        };
        Random random = new Random();
        String userColor = color[random.nextInt(5)];

        //ユーザー情報
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("UserCreated", userCreated);
        newUser.put("UserName", twitterUserName);
        newUser.put("UserId", userId);
        newUser.put("UserImageIs", true);
        newUser.put("UserColor", userColor);

        userRef.document(userId).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {
                //送信成功
                Log.d(TAG, "SUCSESS: Adding User to Database");

                //メニュー画面へ
                changeToMenu(getString(R.string.PROVIDER_KEY_TWITTER));

                //プログレスダイアログ非表示
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //送信失敗
                Log.w(TAG, "UserCreated: " + userCreated);
                Log.w(TAG, "UserId: " + userId);
                Log.w(TAG, "TwitterUserId: " + twitterUserId);
                Log.w(TAG, "TwitterUserName: " + twitterUserName);
                Log.w(TAG, "ERROR: Adding User to Database", e);
                Log.w(TAG, "---------------------------------");

                //プログレスダイアログ非表示
                progressDialog.dismiss();
            }
        });

    }

    //ユーザー画像アップロード
    public void UploadUserImage(InputStream stream) {
        try {
            //Firebaseでのユーザー画像パス
            final StorageReference uploadImageRef = storageReference.child("Images/UserImages/" + userId + ".jpg");

            //アップロード
            UploadTask uploadTask = uploadImageRef.putStream(stream);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            //画像更新日時取得
                            long createdDatetime = storageMetadata.getCreationTimeMillis();

                            //SharedPreferences
                            SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = userData.edit();

                            //画像送信日時をsharedPrefarencesへ
                            editor.putLong("UserImageUpdateTime", createdDatetime);
                            editor.apply();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "ERROR: Getting MetaData", e);
                            Log.w(TAG, "---------------------------------");
                        }
                    });

                    Log.d(TAG, "SUCSESS: Adding UserImage to Database");
                    Log.d(TAG, "---------------------------------");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "ERROR: Adding UserImage to Database", e);
                    Log.w(TAG, "---------------------------------");
                }
            });

        } catch (Exception e){
            Log.w(TAG, "ERROR: Adding UserImage to Database", e);
            Log.w(TAG, "---------------------------------");
        }
    }

    private void changeToMenu(String providerKey){
        //ユーザー情報をSharedPreに
        new DeviceInfo().setDeviceInfo(context, providerKey);

        //メニュー画面アクティビティに移行
        Intent menu = new Intent(ActivitySelectLogin.this, ActivityMenu.class);
        menu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(menu);
    }
}
