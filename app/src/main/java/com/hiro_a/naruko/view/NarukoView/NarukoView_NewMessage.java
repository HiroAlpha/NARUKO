package com.hiro_a.naruko.view.NarukoView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;

public class NarukoView_NewMessage extends View {
    private int lineCount;  //文字列受け取り回数
    private float textSize = convertDp2Px(15, getContext());  //文字サイズ
    private float chatCircleRedius = convertDp2Px(10, getContext());  //UI白丸半径
    private String message;
    private boolean drawMode = false;
    private ArrayList<String> textHolder = new ArrayList<String>(); //過去の文字列格納用Array

    private Paint textPaint;
    private Paint textPathPaint;
    private Paint graphicPain_Line;
    private Paint colorPaint_Stroke, colorPaint_Fill;
    private Paint shadowPaint_Stroke, shadowPaint_Fill;

    private Path textPath;
    private Path textPathSecond;
    private Path shadowPath;
    private Path graphicPath;
    private Path graphicPath_Colored;
    private Path graphicPath_Line;

    private RectF shadowArc;
    private RectF shadowCircle;
    private RectF coloredArc;
    private RectF topArcRect;
    private RectF bottomArcRect;

    public NarukoView_NewMessage(Context context, AttributeSet attrs) {
        super(context, attrs);

        //フォント
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "anzu_font.ttf");

        //文字列設定
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(typeface);

        //文字列補助線設定
        textPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPathPaint.setStyle(Paint.Style.STROKE);
        textPathPaint.setColor(Color.BLACK);
        textPathPaint.setStrokeWidth(1);

        //左円設定
        colorPaint_Fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint_Fill.setStyle(Paint.Style.FILL);
        //チャットバー設定
        colorPaint_Stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint_Stroke.setStyle(Paint.Style.STROKE);

        //UI白線Path設定
        graphicPain_Line = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphicPain_Line.setStyle(Paint.Style.STROKE);
        graphicPain_Line.setColor(Color.WHITE);
        graphicPain_Line.setStrokeWidth(3);

        //UI下色影円設定
        shadowPaint_Fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint_Fill.setStyle(Paint.Style.FILL);
        shadowPaint_Fill.setColor(Color.argb(128, 100, 100, 100));
        //UI下色影Path設定
        shadowPaint_Stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint_Stroke.setStyle(Paint.Style.STROKE);
        shadowPaint_Stroke.setColor(Color.argb(128, 100, 100, 100));
        shadowPaint_Stroke.setStrokeWidth(chatCircleRedius);

        //パス
        textPath = new Path();
        textPathSecond = new Path();
        shadowPath = new Path();
        graphicPath = new Path();
        graphicPath_Colored = new Path();
        graphicPath_Line = new Path();

        //範囲
        shadowArc = new RectF();
        shadowCircle = new RectF();
        coloredArc = new RectF();
        topArcRect = new RectF();
        bottomArcRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(90);

