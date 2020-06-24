package com.hiro_a.naruko.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.fragment.Dialog;
import com.hiro_a.naruko.view.CustomButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "NARUKO_DEBUG @ ActivityRegister";
    private Context context;

    private EditText editText_Email;
    private EditText editText_Password;
    private EditText editText_Password_again;
    private EditText editText_UserName;

    private FragmentManager fragmentManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //コンテキスト
        context = getApplicationContext();

        //メールアドレス入力スペース
        editText_Email = findViewById(R.id.register_editText_email);

        //パスワード入力スペース
        editText_Password = findViewById(R.id.register_editText_password);

        //メールアドレス入力スペース（確認）
        editText_Password_again = findViewById(R.id.register_editText_passwordAgain);

        //ユーザー名入力スペース
        editText_UserName = findViewById(R.id.register_editText_userName);

        //登録ボタン
        CustomButton button_Register = findViewById(R.id.register_view_register);
        button_Register.setOnClickListener(this);

        //フラグメントマネージャー
        fragmentManager = getSupportFragmentManager();

        //*** Firebase ***
        //FireAuth
        firebaseAuth = FirebaseAuth.getInstance();
        //Firestore
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view){
        //アカウント作成
        createAccount(editText_Email.getText().toString(), editText_Password.getText().toString());
    }

    //アカウント作成
    private void createAccount(final String email, final String password){
        //フォームに不備がある場合
        if(!formChecker()){
            //アカウント作成を終了
            return;
        }

        //アカウント作成（メールアドレス・パスワード）
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //AuthResult
                            AuthResult authResult = task.getResult();
                            //AuthResultがnullだった場合
                            if (authResult == null){
                                Log.w(TAG, "ERROR: Exception Occured", task.getException());
                                Log.w(TAG, "---------------------------------");

                                //エラーダイアログ
                                new Dialog().show(fragmentManager, TAG);
                                //メソッド終了
                                return;
                            }

                            //ユーザー
                            FirebaseUser user = task.getResult().getUser();
                            //ユーザーがnullだった場合
                            if (user == null){
                                Log.w(TAG, "ERROR: Exception Occured", task.getException());
                                Log.w(TAG, "---------------------------------");

                                //エラーダイアログ
                                new Dialog().show(fragmentManager, TAG);
                                //メソッド終了
                                return;
                            }

                            //ユーザーID
                            String userId = user.getUid();

                            //ユーザー情報をFirestoreに登録
                            createUser(userId, editText_UserName.getText().toString());

                            //登録完了トースト
                            Toast.makeText(context, "登録完了", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "ERROR: Exception Occured", task.getException());
                            Log.w(TAG, "---------------------------------");

                            //エラーダイアログ
                            new Dialog().show(fragmentManager, TAG);
                        }
                    }
            });
    }

    //フォーム内容確認
    private boolean formChecker(){
        //フォーム内容判定の仮設定
        boolean check = true;

        //メールアドレスの確認
        String email = editText_Email.getText().toString();
        //メールアドレスが空の場合
        if (TextUtils.isEmpty(email)){
            //エラーメッセージ
            editText_Email.setError("メールアドレスが入力されていません");
            //判定false
            check = false;
        }

        //パスワードの確認
        String password = editText_Password.getText().toString();
        String passwordCheck = editText_Password_again.getText().toString();
        if (TextUtils.isEmpty(password)){   //パスワードが空の場合
            //エラーメッセージ
            editText_Password.setError("パスワードが入力されていません");
            //判定false
            check = false;
        } else if (!(passwordCheck.equals(password))){  //パスワードとパスワード（確認）が違う場合
            //エラーメッセージ
            editText_Password_again.setError("パスワードが一致しません");
            //判定false
            check = false;
        }

        //ユーザー名
        String userName = editText_UserName.getText().toString();
        //ユーザー名が空の場合
        if (TextUtils.isEmpty(userName)){
            //エラーメッセージ
            editText_Password_again.setError("ユーザー名が入力されていません");
            //判定false
            check = false;
        }

        return check;
    }

    //ユーザー情報をFirestoreに登録
    private void createUser(String userId, String username){
        //ユーザー情報登録用データーベースへのパス
        CollectionReference userRef = firebaseFirestore.collection("users");

        //作成日時
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String userCreated = SD.format(new Date());

        //登録情報
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("UserCreated", userCreated);
        newUser.put("UserId", userId);
        newUser.put("UserName", username);
        newUser.put("UserImageIs", false);

        //Firestoreに情報を送信
        userRef.document(userId).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void done) {

                //ログイン画面アクティビティに移行
                Intent selectLogin = new Intent(ActivityRegister.this, ActivitySelectLogin.class);
                selectLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selectLogin);

                Log.d(TAG, "SUCSESS: Creating User");
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ERROR: Creating User", e);
                Log.w(TAG, "---------------------------------");

                //送信失敗時ダイアログ表示
                new AlertDialog.Builder(context)
                        .setTitle("エラー")
                        .setMessage("ユーザー情報の送信に失敗しました。")
                        .show();
            }
        });
    }
}