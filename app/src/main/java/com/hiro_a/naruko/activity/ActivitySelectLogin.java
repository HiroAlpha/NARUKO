package com.hiro_a.naruko.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.fragment.Dialog;
import com.hiro_a.naruko.fragment.LoginSelect;
import com.hiro_a.naruko.task.PermissionCheck;
import com.hiro_a.naruko.task.UserImageStream;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivitySelectLogin extends AppCompatActivity {
    Context context;
    String TAG = "NARUKO_DEBUG @ ActivitySelectLogin";

    String userId;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFireStore;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();

        //Permission check
        PermissionCheck permissionCheck = new PermissionCheck();
        permissionCheck.getPermission(context, this);

        int fragmentCount = 1;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentSelectLogin = new LoginSelect();
        FragmentTransaction transactionToSelect = fragmentManager.beginTransaction();
        transactionToSelect.setCustomAnimations(
                R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_left,
                R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_rigt);
        transactionToSelect.replace(R.id.loginSelect_layout_fragmentContainter, fragmentSelectLogin, "FRAG_LOGIN_SELECT");
        if (fragmentCount != 1) {
            transactionToSelect.addToBackStack(null);
        }
        transactionToSelect.commit();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFireStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    //Email login flow
    public void loginWithEmail(String email, String password){
        //ProgressDialog
        progressDialog = new ProgressDialog(ActivitySelectLogin.this);
        progressDialog.setTitle("ログイン中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "SUCSESS: Login with Email");
                    Log.d(TAG, "---------------------------------");

                    changeToMenu(getString(R.string.PROVIDER_KEY_EMAIL));

                    progressDialog.dismiss();
                }else {
                    Log.w(TAG, "ERROR: Login with Email", task.getException());
                    Log.d(TAG, "---------------------------------");

                    progressDialog.dismiss();
                }
            }
        });
    }

    //Twitter Login Flow
    public void loginWithTwitter(){
        progressDialog = new ProgressDialog(ActivitySelectLogin.this);
        progressDialog.setTitle("ログイン中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        /*
        このログイン方式をとると一度ウェブブラウザを開き、アクティビティが保留となるので
        保留中のアクティビティがないかどうかチェックする必要がある。
         */

        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        //保留中のアクティビティがある場合
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "SUCSESS: Handling Twitter login");
                                    Log.d(TAG, "---------------------------------");
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "ERROR: Handling Twitter login", e);
                                    Log.w(TAG, "---------------------------------");

                                    progressDialog.dismiss();
                                }
                            });
        } else {
            //保留中の物がない場合

            firebaseAuth.startActivityForSignInWithProvider(ActivitySelectLogin.this, provider.build()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //ログイン完了
                    Log.d(TAG, "SUCSESS: Login with Twitter");
                    Log.d(TAG, "---------------------------------");
                    CheckDocument(authResult);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //ログイン失敗
                    Log.w(TAG, "ERROR: Login with Twitter", e);
                    Log.w(TAG, "---------------------------------");

                    progressDialog.dismiss();
                }
            });
        }
    }

    private void CheckDocument(final AuthResult authResult){
        //UserRefarence
        final CollectionReference userRef = firebaseFireStore.collection("users");

        //UserId
        userId = firebaseAuth.getCurrentUser().getUid();

        userRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()){
                        //Don't create new account
                        Log.d(TAG, "NOTICE: Account Exists, skipping user create...");
                        Log.d(TAG, "---------------------------------");

                        changeToMenu(getString(R.string.PROVIDER_KEY_TWITTER));
                    } else {
                        //Create Account
                        twitterAccountSetting(authResult, userRef);
                    }
                }
            }
        });
    }

    //Add User to Database（Twitter）
    private void twitterAccountSetting(AuthResult authResult, CollectionReference userRef) {

        //ユーザー追加時刻
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        final String time = SD.format(new Date()).toString();

        final String twitterUserId = authResult.getAdditionalUserInfo().getUsername();      //TwitterユーザーId
        final Map<String, Object> profile = authResult.getAdditionalUserInfo().getProfile();    //Twitterユーザー情報
        final String providerId = authResult.getAdditionalUserInfo().getProviderId();  //プロバイダーId（Twitter.com）

        final String twitterUserName = profile.get("name").toString();

        final String userImageUrlString = profile.get("profile_image_url").toString().replace("normal", "200x200");

        //Upload userImage
        final UserImageStream asyncTask = new UserImageStream(ActivitySelectLogin.this);
        asyncTask.execute(userImageUrlString);

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("datetime", time);
        newUser.put("userName", twitterUserName);
        newUser.put("userId", userId);
        newUser.put("userImageIs", true);

        userRef.document(userId).set(newUser, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {
                Log.d(TAG, "SUCSESS: Adding User to Database");

                changeToMenu(getString(R.string.PROVIDER_KEY_TWITTER));

                /*
                //Update userImage
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(userImageUrlString))
                        .build();
                 */

                /*
                Log.d(TAG, "*** Twitter_User_Info ***");
                Log.d(TAG, "Time: " + time);
                Log.d(TAG, "UserId: " + userId);
                Log.d(TAG, "TwitterUserId: " + twitterUserId);
                Log.d(TAG, "TwitterUserName: " + twitterUserName);
                Log.d(TAG, "TwitterUserImage: " + twitterUserImage);
                Log.d(TAG, "ProviderId:" + providerId);
                Log.d(TAG, "---------------------------------");
                 */

                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "datetime: " + time);
                Log.w(TAG, "UserId: " + userId);
                Log.w(TAG, "TwitterUserId: " + twitterUserId);
                Log.w(TAG, "TwitterUserName: " + twitterUserName);
                Log.w(TAG, "ERROR: Adding User to Database", e);
                Log.w(TAG, "---------------------------------");

                progressDialog.dismiss();
            }
        });

    }

    public void UploadUserImage(InputStream stream) {
        try {
            StorageReference uploadImageRef = storageReference.child("Images/UserImages/" + userId + ".jpg");
            UploadTask uploadTask = uploadImageRef.putStream(stream);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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

        //メニュー画面へ
        Intent menu = new Intent(ActivitySelectLogin.this, ActivityMenu.class);
        menu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(menu);
    }

    /*
    //wifiアドレス取得
    private static String getWifiIPAddress(Context context) {
        WifiManager manager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        int ipAddr = info.getIpAddress();
        String wifiIp = String.format("%02d.%02d.%02d.%02d", (ipAddr>>0)&0xff, (ipAddr>>8)&0xff, (ipAddr>>16)&0xff, (ipAddr>>24)&0xff);

        if (wifiIp.equals("00.00.00.00")){
            return "none";
        } else {
            return wifiIp;
        }
    }

    //ipv4アドレス取得
    public String getLocalIpv4Address(){
        try{
            for (Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces(); networkInterfaceEnum.hasMoreElements();){
                NetworkInterface networkInterface = networkInterfaceEnum.nextElement();
                for (Enumeration<InetAddress> ipAddressEnum = networkInterface.getInetAddresses(); ipAddressEnum.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) ipAddressEnum.nextElement();

                    //ipv4かどうか
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex){
            Log.w(TAG, "Error getting ipv4 address", ex);
        }

        return "none";
    }
    */
}
