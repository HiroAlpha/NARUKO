package com.hiro_a.naruko;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CanvasView_users implements SurfaceHolder.Callback, Runnable {
    int iconOffset = 240;
    float size = 0;
    int animFps = 10;
    long startTime, elapsedTime;
    boolean isRunning = true;
    Point userGrid  = new Point(10, 10);;

    Canvas canvas;
    Paint textPaint;
    Paint iconOuterCirclePaint;

    Path iconOuterCirclePath;

    Thread thread;
    SurfaceHolder surfaceHolder = null;

    public CanvasView_users(Context context, SurfaceView surfaceView) {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.w("CanvasView_user", "created");

        //文字列設定
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);

        //UI白線Path設定
        iconOuterCirclePath = new Path();
        iconOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconOuterCirclePaint.setStyle(Paint.Style.STROKE);
        iconOuterCirclePaint.setColor(Color.WHITE);
        iconOuterCirclePaint.setStrokeWidth(2);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        this.surfaceHolder = surfaceHolder;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
        thread = null;
    }

    @Override
    public void run() {
        Log.w("CanvasView_user", "run");

        long sleepTime = 1000 / animFps;
        while (isRunning){
            Log.w("Debug", "Running...");
            startTime = System.currentTimeMillis();

            doDraw(surfaceHolder);


            elapsedTime = System.currentTimeMillis();
            if (elapsedTime-startTime < sleepTime){
                try{
                    Thread.sleep(sleepTime-(elapsedTime-startTime));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void doDraw(SurfaceHolder surfaceHolder){
        canvas = surfaceHolder.lockCanvas();
        canvas.rotate(90);
        canvas.drawColor(Color.BLACK);

        RectF outerCircleRect = new RectF(userGrid.x+21, userGrid.y-iconOffset+21, userGrid.x+iconOffset-21+size, userGrid.y-21);
        iconOuterCirclePath.addRect(outerCircleRect, Path.Direction.CW);
        canvas.drawPath(iconOuterCirclePath, iconOuterCirclePaint);

        String x = String.valueOf(userGrid.x);
        String y = String.valueOf(userGrid.y);
        canvas.drawText(x+"："+y, 200, -200, textPaint);

        surfaceHolder.unlockCanvasAndPost(canvas);

        size = size+(5/animFps);
    }

    public void getUserGrid(Point grid){
        Log.w("CanvasView_user", "text incoming");
        userGrid = new Point(grid.y, -grid.x);
        doDraw(surfaceHolder);
    }
}


    //    int iconOffset = 240;
//    Point userGrid = new Point(0, 0);
//
//    Paint iconOuterCirclePaint;
//
//    Path iconOuterCirclePath;
//
//    public CanvasView_users(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        iconOuterCirclePath = new Path();
//
//        //UI白線Path設定
//        iconOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        iconOuterCirclePaint.setStyle(Paint.Style.STROKE);
//        iconOuterCirclePaint.setColor(Color.WHITE);
//        iconOuterCirclePaint.setStrokeWidth(2);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        canvas.rotate(90);
//
//        RectF outerCircleRect = new RectF(userGrid.x+21, userGrid.y-iconOffset+21, userGrid.x+iconOffset-21, userGrid.y-21);
//        iconOuterCirclePath.addArc(outerCircleRect, 45, 90);
//        iconOuterCirclePath.addArc(outerCircleRect, 225, 90);
//        iconOuterCirclePath.addRect(outerCircleRect, Path.Direction.CW);
//        canvas.drawPath(iconOuterCirclePath, iconOuterCirclePaint);
//    }
//
//    public void getUserGrid(Point grid){
//        userGrid = new Point(grid.y, -grid.x);
//
//        invalidate();
//    }
//}
