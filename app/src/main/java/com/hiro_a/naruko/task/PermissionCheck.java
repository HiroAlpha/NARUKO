package com.hiro_a.naruko.task;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hiro_a.naruko.R;

public class PermissionCheck {
    static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    static final int ACCESS_NETWORK_STATE_REQUEST_CODE = 3;
    static final int ACCESS_WIFI_STATE_REQUEST_CODE = 4;
    static final int INTERNET_REQUEST_CODE = 5;

    public void getPermission(Context context, Activity activity){

        int read_external_srorage = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write_external_srorage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int access_network_state = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
        int acsess_wifi_state = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
        int internet = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET);

        String alertPermission = context.getString(R.string.ALERT_PERMISSION_NOT_AVIALABLE);

        //if Permission not gtanted show permission dialog
        if (read_external_srorage != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                String alertMessage = context.getString(R.string.ALERT_READ_EXTERNAL_STORAGE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(alertPermission + "\n" + alertMessage);
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, READ_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }

        if (write_external_srorage != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                String alertMessage = context.getString(R.string.ALERT_WRITE_EXTERNAL_STORAGE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(alertPermission + "\n" + alertMessage);
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }

        if (access_network_state != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_NETWORK_STATE)) {
                String alertMessage = context.getString(R.string.ALERT_NETWORK_STATE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(alertPermission + "\n" + alertMessage);
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_NETWORK_STATE
                }, ACCESS_NETWORK_STATE_REQUEST_CODE);
            }
        }

        if (acsess_wifi_state != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_WIFI_STATE)) {
                String alertMessage = context.getString(R.string.ALERT_WIFI_STATE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(alertPermission + "\n" + alertMessage);
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_WIFI_STATE
                }, ACCESS_WIFI_STATE_REQUEST_CODE);
            }
        }

        if (internet != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.INTERNET)) {
                String alertMessage = context.getString(R.string.ALERT_INTERNET);

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(alertPermission + "\n" + alertMessage);
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.INTERNET
                }, INTERNET_REQUEST_CODE);
            }
        }
    }
}
