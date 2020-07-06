package com.hiro_a.naruko.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.view.CustomButton;

public class LoginInfo extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //次へボタン
        CustomButton mLoginButton = (CustomButton) view.findViewById(R.id.fLoginInfo_view_next);
        mLoginButton.setOnTouchListener(new ButtonColorChangeTask(getResources().getColor(R.color.colorKarin)));
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
