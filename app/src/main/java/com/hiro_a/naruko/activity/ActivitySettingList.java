package com.hiro_a.naruko.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hiro_a.naruko.R;

public class ActivitySettingList extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;

    private static final String[] settings = {
      "ユーザー設定", "チャットルーム設定", "ログアウト", "利用規約"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_list);

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

    //メニュークリック時
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //戻るボタン
            case android.R.id.home:
                Intent menu = new Intent(ActivitySettingList.this, ActivityMenu.class);
                startActivity(menu);
                break;
        }
        return true;
    }

    private void startSettings(String item){
        if (item.equals("ユーザー設定")){
            //to Profile Setting
            Intent setting = new Intent(ActivitySettingList.this, ActivitySettingUserProfile.class);
            startActivity(setting);
        }

        if (item.equals("ログアウト")){
            //Logout
            mFirebaseAuth.signOut();

            Intent logout = new Intent(ActivitySettingList.this, ActivitySelectLogin.class);
            startActivity(logout);
        }

        if (item.equals("利用規約")){
            //利用規約へ
            Intent policy = new Intent(ActivitySettingList.this, ActivitySettingPolicy.class);
            startActivity(policy);
        }
    }
}
