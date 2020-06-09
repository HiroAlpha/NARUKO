package com.hiro_a.naruko.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hiro_a.naruko.R;

public class ActivitySetting extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;

    private static final String[] settings = {
      "ユーザー設定", "チャットルーム設定", "ログアウト"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Back_Button active
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ListView settingList = (ListView)findViewById(R.id.setting_list_setting);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settings);
        settingList.setAdapter(arrayAdapter);

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                startSettings(item);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void startSettings(String item){
        if (item.equals("ユーザー設定")){
            //to Profile Setting
            Intent setting = new Intent(ActivitySetting.this, ActivitySettingUserProfile.class);
            startActivity(setting);
        }

        if (item.equals("ログアウト")){
            //Logout
            mFirebaseAuth.signOut();

            Intent logout = new Intent(ActivitySetting.this, ActivitySelectLogin.class);
            startActivity(logout);
        }
    }
}
