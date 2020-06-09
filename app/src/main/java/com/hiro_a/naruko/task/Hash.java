package com.hiro_a.naruko.task;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Hash {
    String TAG = "NARUKO_DEBUG @ Hash";

    String salt_code = "dfsf7se89fs9fd98asd77a8d7a";

    public String doHash(String password){
        //ハッシュ1回目
        String firstHash = getHash(password);

        //ハッシュ2回目
        String secondHash = getHash(firstHash + salt_code);
        Log.d(TAG, firstHash + " + " + salt_code);

        return secondHash;
    }

    private String getHash(String password){
        String pass_hashed = "";

        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(password.getBytes());
            BigInteger bigInteger = new BigInteger(1, hash);
            pass_hashed = String.format("%0" + (hash.length << 1) + "x", bigInteger);

            Log.d(TAG, "SUCSESS: Hashed");
            Log.d(TAG, password + " => " + pass_hashed);
            Log.d(TAG, "---------------------------------");
        } catch (Exception e){
            Log.d(TAG, "ERROR: Exception Occured", e);
            Log.d(TAG, "---------------------------------");
        }

        return pass_hashed;
    }
}
