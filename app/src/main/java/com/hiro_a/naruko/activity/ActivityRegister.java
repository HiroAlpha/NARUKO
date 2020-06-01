package com.hiro_a.naruko.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.view.CustomButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{
    Context context;
    String TAG = "NARUKO_DEBUG @ ActivityRegister";

    EditText editText_Email;
    EditText editText_Password;
    EditText editText_Password_again;
    EditText editText_UserName;
    CustomButton button_Register;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = getApplicationContext();

        editText_Email = (EditText)findViewById(R.id.register_email_edittext);
        editText_Password = (EditText)findViewById(R.id.register_password_edittext);
        editText_Password_again = (EditText)findViewById(R.id.register_password_edittext_check);
        editText_UserName = (EditText)findViewById(R.id.register_username_edittext);

        button_Register = (CustomButton)findViewById(R.id.emailRegisterButton);
        button_Register.setOnClickListener(this);

        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.emailRegisterButton:
                createAccount(editText_Email.getText().toString(), editText_Password.getText().toString());
                break;
        }
    }

    private void createAccount(String email, String password){
        if(!formChecker()){
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            createUser(task.getResult().getUser().getUid(), editText_UserName.getText().toString(), editText_Email.getText().toString());
                            Toast.makeText(context, "登録完了！", Toast.LENGTH_SHORT).show();
                            //ログインフォームへ
//                            Intent makeAccount = new Intent(ActivityRegister.this, ActivityLogin.class);
//                            startActivity(makeAccount);
                        } else {
                            Log.w(TAG, "ERROR: Exception Occured", task.getException());
                            Log.w(TAG, "---------------------------------");
                        }
                    }
            });
    }

    //Check Form is not empty
    private boolean formChecker(){
        boolean check = true;

        String email = editText_Email.getText().toString();
        if (TextUtils.isEmpty(email)){
            editText_Email.setError("メールアドレスが入力されていません");
            check = false;
        }

        String password = editText_Password.getText().toString();
        String passwordCheck = editText_Password_again.getText().toString();
        if (TextUtils.isEmpty(password)){
            editText_Password.setError("パスワードが入力されていません");
            check = false;
        } else if (!(passwordCheck.equals(password))){
            editText_Password_again.setError("パスワードが一致しません");
            check = false;
        }

        String userName = editText_UserName.getText().toString();
        if (TextUtils.isEmpty(userName)){
            editText_Password_again.setError("ユーザー名が入力されていません");
            check = false;
        }

        return check;
    }

    //Create_User
    public void createUser(String userId, String username, String email){
        CollectionReference userRef = firebaseFirestore.collection("users");

        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String time = SD.format(new Date()).toString();

        //String ipAddress = getIp();
        //Log.d(TAG, "IP: " + ipAddress);

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
                Log.w(TAG, "ERROR: Creating User Failed", e);
                Log.w(TAG, "---------------------------------");
            }
        });
    }
}