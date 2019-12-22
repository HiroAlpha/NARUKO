package com.hiro_a.naruko.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import com.hiro_a.naruko.R;

public class MenuGroupAdd extends View {
    Paint graphicPaint_Colored_FILL;

    public MenuGroupAdd(Context context) {
        this(context, null);
    }

    public MenuGroupAdd(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuGroupAdd(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //UI下色円設定
        graphicPaint_Colored_FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Colored_FILL.setStyle(Paint.Style.FILL);
        graphicPaint_Colored_FILL.setColor(Color.rgb(255,192,203));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(0, 0, convertDp2Px(100, getContext()), graphicPaint_Colored_FILL);
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}