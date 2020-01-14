package com.hiro_a.naruko.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityLogin;
import com.hiro_a.naruko.activity.ActivityMenu;
import com.hiro_a.naruko.activity.ActivityRegister;
import com.hiro_a.naruko.activity.ActivitySelectLogin;
import com.hiro_a.naruko.item.SelectorItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginSelect extends Fragment implements View.OnClickListener {
    SelectorItem twitterSelector, emailSelector;

    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseDatabase;

    int SIGN_IN = 9999;
    String TAG = "NARUKO_DEBUG";
    Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();

        String makeAccountMessage = "まだアカウントをお持ちでない方はこちら。";
        SpannableString spannableAccount = createSpannableString(makeAccountMessage, "こちら");

        String securityMessage = "利用規約";
        SpannableString spannableSecurity = createSpannableString(securityMessage, "利用規約");

        TextView makeAccountText = (TextView)view.findViewById(R.id.makeAccout);
        makeAccountText.setText(spannableAccount);
        makeAccountText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView securityText = (TextView)view.findViewById(R.id.security);
        securityText.setText(spannableSecurity);
        securityText.setMovementMethod(LinkMovementMethod.getInstance());

        twitterSelector = (SelectorItem)view.findViewById(R.id.loginSelector_Twitter);
        twitterSelector.setOnClickListener(this);

        emailSelector = (SelectorItem)view.findViewById(R.id.loginSelector_Email);
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
                //Emailログイン画面へ
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragmentEmail = new loginEmail();
                FragmentTransaction transactionToEmail = fragmentManager.beginTransaction();
                transactionToEmail.replace(R.id.login_fragment, fragmentEmail);
                transactionToEmail.addToBackStack(null);
                transactionToEmail.commit();
                break;
        }
    }

    //リンク文字列生成
    private SpannableString createSpannableString(String text, final String keyword){
        SpannableString spannableString = new SpannableString(text);

        //リンク化対象のstart, end計算
        int start = 0;
        int end = 0;
        Pattern pattern = Pattern.compile(keyword);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()){
            start = matcher.start();
            end = matcher.end();
            break;
        }

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (keyword.equals("こちら")){
                    //登録フォームへ
                    Intent makeAccount = new Intent(context, ActivityRegister.class);
                    startActivity(makeAccount);
                }

                if (keyword.equals("利用規約")){
                    //利用規約へ
                }

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
                                    Log.d(TAG, "---------------------------------");
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error handling Twitter login", e);
                                    Log.w(TAG, "---------------------------------");
                                }
                            });
        }
        //保留中の物がない場合
        else {
            mFirebaseAuth.startActivityForSignInWithProvider(getActivity(), provider.build()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //ログイン完了
                    Log.d(TAG, "SUCSESS login with Twitter");
                    Log.d(TAG, "---------------------------------");
                    twitterAccountSetting(authResult);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //ログイン失敗
                    Log.w(TAG, "Error login with Twitter", e);
                    Log.w(TAG, "---------------------------------");
                }
            });
        }
    }

    //ユーザーをデータベースに追加
    private void twitterAccountSetting(AuthResult authResult) {
        CollectionReference userRef = mFirebaseDatabase.collection("users");

        //ユーザーId
        final String userId = mFirebaseAuth.getCurrentUser().getUid();

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
        newUser.put("userId", userId);
        newUser.put("userName", twitterUserName);

        userRef.document(userId).set(newUser, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {
                Log.d(TAG, "SUCSESS adding User to Database");

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
                Log.w(TAG, "Error adding User to Database", e);
                Log.w(TAG, "---------------------------------");
            }
        });

        //メニュー画面へ
        Intent menu = new Intent(context, ActivityMenu.class);
        startActivity(menu);
    }
}
