package com.hiro_a.naruko.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.item.SelectorItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivitySelectLogin extends AppCompatActivity implements View.OnClickListener {
    SelectorItem twitterSelector, emailSelector;

    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseDatabase;

    int SIGN_IN = 9999;
    String TAG = "NARUKO_DEBUG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlogin);

        String makeAccountMessage = "まだアカウントをお持ちでない方はこちら。";
        SpannableString spannableString = createSpannableString(makeAccountMessage);

        TextView textView = (TextView)findViewById(R.id.makeAccout);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        twitterSelector = (SelectorItem)findViewById(R.id.loginSelector_Twitter);
        twitterSelector.setOnClickListener(this);

        emailSelector = (SelectorItem)findViewById(R.id.loginSelector_Email);
        emailSelector.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginSelector_Twitter:
                loginToTwitter();
                break;

            case R.id.loginSelector_Email:
                break;
        }
    }

    //リンク文字列生成
    private SpannableString createSpannableString(String text){
        SpannableString spannableString = new SpannableString(text);

        //リンク化対象のstart, end計算
        int start = 0;
        int end = 0;
        Pattern pattern = Pattern.compile("こちら");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()){
            start = matcher.start();
            end = matcher.end();
            break;
        }

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                //登録フォームへ
                Intent makeAccount = new Intent(ActivitySelectLogin.this, ActivityRegister.class);
                startActivity(makeAccount);
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    private void loginToTwitter(){
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        /*
        このログイン方式をとると一度ウェブブラウザを開き、アクティビティが保留となるので
        保留中のアクティビティがないかどうかチェックする必要がある。
         */

        Task<AuthResult> pendingResultTask = mFirebaseAuth.getPendingAuthResult();
        //保留中のアクティビティがある場合
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "SUCSESS handling Twitter login");
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error handling Twitter login");
                                    Log.w(TAG, e);
                                }
                            });
        }
        //保留中の物がない場合
        else {
            mFirebaseAuth.startActivityForSignInWithProvider(this, provider.build()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //ログイン完了
                    Log.d(TAG, "SUCSESS login with Twitter");
                    twitterAccountSetting(authResult);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //ログイン失敗
                    Log.w(TAG, "Error login with Twitter");
                    Log.w(TAG, e);
                }
            });
        }
    }

    //ユーザーをデータベースに追加
    private void twitterAccountSetting(AuthResult authResult) {
        CollectionReference userRef = mFirebaseDatabase.collection("users");

        //ユーザーId
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userId = mFirebaseUser.getUid();

        //ユーザー追加時刻
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        final String time = SD.format(new Date()).toString();

        final String twitterUserId = authResult.getAdditionalUserInfo().getUsername();      //TwitterユーザーId
        final Map<String, Object> profile = authResult.getAdditionalUserInfo().getProfile();    //Twitterユーザー情報
        final String providerId = authResult.getAdditionalUserInfo().getProviderId();  //プロバイダーId（Twitter.com）

        final String twitterUserName = profile.get("name").toString();
        final String twitterUserImage = profile.get("profile_image_url").toString().replace("_normal", "");

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("datetime", time);
        newUser.put("userName", twitterUserName);

        userRef.document(userId).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {
                Log.d(TAG, "SUCSESS adding User");

                Log.d(TAG, "Time:" + time);
                Log.d(TAG, "UserId:" + userId);
                Log.d(TAG, "TwitterUserId:" + twitterUserId);
                Log.d(TAG, "TwitterUserName:" + twitterUserName);
                Log.d(TAG, "TwitterUserImage:" + twitterUserImage);
                Log.d(TAG, "ProviderId:" + providerId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding User");
                Log.w(TAG, e);
            }
        });
    }
}
