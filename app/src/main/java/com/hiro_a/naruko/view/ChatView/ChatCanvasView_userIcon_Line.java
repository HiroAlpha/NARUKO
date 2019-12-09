package com.hiro_a.naruko.view.ChatView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class ChatCanvasView_userIcon_Line extends View {
    int count = 0;
    int radius = 400;
    int textSize = 30;
    float iconOffset = 0;
    float lineStartX, lineStartY;
    double lineEndX, lineEndY;
    Point userGrid = new Point(0, 0);

    int animFps = 90;
    long startTime, elapsedTime;
    boolean isRunning = false;

    Paint linePaint;
    Paint iconOuterCirclePaint;

    Path iconOuterCirclePath;

    double deg = 90;
    long sleepTime = 1000 / animFps;

    public ChatCanvasView_userIcon_Line(Context context, AttributeSet attrs) {
        super(context, attrs);
        iconOuterCirclePath = new Path();

        //UI白線Path設定
        iconOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconOuterCirclePaint.setStyle(Paint.Style.STROKE);
        iconOuterCirclePaint.setColor(Color.WHITE);
        iconOuterCirclePaint.setStrokeWidth(2);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(5);
        linePaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.rotate(90);

        if (isRunning){
            startTime = System.currentTimeMillis();
            lineEndX = cos((Math.PI/180)*deg)*radius+(textSize/2)+45;
            lineEndY = sin((Math.PI/180)*deg)*radius-(textSize/2);

            canvas.drawLine(lineStartX+iconOffset, lineStartY-iconOffset, -(float)lineEndX, -(float)lineEndY, linePaint);

            elapsedTime = System.currentTimeMillis();
            if (elapsedTime-startTime < sleepTime){
                try{
                    Thread.sleep(sleepTime-(elapsedTime-startTime));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            if (deg < 175){
                deg = deg + 270/animFps;
                invalidate();
            }else {
                isRunning = false;
            }
        }else {
            lineEndX = cos((Math.PI/180)*deg)*radius+(textSize/2)+45;
            lineEndY = sin((Math.PI/180)*deg)*radius-(textSize/2);

            canvas.drawLine(lineStartX+iconOffset, lineStartY-iconOffset, -(float)lineEndX, -(float)lineEndY, linePaint);
        }
    }

    public void getUserGrid(Point grid){
        userGrid = new Point(grid.y, -grid.x);

        //1回目の文字列は既定の半径、2回目以降はTextSize分ずらす
        if (count < 6){
            count++;
        }
        radius = ((count-1) * (textSize+10)) + 400;

        iconOffset = convertDp2Px(50, getContext());
        lineStartX = userGrid.x;
        lineStartY = userGrid.y;

        isRunning = true;
        deg = 90;
        invalidate();
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}
