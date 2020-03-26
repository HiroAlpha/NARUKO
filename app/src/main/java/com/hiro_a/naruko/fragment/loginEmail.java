package com.hiro_a.naruko.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class loginEmail extends Fragment implements View.OnClickListener {
    private EditText mEmailField, mPasswordField;
    private CheckBox mLoginSave;

    SharedPreferences userData;

    String KEY = "";
    String TAG = "NARUKO_DEBUG";

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
        KEY = getString(R.string.LOGIN_KEY);
        userData = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String email = userData.getString("email", "");
        String password = decodedPassword(userData.getString("password", ""));

        //メールアドレス
        mEmailField = (EditText)view.findViewById(R.id.login_email_edittext);
        mEmailField.setText(email);

        //パスワード
        mPasswordField = (EditText)view.findViewById(R.id.login_password_edittext);
        mPasswordField.setText(password);

        //ログインボタン
        int defaultButtonColor = Color.parseColor("#FF6600");
        CustomButton mLoginButton = (CustomButton) view.findViewById(R.id.emailLoginButton);
        mLoginButton.setOnTouchListener(new ButtonColorChangeTask(defaultButtonColor));
        mLoginButton.setOnClickListener(this);

        //チェックボックス
        mLoginSave = (CheckBox)view.findViewById(R.id.login_datasave_checkbox);
    }

    @Override
    public void onClick(View v) {
        if (formChecker()) {
            if (mLoginSave.isChecked()){
                //ログイン情報を保存
                SharedPreferences.Editor editor = userData.edit();
                String email = mEmailField.getText().toString();
                String pass = mPasswordField.getText().toString();
                String encodedPassword = encodedPassword(pass);
                editor.putString("email", email);
                editor.putString("password", encodedPassword);
                editor.commit();

                Log.d(TAG, "LOGIN INFO SAVED!");
                Log.d(TAG, "---------------------------------");
            }

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

    //パスワード暗号化
    private String encodedPassword(String password){
        String algo = "BLOWFISH";
        String encodedPass = "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), algo);
            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            encodedPass = Base64.encodeToString(encrypted, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException e){
            Log.w(TAG, "Error NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e){
            Log.w(TAG, "Error NoSuchPaddingException", e);
        } catch (InvalidKeyException e){
            Log.w(TAG, "Error InvalidKeyException", e);
        } catch (IllegalBlockSizeException e){
            Log.w(TAG, "Error IllegalBlockSizeException", e);
        } catch (BadPaddingException e){
            Log.w(TAG, "Error BadPaddingException", e);
        } finally {
            Log.d(TAG, "END ENCODING PASSWORD");
            Log.d(TAG, password + " => " + encodedPass);
            Log.d(TAG, "---------------------------------");
            return encodedPass;
        }
    }

    //パスワード複合化
    private String decodedPassword(String encodedPassword){
        String algo = "BLOWFISH";
        String decodedPass = "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), algo);
            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decByte = Base64.decode(encodedPassword, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decByte);
            decodedPass = new String(decrypted);

        } catch (NoSuchAlgorithmException e){
            Log.w(TAG, "Error NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e){
            Log.w(TAG, "Error NoSuchPaddingException", e);
        } catch (InvalidKeyException e){
            Log.w(TAG, "Error InvalidKeyException", e);
        } catch (IllegalBlockSizeException e){
            Log.w(TAG, "Error IllegalBlockSizeException", e);
        } catch (BadPaddingException e){
            Log.w(TAG, "Error BadPaddingException", e);
        } finally {
            Log.d(TAG, "END DECODING PASSWORD");
            Log.d(TAG, encodedPassword + " => " + decodedPass);
            Log.d(TAG, "---------------------------------");
            return decodedPass;
        }
    }
}
