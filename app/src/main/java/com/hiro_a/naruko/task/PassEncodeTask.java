package com.hiro_a.naruko.task;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class PassEncodeTask {
    private String TAG = "NARUKO_DEBUG";

    public String encode(String KEY, String password, String algorithm){
        String encodedPass = "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
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
        }

        return encodedPass;
    }
}
