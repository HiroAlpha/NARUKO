package com.hiro_a.naruko.task;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class AccordionAnimation extends Animation {
    private int heightAdd;
    private int heightOrg;

    private View view;

    public AccordionAnimation(View view, int heightAdd, int heightOrg){
        this.view = view;
        this.heightAdd = heightAdd;
        this.heightOrg = heightOrg;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        int heightNew = (int) (heightOrg + heightAdd * interpolatedTime);
        view.getLayoutParams().height = heightNew;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
