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
import com.hiro_a.naruko.view.CustomButton;

public class LoginEmail extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    String TAG = "NARUKO_DEBUG @ loginEmail.fragment";

    private int onLongCount = 0;

    private EditText editText_Email, editText_Password;
    private CheckBox checkBox_SaveData;

    private SharedPreferences userData;

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
        userData = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String email = userData.getString("UserEmail", "");
        String password = userData.getString("UserPassword", ""); //複合化
        boolean loginSave = userData.getBoolean("LoginSave", false);

        //メールアドレス
        editText_Email = (EditText)view.findViewById(R.id.fLoginEmail_editText_email);

        //パスワード
        editText_Password = (EditText)view.findViewById(R.id.fLoginEmail_editText_password);

        //ログインボタン
        int defaultButtonColor = Color.parseColor("#FF6600");
        CustomButton mLoginButton = (CustomButton) view.findViewById(R.id.fLoginEmail_view_login);
        mLoginButton.setOnTouchListener(new ButtonColorChangeTask(defaultButtonColor));
        mLoginButton.setOnClickListener(this);
        mLoginButton.setOnLongClickListener(this);

        //チェックボックス
        checkBox_SaveData = (CheckBox)view.findViewById(R.id.fLoginEmail_check_saveLogin);
        checkBox_SaveData.setChecked(loginSave);

        if (loginSave){
            editText_Email.setText(email);
            editText_Password.setText(password);
        }
    }

    @Override
    public void onClick(View v) {
        if (formChecker()) {
            SharedPreferences.Editor editor = userData.edit();

            if (checkBox_SaveData.isChecked()){
                //ログイン情報を保存
                String email = editText_Email.getText().toString();
                String pass = editText_Password.getText().toString();

                editor.putString("UserEmail", email);
                editor.putString("UserPassword", pass);
                editor.putBoolean("LoginSave", true);
                editor.apply();

                Log.d(TAG, "LOGIN INFO SAVED");
                Log.d(TAG, "---------------------------------");
            } else {
                //ログイン情報を保存しない
                editor.putString("UserEmail", "");
                editor.putString("UserPassword", "");
                editor.putBoolean("LoginSave", false);
                editor.apply();

                Log.d(TAG, "LOGIN INFO NOT SAVED");
                Log.d(TAG, "---------------------------------");
            }

            //Emailログイン
            ActivitySelectLogin activitySelectLogin = (ActivitySelectLogin)getActivity();
            activitySelectLogin.loginWithEmail(editText_Email.getText().toString(), editText_Password.getText().toString());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        onLongCount++;

        if (onLongCount == 3){
            SharedPreferences.Editor editor = userData.edit();
            editor.clear().apply();

            Log.d(TAG, "SHAREDPREFERENCES RESET");
            Log.d(TAG, "---------------------------------");

            onLongCount = 0;
        }

        return true;
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
