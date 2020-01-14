package com.hiro_a.naruko.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hiro_a.naruko.Fragment.loginSelect;
import com.hiro_a.naruko.R;

public class ActivitySelectLogin extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlogin);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentSelectLogin = new loginSelect();
        FragmentTransaction transactionToSelect = fragmentManager.beginTransaction();
        transactionToSelect.replace(R.id.login_fragment, fragmentSelectLogin);
        transactionToSelect.addToBackStack(null);
        transactionToSelect.commit();
    }
}
