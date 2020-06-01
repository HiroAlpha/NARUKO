package com.hiro_a.naruko.task;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class PassDecodeTask {
    private String TAG = "NARUKO_DEBUG";

    public String decode(String KEY, String encodedPassword, String algorithm){
        String decodedPass = "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
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
