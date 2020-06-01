package com.hiro_a.naruko.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivitySelectLogin;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.PassDecodeTask;
import com.hiro_a.naruko.task.PassEncodeTask;
import com.hiro_a.naruko.view.CustomButton;

public class loginEmail extends Fragment implements View.OnClickListener {
    String TAG = "NARUKO_DEBUG @ loginEmail.fragment";

    private EditText editText_Email, editText_Password;
    private CheckBox checkBox_SaveData;

    private SharedPreferences userData;

    String KEY = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //データ読み込み
        KEY = getString(R.string.ENC_KEY);
        userData = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String email = userData.getString("Email", "");
        String password = new PassDecodeTask().decode(KEY, userData.getString("Password", ""), "BLOWFISH"); //複合化

        //メールアドレス
        editText_Email = (EditText)view.findViewById(R.id.login_email_edittext);
        editText_Email.setText(email);

        //パスワード
        editText_Password = (EditText)view.findViewById(R.id.login_password_edittext);
        editText_Password.setText(password);

        //ログインボタン
        int defaultButtonColor = Color.parseColor("#FF6600");
        CustomButton mLoginButton = (CustomButton) view.findViewById(R.id.emailLoginButton);
        mLoginButton.setOnTouchListener(new ButtonColorChangeTask(defaultButtonColor));
        mLoginButton.setOnClickListener(this);

        //チェックボックス
        checkBox_SaveData = (CheckBox)view.findViewById(R.id.login_datasave_checkbox);
    }

    @Override
    public void onClick(View v) {
        if (formChecker()) {
            if (checkBox_SaveData.isChecked()){
                //ログイン情報を保存
                SharedPreferences.Editor editor = userData.edit();
                String email = editText_Email.getText().toString();
                String pass = editText_Password.getText().toString();
                String encodedPassword = new PassEncodeTask().encode(KEY, pass, "BLOWFISH");    //暗号化
                editor.putString("Email", email);
                editor.putString("Password", encodedPassword);
                editor.apply();

                Log.d(TAG, "LOGIN INFO SAVED!");
                Log.d(TAG, "---------------------------------");
            }

            //Emailログイン
            ActivitySelectLogin activitySelectLogin = (ActivitySelectLogin)getActivity();
            activitySelectLogin.loginWithEmail(editText_Email.getText().toString(), editText_Password.getText().toString());
        }
    }

    //入力チェック
    private boolean formChecker(){
        boolean check = true;

        String email = editText_Email.getText().toString();
        if (TextUtils.isEmpty(email)){
            editText_Email.setError("メールアドレスが入力されていません");
            check = false;
        }

        String password = editText_Password.getText().toString();
        if (TextUtils.isEmpty(password)){
            editText_Password.setError("パスワードが入力されていません");
            check = false;
        }

        return check;
    }
}
