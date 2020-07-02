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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;

public class NarukoView_OldMessage extends View {
    private int lineCount = 0;
    String userColor;
    private float textSize = convertDp2Px(15, getContext());  //文字サイズ
    private ArrayList<String> textHolder = new ArrayList<String>(); //過去の文字列格納用Array

    private Paint textPaint;
    private Paint pathPaint;
    private Paint graphicPaint_Line;
    private Paint graphicPaint_Colored, graphicPaint_Colored_FILL;
    private Paint shadowPaint, shadowPaint_FILL;

    public NarukoView_OldMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "anzu_font.ttf"); //フォント

        //文字列設定
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(typeface);

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

        //影
        //UI下色影円設定
        shadowPaint_FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint_FILL.setStyle(Paint.Style.FILL);
        shadowPaint_FILL.setColor(Color.argb(128, 100, 100, 100));
        //UI下色影Path設定
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setColor(Color.argb(128, 100, 100, 100));

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
            //チャットバー円半径倍率
            int multiplier = 0;
            //表示する文字列の最初の番号
            int textSpan = 0;

            //8行目以降の場合
            if (lineCount >= 8){
                //表示を開始する配列番号の指定
                textSpan = textHolder.size() - 8;
                Log.w("NARUKO_", "8行以上なので" + textHolder.size() + "- 8 = " + textSpan);

                //新しいメッセージが23文字以上の場合
                if (textHolder.get(textHolder.size()-1).length() > 23){
                    //表示行数を１行減らす
                    textSpan++;
                    Log.w("NARUKO_", textHolder.get(textHolder.size()-1) + "により配列番号+1");
                }
            }

            //表示予定メッセージ配列の中に23文字以上がある場合
            for (int i=textSpan; i<textHolder.size()-1; i++){
                if (textHolder.get(i).length() > 23){
                    //表示行数を１行減らす
                    textSpan++;
                    Log.w("NARUKO_", textHolder.get(i) + "により配列番号+1");
                }
            }
            Log.w("NARUKO_", "最終開始配列番号: " + textSpan);

            //入力されたものより1つ前の文字列から最も古いものまで
            for (int i=textSpan; i<textHolder.size()-1; i++){

                //メッセージバー円半径
                //左右円半径
                float chatCircleRedius = convertDp2Px(10, getContext());
                //回転中心位置をずらす
                float radiusPivotOffset = 45;
                //左円の角度オフセット
                float angleOffset_leftCircle = 0;
                //円弧の描画最大角度
                float sweepangle = 89.15f;
                //大型円判定
                boolean large = false;

                 //2行目以降の場合
                if (i != textSpan){
                    //前のメッセージの長さが23文字以上の場合
                    if (textHolder.get(i-1).length() > 23){

                        //TextSize分メッセージバーの表示半径１行分ずらす
                        multiplier++;
                    }
                }

                //メッセージの長さが23文字以上の場合
                if (textHolder.get(i).length() > 23){
                    //メッセージバー円半径×２
                    chatCircleRedius = chatCircleRedius * 2;

                    angleOffset_leftCircle = 2;
                    large = true;
                }

                //右円の半径が決定したのでメッセージバーの色の太さを設定
                graphicPaint_Colored.setStrokeWidth(chatCircleRedius *2);

                //右円の半径が決定したのでメッセージバーの影の太さを設定
                shadowPaint.setStrokeWidth(chatCircleRedius);

                //回転半径
                float radius = (multiplier * (textSize + convertDp2Px(5, getContext()))) + convertDp2Px(200, getContext());
                //大型円の場合
                if (large){
                    //さらにずらす
                    radius = radius + textSize/2 + convertDp2Px(5, getContext())/2;
                }

                //文字列補助線
                Path textPath = new Path();
                Path textPathSecond = new Path();
                if (large){
                    textPaint.setTextSize((textSize*6)/7);
                    textPath.addCircle(-radiusPivotOffset, 0, radius -textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                    textPathSecond.addCircle(-radiusPivotOffset, 0, radius +textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                } else {
                    textPaint.setTextSize(textSize);
                    textPath.addCircle(-radiusPivotOffset, 0, radius, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                }
                //canvas.drawPath(textPath, pathPaint);

                //左白丸描画用座標
                double leftCircleSin = (sin(Math.toRadians(angleOffset_leftCircle)) * (radius - (textSize / 2)));
                double leftCircleCos = (cos(Math.toRadians(angleOffset_leftCircle)) * (radius - (textSize / 2))) - radiusPivotOffset;
                if (large){
                    leftCircleSin = (sin(Math.toRadians(angleOffset_leftCircle))*(radius -(textSize/2)));
                    leftCircleCos = (cos(Math.toRadians(angleOffset_leftCircle))*(radius -(textSize/2))) - radiusPivotOffset;
                }
                //右白丸描画用座標
                double rightCircleSin = (sin(Math.toRadians(90)) * (radius - (textSize / 2)));
                double rightCircleCos = (cos(Math.toRadians(90)) * (radius - (textSize / 2))) - radiusPivotOffset;
                //円弧描画用座標（上側）
                float topArcLeft = -(radius - (textSize / 2) - chatCircleRedius + radiusPivotOffset);
                float topArcTop = -(radius - (textSize / 2) - chatCircleRedius);
                float topArcRight = radius - (textSize / 2) - chatCircleRedius - radiusPivotOffset;
                float topArcBttom = radius - (textSize / 2) - chatCircleRedius;
                //円弧描画用座標（下側）
                float btmArcLeft = -(radius - (textSize / 2) + chatCircleRedius + radiusPivotOffset);
                float btmArcTop = -(radius - (textSize / 2) + chatCircleRedius);
                float btmArcRight = radius - (textSize / 2) + chatCircleRedius - radiusPivotOffset;
                float btmArcBttom = radius - (textSize / 2) + chatCircleRedius;
                //色付き円弧描画用座標
                float coloredArcLeft = topArcLeft - chatCircleRedius;
                float coloredArcTop = topArcTop - chatCircleRedius;
                float coloredArcRight = topArcRight + chatCircleRedius;
                float coloredArcBttom = topArcBttom + chatCircleRedius;
                //影円描画用座標（左側）
                float shadowCircleLeft = radius - radiusPivotOffset -(textSize/2);
                float shadowCircleTop = -chatCircleRedius -convertDp2Px(3, getContext());
                float shadowCircleRight = radius - radiusPivotOffset -(textSize/2)+(chatCircleRedius + chatCircleRedius /2);
                float shadowCircleBttom = chatCircleRedius -convertDp2Px(3, getContext());

                //UI影
                Path shadowPath = new Path();
                RectF shadowArc = new RectF(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);   //円弧範囲
                shadowPath.addArc(shadowArc, 270, sweepangle); //円弧
                canvas.drawPath(shadowPath, shadowPaint);
                RectF shadowCircle = new RectF(shadowCircleLeft, shadowCircleTop, shadowCircleRight, shadowCircleBttom);   //半円範囲
                //canvas.drawArc(shadowCircle, 0, 180, false, shadowPaint_FILL);  //円だとかぶるので半円

                //UI下色
                Path graphicPath_Colored = new Path();
                RectF coloredArc = new RectF(coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom);   //円弧範囲
                graphicPath_Colored.addArc(coloredArc, 270, sweepangle); //円弧
                canvas.drawPath(graphicPath_Colored, graphicPaint_Colored);

                //共通項
                Path graphicPath = new Path();
                graphicPath.addCircle((float) leftCircleCos, -((float) leftCircleSin), chatCircleRedius, Path.Direction.CW); //左丸
                graphicPath.addCircle((float) rightCircleCos, -((float) rightCircleSin), chatCircleRedius, Path.Direction.CW); //右丸
                canvas.drawPath(graphicPath, graphicPaint_Colored_FILL);
                canvas.drawPath(graphicPath, graphicPaint_Line);

                //UI白線
                Path graphicPath_Line = new Path();
                RectF topArcRect = new RectF(topArcLeft, topArcTop, topArcRight, topArcBttom);   //円弧上側範囲
                graphicPath_Line.addArc(topArcRect, 270, sweepangle); //円弧上側
                RectF bottomArcRect = new RectF(btmArcLeft, btmArcTop, btmArcRight, btmArcBttom);  //円弧下側範囲
                graphicPath_Line.addArc(bottomArcRect, 270, sweepangle); //円弧下側
                canvas.drawPath(graphicPath_Line, graphicPaint_Line);

                //曲線文字列
                int textAngleOffset = 30;
                String text1;
                String text2;
                if (large){
                    textAngleOffset = 65;
                    text1 = textHolder.get(i).substring(0, 23);
                    text2 = textHolder.get(i).substring(23);

                    canvas.drawTextOnPath(text1, textPath, textAngleOffset, 0, textPaint);
                    canvas.drawTextOnPath(text2, textPathSecond, textAngleOffset, 0, textPaint);

                    Log.d("NARUKO_", "1: "+text1);
                    Log.d("NARUKO_", "2: "+text2);
                }else {
                    canvas.drawTextOnPath(textHolder.get(i), textPath, textAngleOffset, 0, textPaint);
                }


                multiplier++;
            }
        }
    }

    public void getMessage(Context context, String messageText, String userColor){
        //ユーザーカラー取得
        int color = getResources().getColor(getResources().getIdentifier("color"+userColor, "color", context.getPackageName()));
        //色を指定
        graphicPaint_Colored_FILL.setColor(color);
        graphicPaint_Colored.setColor(color);

        this.userColor = userColor;

        textHolder.add(messageText);    //Arrayに文字列を追加

        if (lineCount < 8){
            lineCount++;
        }

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
