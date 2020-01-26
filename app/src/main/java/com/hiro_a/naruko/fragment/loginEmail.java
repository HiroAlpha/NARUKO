package com.hiro_a.naruko.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivitySelectLogin;
import com.hiro_a.naruko.view.CustomButtonLogin;

public class loginEmail extends Fragment implements View.OnClickListener {
    private EditText mEmailField, mPasswordField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //メールアドレス
        mEmailField = (EditText)view.findViewById(R.id.login_email_edittext);
        mEmailField.setText("sample@gmail.com");

        //パスワード
        mPasswordField = (EditText)view.findViewById(R.id.login_password_edittext);
        mPasswordField.setText("naruko");

        //ログインボタン
        CustomButtonLogin mLoginButton = (CustomButtonLogin) view.findViewById(R.id.emailLoginButton);
        mLoginButton.setOnTouchListener(new View.OnTouchListener() {
            int defaultButtonColor = Color.parseColor("#FF6600");

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float[] hsv = new float[3];
                        Color.colorToHSV(defaultButtonColor, hsv);
                        hsv[2] -= 0.2f;
                        v.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));

                        break;
                    case MotionEvent.ACTION_UP:
                        v.setBackgroundTintList(ColorStateList.valueOf(defaultButtonColor));
                        break;
                }
                return false;
            }
        });
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (formChecker()) {
            //Emailログイン
            ActivitySelectLogin activitySelectLogin = (ActivitySelectLogin)getActivity();
            activitySelectLogin.loginWithEmail(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    //入力チェック
    private boolean formChecker(){
        boolean check = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)){
            mEmailField.setError("メールアドレスが入力されていません");
            check = false;
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)){
            mPasswordField.setError("パスワードが入力されていません");
            check = false;
        }

        return check;
    }
}
