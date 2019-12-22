package com.hiro_a.naruko.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.User;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{

    EditText mEmailField;
    EditText mPasswordField;
    EditText mPasswordField_again;
    EditText mUserNameField;
    Button mRegisterButton;

    FirebaseAuth mFirebaseAuth;
    DatabaseReference mFirebaseDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //登録フォーム
        mEmailField = (EditText)findViewById(R.id.email_login);
        mPasswordField = (EditText)findViewById(R.id.password_login);
        mPasswordField_again = (EditText)findViewById(R.id.password_reg_again);
        mUserNameField = (EditText)findViewById(R.id.username_reg);

        mRegisterButton = (Button)findViewById(R.id.user_reg_button);
        mRegisterButton.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.user_reg_button:
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
                            Intent makeAccount = new Intent(ActivityRegister.this, ActivityLogin.class);
                            startActivity(makeAccount);
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

        return check;
    }

    public void makeUser(String userId, String username, String email){
        User user = new User(username, email);

        mFirebaseDatabaseRef.child("Users").child(userId).child("_id").setValue(userId);
        mFirebaseDatabaseRef.child("Users").child(userId).child("userName").setValue(username);
    }
}