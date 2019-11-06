package com.hiro_a.naruko;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class CanvasView_userIcon_outerCircle extends View {
    float lineWidth = convertDp2Px(2, getContext());
    float iconOffset = convertDp2Px(120, getContext());
    float iconBorderOffset = 0;
    Point userGrid = new Point(0, 0);

    Paint iconOuterCirclePaint;

    Path iconOuterCirclePath;

    public CanvasView_userIcon_outerCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        iconOuterCirclePath = new Path();

        //UI白線Path設定
        iconOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconOuterCirclePaint.setStyle(Paint.Style.STROKE);
        iconOuterCirclePaint.setColor(Color.WHITE);
        iconOuterCirclePaint.setStrokeWidth(lineWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.rotate(90);

        RectF outerCircleRect = new RectF(userGrid.x+20+lineWidth/2, userGrid.y-iconOffset+20+lineWidth/2, userGrid.x+iconOffset-20-lineWidth/2, userGrid.y-20-lineWidth/2);
        iconOuterCirclePath.addArc(outerCircleRect, 45, 90);
        iconOuterCirclePath.addArc(outerCircleRect, 225, 90);
        //iconOuterCirclePath.addRect(outerCircleRect, Path.Direction.CW);
        canvas.drawPath(iconOuterCirclePath, iconOuterCirclePaint);
    }

    public void getUserGrid(Point grid){
        userGrid = new Point(grid.y, -grid.x);

        invalidate();
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}