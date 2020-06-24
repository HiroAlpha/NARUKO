package com.hiro_a.naruko.view.NarukoView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class NarukoView_TopCircle extends View {
    float textCircleRadius;   //回転半径(dp)
    float boundaryLineRedius;
    float radiusPivotOffset = 45;

    Paint centerCirclePaint;
    Paint outerCirclePaint;
    Paint historyButtonPaint;
    Paint graphicPaint_Line;
    Paint backgroundWitePaint;

    Path centerCirclePath;
    Path outerCirclePath;
    Path historyButtonPath;
    Path historyButtonOverritePath;
    Path historyButtonOverritePath_2;
    Path graphicPath_Line;

    public NarukoView_TopCircle(Context context, AttributeSet attrs){
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

        //履歴ボタン設定
        historyButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        historyButtonPaint.setStyle(Paint.Style.FILL);
        historyButtonPaint.setColor(Color.rgb(74,220,250));

        //UI白線Path設定
        graphicPaint_Line = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Line.setStyle(Paint.Style.STROKE);
        graphicPaint_Line.setColor(Color.WHITE);
        graphicPaint_Line.setStrokeWidth(3);

        //履歴ボタン背景設定
        backgroundWitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundWitePaint.setStyle(Paint.Style.FILL);
        backgroundWitePaint.setColor(Color.WHITE);

        centerCirclePath = new Path();
        outerCirclePath = new Path();
        historyButtonPath = new Path();
        historyButtonOverritePath = new Path();
        historyButtonOverritePath_2 = new Path();
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

        //履歴ボタン横側白線
        RectF rect_ButtonSideLine = new RectF(-(int) (boundaryLineRedius+radiusPivotOffset-1.5-20), -(int) (boundaryLineRedius-1.5-20), (int) (boundaryLineRedius-radiusPivotOffset-1.5-20), (int) (boundaryLineRedius-1.5-20));
        canvas.drawArc(rect_ButtonSideLine, 284.5f, 56f, true, backgroundWitePaint);

        //履歴ボタン
        RectF buttonRect = new RectF(-(int) (boundaryLineRedius+radiusPivotOffset-1.5), -(int) (boundaryLineRedius-1.5), (int) (boundaryLineRedius-radiusPivotOffset-1.5), (int) (boundaryLineRedius-1.5));
        canvas.drawArc(buttonRect, 285f, 55f, true, historyButtonPaint);


        //中心部履歴ボタン上書き用円(1)
        historyButtonOverritePath.addCircle(-radiusPivotOffset, 0, boundaryLineRedius-20-35, Path.Direction.CW);
        canvas.drawPath(historyButtonOverritePath, centerCirclePaint);

        //履歴ボタン横側白線
        RectF rect_ButtonTopLine = new RectF(-(int) (boundaryLineRedius+radiusPivotOffset-1.5-20-35), -(int) (boundaryLineRedius-1.5-20-35), (int) (boundaryLineRedius-radiusPivotOffset-1.5-20-35), (int) (boundaryLineRedius-1.5-20-35));
        canvas.drawArc(rect_ButtonTopLine, 284.5f, 56f, true, backgroundWitePaint);

        //中心部履歴ボタン上書き用円(2)
        historyButtonOverritePath_2.addCircle(-radiusPivotOffset, 0, boundaryLineRedius-20-35-4, Path.Direction.CW);
        canvas.drawPath(historyButtonOverritePath_2, centerCirclePaint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float touchedX = event.getX();
        float touchedY = event.getY();

        Point centerPoint = new Point(0, -45);

        double length = Math.hypot(touchedX - centerPoint.x, touchedY - centerPoint.y);

        //Log.d("NARUKO_", "Touched @" + touchedX + ", " + touchedY + "//" + length);

        if (length >= (boundaryLineRedius-20-35-4) && boundaryLineRedius >= length){

            return super.dispatchTouchEvent(event);
        } else {
            return false;
        }
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
