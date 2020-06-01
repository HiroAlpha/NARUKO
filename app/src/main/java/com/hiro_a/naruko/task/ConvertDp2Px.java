package com.hiro_a.naruko.task;

import android.content.Context;
import android.util.DisplayMetrics;

//dp => px
public class ConvertDp2Px {

    public float convert(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }
}
