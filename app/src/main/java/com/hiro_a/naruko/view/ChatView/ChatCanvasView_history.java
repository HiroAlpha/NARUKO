package com.hiro_a.naruko.view.ChatView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class ChatCanvasView_history extends View {
    float textSize = convertDp2Px(15, getContext());  //文字サイズ
    float radius;   //回転半径
    float radiusPivotOffset = 45;
    float sweepangle = 89.15f;
    float chatCircleRedius = convertDp2Px(10, getContext()); //UI白丸半径
    String text = "";
    ArrayList<String> textHolder = new ArrayList<String>(); //過去の文字列格納用Array
    float btmArcLeft, btmArcTop, btmArcRight, btmArcBttom;
    float topArcLeft, topArcTop, topArcRight, topArcBttom;
    double rightCircleSin, rightCircleCos;
    float coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom;
    float shadowCircleLeft, shadowCircleTop, shadowCircleRight, shadowCircleBttom;

    Paint textPaint;
    Paint pathPaint;
    Paint graphicPaint_Line;
    Paint graphicPaint_Colored, graphicPaint_Colored_FILL;
    Paint shadowPaint, shadowPaint_FILL;

    Path textPath;
    Path graphicPath;
    Path graphicPath_Line;
    Path graphicPath_Colored;
    Path shadowPath;

    public ChatCanvasView_history(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "anzu_font.ttf"); //フォント

        //文字列設定
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(textSize);

        //文字列補助線Path設定
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(Color.RED);
        pathPaint.setStrokeWidth(1);

        //色付き
        //UI下色円設定
        graphicPaint_Colored_FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Colored_FILL.setStyle(Paint.Style.FILL);
        graphicPaint_Colored_FILL.setColor(Color.rgb(255,192,203));
        //UI下色Path設定
        graphicPaint_Colored = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Colored.setStyle(Paint.Style.STROKE);
        graphicPaint_Colored.setColor(Color.rgb(255,192,203));
        graphicPaint_Colored.setStrokeWidth(chatCircleRedius*2);

        //影
        //UI下色影円設定
        shadowPaint_FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint_FILL.setStyle(Paint.Style.FILL);
        shadowPaint_FILL.setColor(Color.argb(128, 100, 100, 100));
        //UI下色影Path設定
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setColor(Color.argb(128, 100, 100, 100));
        shadowPaint.setStrokeWidth(chatCircleRedius);

        //UI白線Path設定
        graphicPaint_Line = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPaint_Line.setStyle(Paint.Style.STROKE);
        graphicPaint_Line.setColor(Color.WHITE);
        graphicPaint_Line.setStrokeWidth(3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(90);  //反時計回りに描画すると文字が画面外なので回転

        if (textHolder.size() > 1){
            int multiplier = 0;
            int textSpan = 0;
            if (textHolder.size() >= 7){
                textSpan = textHolder.size() - 6;
            }

            //入力されたものより1つ前の文字列から最も古いものまで
            for (int i=textSpan; i<textHolder.size()-1; i++){
                radius = (multiplier * (textSize + convertDp2Px(5, getContext()))) + convertDp2Px(200, getContext());    //TextSize分半径をずらす
                sweepangle = (multiplier * 0.06f) + 89.15f;

                //文字列補助線
                textPath = new Path();
                textPath.addCircle(-radiusPivotOffset, 0, radius, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                //canvas.drawPath(textPath, pathPaint);

                //右白丸描画用座標
                rightCircleSin = (sin(Math.toRadians(90))*(radius-(textSize/2)));
                rightCircleCos = (cos(Math.toRadians(90))*(radius-(textSize/2))) - radiusPivotOffset;
                //円弧描画用座標（上側）
                topArcLeft = -(radiusPivotOffset+radius-(textSize/2)-chatCircleRedius);
                topArcTop = -(radius-(textSize/2)-chatCircleRedius);
                topArcRight = radius-radiusPivotOffset-(textSize/2)-chatCircleRedius;
                topArcBttom = radius-(textSize/2)-chatCircleRedius;
                //円弧描画用座標（下側）
                btmArcLeft = -(radiusPivotOffset+radius-(textSize/2)+chatCircleRedius);
                btmArcTop = -(radius-(textSize/2)+chatCircleRedius);
                btmArcRight = radius-radiusPivotOffset-(textSize/2)+chatCircleRedius;
                btmArcBttom = radius-(textSize/2)+chatCircleRedius;
                //色付き円弧描画用座標
                coloredArcLeft = topArcLeft-chatCircleRedius;
                coloredArcTop = topArcTop-chatCircleRedius;
                coloredArcRight = topArcRight+chatCircleRedius;
                coloredArcBttom = topArcBttom+chatCircleRedius;
                //影円描画用座標（左側）
                shadowCircleLeft = radius-radiusPivotOffset-(textSize/2);
                shadowCircleTop = -chatCircleRedius-convertDp2Px(3, getContext());
                shadowCircleRight = radius-radiusPivotOffset-(textSize/2)+(chatCircleRedius+chatCircleRedius/2);
                shadowCircleBttom = chatCircleRedius-convertDp2Px(3, getContext());

                shadowPath = new Path();
                graphicPath_Colored = new Path();
                graphicPath = new Path();
                graphicPath_Line = new Path();

                //UI影
                RectF shadowArc = new RectF(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);   //円弧範囲
                shadowPath.addArc(shadowArc, 270, sweepangle); //円弧
                canvas.drawPath(shadowPath, shadowPaint);
                RectF shadowCircle = new RectF(shadowCircleLeft, shadowCircleTop, shadowCircleRight, shadowCircleBttom);   //半円範囲
                canvas.drawArc(shadowCircle, 0, 180, false, shadowPaint_FILL);  //円だとかぶるので半円

                //UI下色
                RectF coloredArc = new RectF(coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom);   //円弧範囲
                graphicPath_Colored.addArc(coloredArc, 270, 90); //円弧
                canvas.drawPath(graphicPath_Colored, graphicPaint_Colored);

                //共通項
                graphicPath.addCircle(radius-radiusPivotOffset-(textSize/2), 0, chatCircleRedius, Path.Direction.CW); //左丸
                graphicPath.addCircle((float) rightCircleCos, -((float)rightCircleSin), chatCircleRedius, Path.Direction.CW); //右丸
                canvas.drawPath(graphicPath, graphicPaint_Colored_FILL);
                canvas.drawPath(graphicPath, graphicPaint_Line);

                //UI白線
                RectF topArcRect = new RectF(topArcLeft, topArcTop, topArcRight, topArcBttom);   //円弧上側範囲
                graphicPath_Line.addArc(topArcRect, 270, 90); //円弧上側
                RectF bottomArcRect = new RectF(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);  //円弧下側範囲
                graphicPath_Line.addArc(bottomArcRect, 270, 90); //円弧下側
                canvas.drawPath(graphicPath_Line, graphicPaint_Line);

                //曲線文字列
                canvas.drawTextOnPath(textHolder.get(i), textPath, 30, 0, textPaint);

                multiplier++;
            }
        }
    }

    public void getMessage(String messageText){
        textHolder.add(messageText);    //Arrayに文字列を追加

        //最初の入力は描画しない
        if (textHolder.size() > 1){
            invalidate();
        }
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}
