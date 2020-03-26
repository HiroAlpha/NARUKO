package com.hiro_a.naruko.task;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.view.CustomImageView;

public class ButtonColorChangeTask implements View.OnTouchListener {
    int defaultButtonColor;

    String TAG = "NARUKO_DEBUG";

    public ButtonColorChangeTask(int defaultButtonColor){
        this.defaultButtonColor = defaultButtonColor;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.getClass().getName().contains("CustomImageView")){  //CustomImageViewの場合
            CustomImageView customImageView = (CustomImageView) view.findViewById(view.getId());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float[] hsv = new float[3];
                    Color.colorToHSV(defaultButtonColor, hsv);
                    hsv[2] -= 0.2f;
                    customImageView.setColorFilter(Color.HSVToColor(hsv));

                    break;
                case MotionEvent.ACTION_UP:
                    customImageView.setColorFilter(defaultButtonColor);
                    break;
            }
        } else if (view.getClass().getName().contains("CustomButton")){  //CustomButtonの場合
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float[] hsv = new float[3];
                    Color.colorToHSV(defaultButtonColor, hsv);
                    hsv[2] -= 0.2f;
                    view.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));

                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackgroundTintList(ColorStateList.valueOf(defaultButtonColor));
                    break;
            }
        }

        return false;
    }
}