        //canvas.drawPath(textPath, textPathPaint);
        if (message != null){
            //左円半径
            chatCircleRedius = convertDp2Px(10, getContext());
            //回転中心位置をずらす
            float radiusPivotOffset = 45;
            //円弧描画最大角度
            float sweepangle = 89.15f;
            //大型円判定
            boolean large = false;

            //メッセージの長さが23文字以上の場合
            if (message.length() > 23){
                //メッセージバー円半径×２
                chatCircleRedius = chatCircleRedius * 2;
                //大型円判定true
                large = true;
            }

            //右円の半径が決定したのでメッセージバーの色の太さを設定
            colorPaint_Stroke.setStrokeWidth(chatCircleRedius *2);

            //回転半径
            float radius = ((lineCount - 1) * (textSize + convertDp2Px(5, getContext()))) + convertDp2Px(200, getContext());
            //大型円の場合
            if (large){
                //さらにずらす
                radius = radius + textSize/2 + convertDp2Px(5, getContext())/2;
            }

            //左円描画用座標
            double leftCircleX = radius -(textSize/2)-radiusPivotOffset;
            double leftCircleY = 0;
            //大型円の場合
            if (large){
                //左円の角度オフセット3°
                float angleOffset_leftCircle = 2;
                //座標更新
                leftCircleX = (cos(Math.toRadians(angleOffset_leftCircle))*(radius -(textSize/2))) - radiusPivotOffset;
                leftCircleY = (sin(Math.toRadians(angleOffset_leftCircle))*(radius -(textSize/2)));
            }

            //円弧描画用座標（上側）
            float topArcLeft = -(radius -(textSize/2)-chatCircleRedius+radiusPivotOffset);
            float topArcTop = -(radius -(textSize/2)-chatCircleRedius);
            float topArcRight = radius -(textSize/2)-chatCircleRedius-radiusPivotOffset;
            float topArcBttom = radius -(textSize/2)-chatCircleRedius;

            //円弧描画用座標（下側）
            float btmArcLeft = -(radius - (textSize / 2) + chatCircleRedius + radiusPivotOffset);
            float btmArcTop = -(radius -(textSize/2)+chatCircleRedius);
            float btmArcRight = radius -(textSize/2)+chatCircleRedius-radiusPivotOffset;
            float btmArcBttom = radius -(textSize/2)+chatCircleRedius;

            //色付き円弧描画用座標
            float coloredArcLeft = topArcLeft-chatCircleRedius;
            float coloredArcTop = topArcTop-chatCircleRedius;
            float coloredArcRight = topArcRight+chatCircleRedius;
            float coloredArcBttom = topArcBttom+chatCircleRedius;

            //影円描画用座標（左側）
            float shadowCircleLeft = radius -radiusPivotOffset-(textSize/2);
            float shadowCircleTop = -chatCircleRedius-convertDp2Px(3, getContext());
            float shadowCircleRight = radius -radiusPivotOffset-(textSize/2)+(chatCircleRedius+chatCircleRedius/2);
            float shadowCircleBttom = chatCircleRedius-convertDp2Px(3, getContext());
            //大型円の場合
            if (large){
                shadowCircleLeft = radius -radiusPivotOffset-(textSize);
                shadowCircleRight = radius -radiusPivotOffset-(textSize)+(chatCircleRedius+chatCircleRedius/2);
            }

            //------以下パス・表示------

            //文字列補助線
            if (large){
                textPaint.setTextSize((textSize*6)/7);
                textPath.addCircle(-radiusPivotOffset, 0, radius -textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                textPathSecond.addCircle(-radiusPivotOffset, 0, radius +textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
            } else {
                textPaint.setTextSize(textSize);
                textPath.addCircle(-radiusPivotOffset, 0, radius, Path.Direction.CCW);    //円形のパスをx-45、y0を中心として描画、反時計回り
            }

            //UI影
            shadowArc.set(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);   //円弧範囲
            shadowPath.addArc(shadowArc, 270, 90); //円弧
            canvas.drawPath(shadowPath, shadowPaint_Stroke);
            shadowCircle.set(shadowCircleLeft, shadowCircleTop, shadowCircleRight, shadowCircleBttom);   //半円範囲
            canvas.drawArc(shadowCircle, 0, 180, false, shadowPaint_Fill);  //円だとかぶるので半円

            //UI下色
            coloredArc.set(coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom);   //円弧範囲
            graphicPath_Colored.addArc(coloredArc, 270, sweepangle); //円弧
            canvas.drawPath(graphicPath_Colored, colorPaint_Stroke);

            //左丸
            graphicPath.addCircle((float) leftCircleX, -((float) leftCircleY), chatCircleRedius, Path.Direction.CW); //左丸
            canvas.drawPath(graphicPath, colorPaint_Fill);
            canvas.drawPath(graphicPath, graphicPain_Line);

            //UI白線
            topArcRect.set(topArcLeft, topArcTop, topArcRight, topArcBttom);   //円弧上側範囲
            graphicPath_Line.addArc(topArcRect, 270, sweepangle); //円弧上側
            bottomArcRect.set(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);  //円弧下側範囲
            graphicPath_Line.addArc(bottomArcRect, 270, sweepangle); //円弧下側
            canvas.drawPath(graphicPath_Line, graphicPain_Line);

            //曲線文字列
            int textAngleOffset = 30;
            String text1;
            String text2;
            if (large){
                textAngleOffset = 65;
                text1 = message.substring(0, 23);
                text2 = message.substring(23);

                canvas.drawTextOnPath(text1, textPath, textAngleOffset, 0, textPaint);
                canvas.drawTextOnPath(text2, textPathSecond, textAngleOffset, 0, textPaint);

                Log.d("NARUKO_", "1: "+text1);
                Log.d("NARUKO_", "2: "+text2);
            }else {
                canvas.drawTextOnPath(message, textPath, textAngleOffset, 0, textPaint);
            }
        }
    }

    public void getMessage(Context context, String message, String userColor){
        //メッセージ取得
        this.message = message;

        //ユーザーカラー取得
        int color = getResources().getColor(getResources().getIdentifier("color"+userColor, "color", context.getPackageName()));
        //左円色指定
        colorPaint_Fill.setColor(color);
        //チャットバー色指定
        colorPaint_Stroke.setColor(color);

        //8行目ではない場合
        if (lineCount < 8){
            //メッセージバーの表示半径１行分ずらす
            lineCount++;
        }

        textHolder.add(message);    //Arrayに文字列を追加

        //描画モード
        drawMode = true;

        //再描画開始
        invalidate();
    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}
