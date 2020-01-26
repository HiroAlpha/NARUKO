package com.hiro_a.naruko.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

public class CustomButtonLogin extends AppCompatButton {
    int defaultButtonColor;

    public CustomButtonLogin(Context context) {
        super(context);
    }

    public CustomButtonLogin(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButtonLogin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        super.performClick();

        return true;
    }
}
