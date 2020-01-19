package com.hiro_a.naruko.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.User;
import com.hiro_a.naruko.view.LoginButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{

    EditText mEmailField;
    EditText mPasswordField;
    EditText mPasswordField_again;
    EditText mUserNameField;
    LoginButton mRegisterButton;

    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseDatabase;

    String TAG = "NARUKO_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //登録フォーム
        mEmailField = (EditText)findViewById(R.id.register_email_edittext);
        mPasswordField = (EditText)findViewById(R.id.register_password_edittext);
        mPasswordField_again = (EditText)findViewById(R.id.register_password_edittext_check);
        mUserNameField = (EditText)findViewById(R.id.register_username_edittext);

        mRegisterButton = (LoginButton)findViewById(R.id.emailRegisterButton);
        mRegisterButton.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.emailRegisterButton:
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
        }
    }

    private void createAccount(String email, String password){
        if(!formChecker()){
            return;
        }

        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            makeUser(task.getResult().getUser().getUid(), mUserNameField.getText().toString(), mEmailField.getText().toString());
                            Toast.makeText(ActivityRegister.this, "登録完了！", Toast.LENGTH_SHORT).show();
                            //ログインフォームへ
//                            Intent makeAccount = new Intent(ActivityRegister.this, ActivityLogin.class);
//                            startActivity(makeAccount);
                        } else {
                            Toast.makeText(ActivityRegister.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            });
    }

    private boolean formChecker(){
        boolean check = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)){
            mEmailField.setError("メールアドレスが入力されていません");
            check = false;
        }

        String password = mPasswordField.getText().toString();
        String passwordCheck = mPasswordField_again.getText().toString();
        if (TextUtils.isEmpty(password)){
            mPasswordField.setError("パスワードが入力されていません");
            check = false;
        } else if (!(passwordCheck.equals(password))){
            mPasswordField_again.setError("パスワードが一致しません");
            check = false;
        }

        String userName = mUserNameField.getText().toString();
        if (TextUtils.isEmpty(userName)){
            mPasswordField_again.setError("ユーザー名が入力されていません");
            check = false;
        }

        return check;
    }

    public void makeUser(String userId, String username, String email){
        CollectionReference userRef = mFirebaseDatabase.collection("users");

        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String time = SD.format(new Date()).toString();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("datetime", time);
        newUser.put("userId", userId);
        newUser.put("userName", username);

        userRef.document(userId).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {


                //ログイン画面へ
                Intent selectLogin = new Intent(ActivityRegister.this, ActivitySelectLogin.class);
                selectLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selectLogin);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding User to Database", e);
                Log.w(TAG, "---------------------------------");
            }
        });
    }
}