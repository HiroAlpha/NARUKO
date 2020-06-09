package com.hiro_a.naruko.view.NarukoView;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityChat;
import com.hiro_a.naruko.task.ConvertDp2Px;
import com.hiro_a.naruko.view.UserIconView;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class NarukoView_UserIconPopup extends View implements Animator.AnimatorListener, View.OnTouchListener {
    Context context;
    String TAG = "NARUKO_DEBUG @ NarukoUserIconPopoutView";

    private int lastSpeakerNum;
    private int preDx, preDy;
    boolean whiteline = false;

    ArrayList<UserIconView> userIconViewArrayList;

    NarukoView_UserIconLine canvasViewUserIconLine;

    public NarukoView_UserIconPopup(Context context) {
        this(context, null);
    }

    public NarukoView_UserIconPopup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NarukoView_UserIconPopup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        userIconViewArrayList = new ArrayList<UserIconView>();
    }

    public void addUserIcon(Point screenSize, RelativeLayout relativeLayout, String userName, String userImage){
        Point screenCenter = new Point(screenSize.x /2, screenSize.y/2);

        UserIconView userIconView = new UserIconView(context);
        userIconViewArrayList.add(userIconView);

        int userIconCount = userIconViewArrayList.size();
        int userIconArrayNumber = userIconCount - 1;
        int color = getResources().getColor(R.color.colorYunaLight);
        StorageReference imageStorgeRefarence = null;
        if (!userImage.equals("Default_Image")){
            imageStorgeRefarence = FirebaseStorage.getInstance().getReference().child(userImage);
        }

        UserIconView foregroundUserIcon = userIconViewArrayList.get(userIconArrayNumber);
        foregroundUserIcon.setData(imageStorgeRefarence, userName, color);
        foregroundUserIcon.setOnTouchListener(this);
        relativeLayout.addView(foregroundUserIcon);

        int iconSize = (int) convertDp2Px(100, context);
        int userIconRow = (userIconArrayNumber / 3) + 1;
        int userIconColumn = userIconCount - (3 * (userIconRow-1));

        if (userIconRow == 1){
            if (userIconColumn==2){
                userIconColumn = 3;
            }
            else if (userIconColumn==3){
                userIconColumn = 2;
            }
        }

        float iconX = (screenSize.x / 6) * (userIconColumn + (userIconColumn - 1));
        float iconY = screenCenter.y + (iconSize - (iconSize * 0.6f * (userIconColumn-1))) + ((iconSize * (userIconRow-1))+iconSize/4);

        foregroundUserIcon.setX(iconX - (iconSize/2));
        foregroundUserIcon.setY(iconY - (iconSize/2));

        popupAnimation(relativeLayout, foregroundUserIcon, iconX, iconY);
    }

    public void removeUserIcon(RelativeLayout relativeLayout){
        int userIconNumber = userIconViewArrayList.size();

        for (int i=0;i<userIconNumber;i++){
            relativeLayout.removeView(userIconViewArrayList.get(i));
        }

        userIconViewArrayList.clear();
    }

    //define the last speaker
    public void setLastSpeaker(NarukoView_UserIconLine chatCanvasViewUserIconLine, int iconNum){
        UserIconView selectedUserIcon = userIconViewArrayList.get(iconNum);
        float selectedIconX = selectedUserIcon.getX();
        float selectedIconY = selectedUserIcon.getY();

        Point selectedIconGrid = new Point((int) selectedIconX, (int) selectedIconY);
        chatCanvasViewUserIconLine.getUserGrid(selectedIconGrid);

        lastSpeakerNum = iconNum;
        canvasViewUserIconLine = chatCanvasViewUserIconLine;
        whiteline = true;
    }

    //move line as drag
    private void moveWhiteline(int itemNum, float dx, float dy){
        Point movingGrid = new Point((int) dx, (int) dy);

        if (whiteline && itemNum == lastSpeakerNum){
            canvasViewUserIconLine.getUserGridMoving(movingGrid);
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int touchedItemNum = userIconViewArrayList.indexOf(view);

        // x,y 位置取得
        int newDx = (int)event.getRawX();
        int newDy = (int)event.getRawY();

        switch (event.getAction()) {
            // タッチダウンでdragされた
            case MotionEvent.ACTION_MOVE:
                ActivityChat activityChat = new ActivityChat();

                float dx = view.getX();
                float dy = view.getY();
                int left = view.getLeft() + (newDx - preDx);
                int top = view.getTop() + (newDy - preDy);
                int imgW = left + view.getWidth();
                int imgH = top + view.getHeight();

                // 画像の位置を設定する
                view.layout(left, top, imgW, imgH);
                moveWhiteline(touchedItemNum, dx, dy);
                break;

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_UP:
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(view.getLeft(), view.getTop(), 0, 0);
                view.setLayoutParams(layoutParams);
                break;

            default:
                break;
        }

        // タッチした位置を古い位置とする
        preDx = newDx;
        preDy = newDy;

        return true;
    }

    //↓以下アニメーション
    private void popupAnimation(RelativeLayout relativeLayout, UserIconView userIcon, float iconX, float iconY){
        AnimatorSet animatorParts[] = new AnimatorSet[21];

        PropertyValuesHolder iconScaleX;
        PropertyValuesHolder iconScaleY;
        ObjectAnimator iconScaleAnimator;
        ArrayList<Animator> iconScaleAnimatorArrayList = new ArrayList<Animator>();

        //IconScaleAnimator
        iconScaleX = PropertyValuesHolder.ofFloat("ScaleX", 0.25f, 1f);
        iconScaleY = PropertyValuesHolder.ofFloat("ScaleY", 0.25f, 1f);
        iconScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(userIcon, iconScaleX, iconScaleY);
        iconScaleAnimatorArrayList.add(iconScaleAnimator);

        iconScaleX = PropertyValuesHolder.ofFloat("ScaleX", 1f, 0.85f);
        iconScaleY = PropertyValuesHolder.ofFloat("ScaleY", 1f, 0.85f);
        iconScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(userIcon, iconScaleX, iconScaleY);
        iconScaleAnimatorArrayList.add(iconScaleAnimator);

        iconScaleX = PropertyValuesHolder.ofFloat("ScaleX", 0.85f, 1f);
        iconScaleY = PropertyValuesHolder.ofFloat("ScaleY", 0.85f, 1f);
        iconScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(userIcon, iconScaleX, iconScaleY);
        iconScaleAnimatorArrayList.add(iconScaleAnimator);


        animatorParts[0] = new AnimatorSet();
        animatorParts[0].setDuration(90);
        animatorParts[0].playSequentially(iconScaleAnimatorArrayList);


        //SplashAnimation
        float imageSize = convertDp2Px(16, context);
        Point imageInitialPos = new Point((int)(iconX - (imageSize/2)), (int)(iconY - imageSize));

        ImageView splash[] = new ImageView[20];
        ObjectAnimator splashTransitionAnimator[] = new ObjectAnimator[20];
        ObjectAnimator splashScaleAnimator[] = new ObjectAnimator[20];

        for (int i=0;i<20;i++){
            splash[i] = new ImageView(context);
            splash[i].setImageResource(R.drawable.image_naruko_icon_popupitem);
            splash[i].setX(imageInitialPos.x);
            splash[i].setY(imageInitialPos.y);
            relativeLayout.addView(splash[i]);

            //SplashTransitionAnimator
            Random random = new Random();
            //float animationDistance = convertDp2Px(random.nextInt(55)+15, context);
            float animationDistance = convertDp2Px(random.nextInt(55)+10, context);
            if (i%2==0){
                animationDistance = convertDp2Px(random.nextInt(30)+35, context);
            }
            float animationDegree = 18 * i;

            float imageDestinationPosX = imageInitialPos.x - (float) (cos((Math.PI/180) * animationDegree) * animationDistance);
            float imageDestinationPosY = imageInitialPos.y - (float) (sin((Math.PI/180) * animationDegree) * animationDistance);

            PropertyValuesHolder splashTranslationX = PropertyValuesHolder.ofFloat("translationX", imageInitialPos.x, imageDestinationPosX);
            PropertyValuesHolder splashTranslationY = PropertyValuesHolder.ofFloat("translationY", imageInitialPos.y, imageDestinationPosY);
            splashTransitionAnimator[i] = ObjectAnimator.ofPropertyValuesHolder(splash[i], splashTranslationX, splashTranslationY);
            splashTransitionAnimator[i].addListener(this);

            //SplashScaleAnimator
            PropertyValuesHolder splashScaleX = PropertyValuesHolder.ofFloat("ScaleX", 0.2f);
            PropertyValuesHolder splashScaleY = PropertyValuesHolder.ofFloat("ScaleY", 0.2f);
            splashScaleAnimator[i] = ObjectAnimator.ofPropertyValuesHolder(splash[i], splashScaleX, splashScaleY);
            splashScaleAnimator[i].setInterpolator(new AccelerateInterpolator(1.2f));

            animatorParts[i+1] = new AnimatorSet();
            animatorParts[i+1].setDuration(random.nextInt(300) + 200);
            animatorParts[i+1].playTogether(splashTransitionAnimator[i], splashScaleAnimator[i]);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorParts);
        animatorSet.start();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        View view = (View)((ObjectAnimator) animation).getTarget();
        view.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    //dp→px変換
    public static float convertDp2Px(float dp, Context context){
        ConvertDp2Px convertDp2Px = new ConvertDp2Px();

        return convertDp2Px.convert(dp, context);
    }
}
