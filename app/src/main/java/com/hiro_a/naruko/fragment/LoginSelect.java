package com.hiro_a.naruko.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityRegister;
import com.hiro_a.naruko.activity.ActivitySelectLogin;
import com.hiro_a.naruko.item.SelectorItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginSelect extends Fragment implements View.OnClickListener {
    String TAG = "NARUKO_DEBUG @ loginSelect.fragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String makeAccountMessage = "まだアカウントをお持ちでない方はこちら。";
        SpannableString spannableAccount = createSpannableString(makeAccountMessage, "こちら");

        String securityMessage = "利用規約";
        SpannableString spannableSecurity = createSpannableString(securityMessage, "利用規約");

        TextView makeAccountText = (TextView)view.findViewById(R.id.fLoginSelect_textView_createAccount);
        makeAccountText.setText(spannableAccount);
        makeAccountText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView securityText = (TextView)view.findViewById(R.id.fLoginSelect_textView_policy);
        securityText.setText(spannableSecurity);
        securityText.setMovementMethod(LinkMovementMethod.getInstance());

        SelectorItem twitterSelector = (SelectorItem) view.findViewById(R.id.fLoginSelect_view_twitter);
        twitterSelector.setOnClickListener(this);

        SelectorItem emailSelector = (SelectorItem) view.findViewById(R.id.fLoginSelect_view_email);
        emailSelector.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fLoginSelect_view_twitter:
                //Twitterログイン
                ActivitySelectLogin activitySelectLogin = (ActivitySelectLogin)getActivity();
                activitySelectLogin.loginWithTwitter();
                break;

            case R.id.fLoginSelect_view_email:
                //Emailログイン画面へ
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragmentEmail = new LoginEmail();

                final FragmentTransaction transactionToEmail = fragmentManager.beginTransaction();
                transactionToEmail.setCustomAnimations(
                        R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_left,
                        R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_rigt);
                transactionToEmail.replace(R.id.loginSelect_layout_fragmentContainter, fragmentEmail, "FRAG_LOGIN_EMAIL");
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
                    Intent makeAccount = new Intent((ActivitySelectLogin)getActivity(), ActivityRegister.class);
                    startActivity(makeAccount);
                }

                if (keyword.equals("利用規約")){
                    //利用規約へ
                }

            }
        }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }
}
