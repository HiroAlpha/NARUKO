package com.hiro_a.naruko.view.ChatView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class ChatCanvasView_impassive extends View {
    float textCircleRadius;   //回転半径(dp)
    float boundaryLineRedius;
    float radiusPivotOffset = 45;

    Paint centerCirclePaint;
    Paint outerCirclePaint;
    Paint graphicPaint_Line;

    Path centerCirclePath;
    Path outerCirclePath;
    Path graphicPath_Line;

    public ChatCanvasView_impassive(Context context, AttributeSet attrs){
        super(context, attrs);

        //中心円設定
        centerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerCirclePaint.setStyle(Paint.Style.FILL);
        centerCirclePaint.setColor(Color.rgb(192,252,214));

        //外側円設定
        outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setColor(Color.rgb(172,222,242));
        outerCirclePaint.setStrokeWidth(20);

        //UI白線Path設定
        graphicPaint_Line = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Line.setStyle(Paint.Style.STROKE);
        graphicPaint_Line.setColor(Color.WHITE);
        graphicPaint_Line.setStrokeWidth(3);

        centerCirclePath = new Path();
        outerCirclePath = new Path();
        graphicPath_Line = new Path();

        textCircleRadius = convertDp2Px(200, getContext());
        boundaryLineRedius = textCircleRadius-35;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(90);

        //中心円
        centerCirclePath.addCircle(-radiusPivotOffset, 0, boundaryLineRedius-20, Path.Direction.CW);
        canvas.drawPath(centerCirclePath, centerCirclePaint);

        //外側円
        outerCirclePath.addCircle(-radiusPivotOffset, 0, boundaryLineRedius-10, Path.Direction.CW);
        canvas.drawPath(outerCirclePath, outerCirclePaint);

        //UI白線
        graphicPath_Line.addCircle(-radiusPivotOffset, 0, boundaryLineRedius, Path.Direction.CW);
        graphicPath_Line.addCircle(-radiusPivotOffset, 0, boundaryLineRedius-20, Path.Direction.CW);
        canvas.drawPath(graphicPath_Line, graphicPaint_Line);
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public static float convertPx2Dp(int px, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
}
