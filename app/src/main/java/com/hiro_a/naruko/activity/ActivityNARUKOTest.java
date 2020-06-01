package com.hiro_a.naruko.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.view.NarukoUserIconPopoutView;
import com.hiro_a.naruko.view.UserIconView;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.floor;
import static java.lang.StrictMath.sin;

public class ActivityNARUKOTest extends AppCompatActivity {
    Context context;

    RelativeLayout relativeLayout;

    String TAG = "NARUKO_DEBUG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.activity_naruko_test);

        //ウィンドウサイズ取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        final Point screenSize = new Point();
        disp.getSize(screenSize);

        relativeLayout = (RelativeLayout) findViewById(R.id.narukoRelativeLayout);

        final NarukoUserIconPopoutView narukoUserIconPopoutView = (NarukoUserIconPopoutView)findViewById(R.id.icon_view);

        Button sendMessage = (Button) findViewById(R.id.button_send_message);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                narukoUserIconPopoutView.addUserIcon(screenSize, relativeLayout, "N/A");
            }
        });

        Button reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                narukoUserIconPopoutView.removeUserIcon(relativeLayout);
            }
        });
    }
}
