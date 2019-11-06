package com.hiro_a.naruko;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    FirebaseAuth mFirebaseAuth;

    EditText mEmailField;
    EditText mPasswordField;
    Button mLoginButton;
    TextView mMakeAccountButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //メールアドレス、パスワードフォーム
        mEmailField = (EditText)findViewById(R.id.email_login);
        mPasswordField = (EditText)findViewById(R.id.password_login);
        mEmailField.setText("sample@gmail.com");
        mPasswordField.setText("naruko");

        mLoginButton = (Button)findViewById(R.id.user_login_button);
        mLoginButton.setOnClickListener(this);

        //アカウント作成ボタン
        mMakeAccountButton = (TextView)findViewById(R.id.makeAccount);
        mMakeAccountButton.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.user_login_button:
                //ログイン
                login(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;

            case R.id.makeAccount:
                //登録フォームへ
                Intent makeAccount = new Intent(ActivityLogin.this, ActivityRegister.class);
                startActivity(makeAccount);
                break;
        }
    }

    //ログイン
    private void login(String email, String password){
        if(!formChecker()){
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ActivityLogin.this, "ログイン！", Toast.LENGTH_SHORT).show();
//                    Intent mainPage = new Intent(ActivityLogin.this, ActivityMainPage.class);
//                    startActivity(mainPage);

                    Intent chatPage = new Intent(ActivityLogin.this, ActivityChat.class);
                    startActivity(chatPage);
                }else {
                    Toast.makeText(ActivityLogin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
