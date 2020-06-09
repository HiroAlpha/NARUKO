package com.hiro_a.naruko.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hiro_a.naruko.R;

public class SelectorItem extends LinearLayout {
    int selectorImage;
    String selectorText;
    int selectorTextColor;

    public SelectorItem(Context context) {
        this(context, null);
    }

    public SelectorItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectorItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SelectorItem, 0, 0);

        try {
            selectorImage = typedArray.getResourceId(R.styleable.SelectorItem_selectorImage, 0);
            selectorText = typedArray.getString(R.styleable.SelectorItem_selectorText);
            selectorTextColor = typedArray.getColor(R.styleable.SelectorItem_selectorTextColor, Color.BLACK);
        }finally {
            typedArray.recycle();
        }

        setProperty(context);
    }

    private void setProperty(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.item_login_select, this, true);

        ImageView selectorImageView = (ImageView)findViewById(R.id.itemLoginSelect_imageView);
        selectorImageView.setImageResource(selectorImage);

        TextView selectorTextView = (TextView)findViewById(R.id.itemLoginSelect_textView);
        selectorTextView.setTextColor(selectorTextColor);
        selectorTextView.setText(selectorText);
    }
}
