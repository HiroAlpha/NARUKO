package com.hiro_a.naruko.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.fragment.loginSelect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivitySelectLogin extends AppCompatActivity {
    Context context;
    String TAG = "NARUKO_DEBUG @ ActivitySelectLogin";

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFireStore;

    DeviceInfo userInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlogin);
        context = getApplicationContext();

        int fragmentCount = 1;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentSelectLogin = new loginSelect();
        FragmentTransaction transactionToSelect = fragmentManager.beginTransaction();
        transactionToSelect.setCustomAnimations(
                R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_left,
                R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_rigt);
        transactionToSelect.replace(R.id.login_fragment, fragmentSelectLogin, "FRAG_LOGIN_SELECT");
        if (fragmentCount != 1) {
            transactionToSelect.addToBackStack(null);
        }
        transactionToSelect.commit();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFireStore = FirebaseFirestore.getInstance();

        userInfo = new DeviceInfo();
    }

    //Emailログインフロー
    public void loginWithEmail(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "SUCSESS: Login with Email");
                    Log.d(TAG, "---------------------------------");

                    userInfo.setDeviceInfo(context);

                    //to Menu
                    Intent menu = new Intent(ActivitySelectLogin.this, ActivityMenu.class);
                    menu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(menu);
                }else {
                    Log.w(TAG, "ERROR: Login with Email", task.getException());
                    Log.d(TAG, "---------------------------------");
                }
            }
        });
    }

    //Twitter Login Flow
    public void loginWithTwitter(){
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

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
                                }
                            });
        }
        //保留中の物がない場合
        else {
            firebaseAuth.startActivityForSignInWithProvider(ActivitySelectLogin.this, provider.build()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //ログイン完了
                    Log.d(TAG, "SUCSESS: Login with Twitter");
                    Log.d(TAG, "---------------------------------");
                    twitterAccountSetting(authResult);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //ログイン失敗
                    Log.w(TAG, "ERROR: Login with Twitter", e);
                    Log.w(TAG, "---------------------------------");
                }
            });
        }
    }

    //Add User to Database（Twitter）
    private void twitterAccountSetting(AuthResult authResult) {
        CollectionReference userRef = firebaseFireStore.collection("users");

        //UserId
        final String userId = firebaseAuth.getCurrentUser().getUid();

        //ユーザー追加時刻
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        final String time = SD.format(new Date()).toString();

        final String twitterUserId = authResult.getAdditionalUserInfo().getUsername();      //TwitterユーザーId
        final Map<String, Object> profile = authResult.getAdditionalUserInfo().getProfile();    //Twitterユーザー情報
        final String providerId = authResult.getAdditionalUserInfo().getProviderId();  //プロバイダーId（Twitter.com）

        final String twitterUserName = profile.get("name").toString();
        final String twitterUserImage = profile.get("profile_image_url").toString();
        Log.d(TAG, "TwitterUserImageCheck: " + twitterUserImage);

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("datetime", time);
        newUser.put("userId", userId);
        newUser.put("userName", twitterUserName);

        userRef.document(userId).set(newUser, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {
                Log.d(TAG, "SUCSESS: Adding User to Database");

                Log.d(TAG, "TwitterUserImageCheck: " + twitterUserImage);
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(twitterUserImage))
                        .build();

                Log.d(TAG, "*** Twitter_User_Info ***");
                Log.d(TAG, "Time: " + time);
                Log.d(TAG, "UserId: " + userId);
                Log.d(TAG, "TwitterUserId: " + twitterUserId);
                Log.d(TAG, "TwitterUserName: " + twitterUserName);
                Log.d(TAG, "TwitterUserImage: " + twitterUserImage);
                Log.d(TAG, "ProviderId:" + providerId);
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ERROR: Adding User to Database", e);
                Log.w(TAG, "---------------------------------");
            }
        });

        //ユーザー情報をSharedPreに
        userInfo.setDeviceInfo(context);

        //メニュー画面へ
        Intent menu = new Intent(ActivitySelectLogin.this, ActivityMenu.class);
        menu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(menu);
    }

    //表示フラグメント確認
    public String checkFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();

        //フラグメント
        Fragment fragment_loginSelect = fragmentManager.findFragmentByTag("FRAG_LOGIN_SELECT");
        Fragment fragment_emailLogin = fragmentManager.findFragmentByTag("FRAG_LOGIN_EMAIL");

        if (fragment_loginSelect != null && fragment_loginSelect.isVisible()){
            return "FRAG_LOGIN_SELECT";
        }

        if (fragment_loginSelect != null && fragment_loginSelect.isVisible()){
            return "FRAG_LOGIN_EMAIL";
        }

        return "FRAG_NOT_VISIBLE";
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
