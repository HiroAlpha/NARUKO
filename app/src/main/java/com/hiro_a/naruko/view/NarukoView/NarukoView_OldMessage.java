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
    float textSize = convertDp2Px(15, getContext());  //文字サイズ
    float radius;   //回転半径
    float radiusPivotOffset;
    float sweepangle = 89.15f;
    float chatCircleRedius; //UI白丸半径
    String text = "";
    ArrayList<String> textHolder = new ArrayList<String>(); //過去の文字列格納用Array
    float btmArcLeft, btmArcTop, btmArcRight, btmArcBttom;
    float topArcLeft, topArcTop, topArcRight, topArcBttom;
    double rightCircleSin, rightCircleCos;
    double leftCircleSin, leftCircleCos;
    float coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom;
    float shadowCircleLeft, shadowCircleTop, shadowCircleRight, shadowCircleBttom;

    Paint textPaint;
    Paint pathPaint;
    Paint graphicPaint_Line;
    Paint graphicPaint_Colored, graphicPaint_Colored_FILL;
    Paint shadowPaint, shadowPaint_FILL;

    Path textPath;
    Path textPathSecond;
    Path graphicPath;
    Path graphicPath_Line;
    Path graphicPath_Colored;
    Path shadowPath;

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
            int multiplier = 0;
            //表示する文字列の最初の番号
            int textSpan = 0;
            if (textHolder.size() >= 9){
                textSpan = textHolder.size() - 8;
            }

            //入力されたものより1つ前の文字列から最も古いものまで
            for (int i=0; i<textHolder.size()-1; i++){
                if (i == 0){
                    graphicPaint_Colored_FILL.setColor(Color.rgb(255,192,203));
                    graphicPaint_Colored.setColor(Color.rgb(255,192,203));
                }
                if (i == 1){
                    graphicPaint_Colored_FILL.setColor(Color.rgb(244,115,106));
                    graphicPaint_Colored.setColor(Color.rgb(244,115,106));
                }
                if (i == 2){
                    graphicPaint_Colored_FILL.setColor(Color.rgb(255,192,203));
                    graphicPaint_Colored.setColor(Color.rgb(255,192,203));
                }
                if (i == 3){
                    graphicPaint_Colored_FILL.setColor(Color.rgb(243,228,138));
                    graphicPaint_Colored.setColor(Color.rgb(243,228,138));
                }
                if (i == 4){
                    graphicPaint_Colored_FILL.setColor(Color.rgb(255,192,203));
                    graphicPaint_Colored.setColor(Color.rgb(255,192,203));
                }


                //メッセージバー円半径
                 chatCircleRedius = convertDp2Px(10, getContext());
                 radiusPivotOffset = 45;
                 float angle_leftCircle = 0;
                 boolean large = false;
                 //2行目以降の場合
                if (i != 0){
                    //前のメッセージの長さが23文字以上の場合
                    if (textHolder.get(i-1).length() > 23){
                        multiplier++;
                    }
                }

                //メッセージの長さが23文字以上の場合
                if (textHolder.get(i).length() > 23){
                    //メッセージバー円半径×２
                    chatCircleRedius = chatCircleRedius * 2;

                    angle_leftCircle = 2;
                    large = true;
                }

                //右円の半径が決定したのでメッセージバーの色の太さを設定
                graphicPaint_Colored.setStrokeWidth(chatCircleRedius*2);

                //右円の半径が決定したのでメッセージバーの影の太さを設定
                shadowPaint.setStrokeWidth(chatCircleRedius);

                //TextSize分メッセージバーの表示半径をずらす
                radius = (multiplier * (textSize + convertDp2Px(5, getContext()))) + convertDp2Px(200, getContext());
                //大型円の場合
                if (large){
                    //さらにずらす
                    radius = radius + textSize/2 + convertDp2Px(5, getContext())/2;
                }

                //文字列補助線
                textPath = new Path();
                textPathSecond = new Path();
                if (large){
                    textPaint.setTextSize((textSize*6)/7);
                    textPath.addCircle(-radiusPivotOffset, 0, radius-textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                    textPathSecond.addCircle(-radiusPivotOffset, 0, radius+textSize/2, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                } else {
                    textPaint.setTextSize(textSize);
                    textPath.addCircle(-radiusPivotOffset, 0, radius, Path.Direction.CCW);    //円形のパスをx-400、y0を中心として描画、反時計回り
                }
                //canvas.drawPath(textPath, pathPaint);

                //左白丸描画用座標
                leftCircleSin = (sin(Math.toRadians(angle_leftCircle))*(radius-(textSize/2)));
                leftCircleCos = (cos(Math.toRadians(angle_leftCircle))*(radius-(textSize/2))) - radiusPivotOffset;
                //右白丸描画用座標
                rightCircleSin = (sin(Math.toRadians(90))*(radius-(textSize/2)));
                rightCircleCos = (cos(Math.toRadians(90))*(radius-(textSize/2))) - radiusPivotOffset;
                //円弧描画用座標（上側）
                topArcLeft = -(radius-(textSize/2)-chatCircleRedius+radiusPivotOffset);
                topArcTop = -(radius-(textSize/2)-chatCircleRedius);
                topArcRight = radius-(textSize/2)-chatCircleRedius-radiusPivotOffset;
                topArcBttom = radius-(textSize/2)-chatCircleRedius;
                if (large){
                    leftCircleSin = (sin(Math.toRadians(angle_leftCircle))*(radius-(textSize/2)-1.5));
                    leftCircleCos = (cos(Math.toRadians(angle_leftCircle))*(radius-(textSize/2)-1.5)) - radiusPivotOffset;
                }
                //円弧描画用座標（下側）
                btmArcLeft = -(radius-(textSize/2)+chatCircleRedius+radiusPivotOffset);
                btmArcTop = -(radius-(textSize/2)+chatCircleRedius);
                btmArcRight = radius-(textSize/2)+chatCircleRedius-radiusPivotOffset;
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
                //canvas.drawArc(shadowCircle, 0, 180, false, shadowPaint_FILL);  //円だとかぶるので半円

                //UI下色
                RectF coloredArc = new RectF(coloredArcLeft, coloredArcTop, coloredArcRight, coloredArcBttom);   //円弧範囲
                graphicPath_Colored.addArc(coloredArc, 270, 90); //円弧
                canvas.drawPath(graphicPath_Colored, graphicPaint_Colored);

                //共通項
                graphicPath.addCircle((float) leftCircleCos, -((float)leftCircleSin), chatCircleRedius, Path.Direction.CW); //左丸
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
