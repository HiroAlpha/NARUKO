package com.hiro_a.naruko.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hiro_a.naruko.R;

public class MenuItem extends RelativeLayout {
    int iconImage;
    String iconText;

    public MenuItem(Context context) {
        this(context, null);
    }

    public MenuItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MenuItem, 0, 0);

        try {
            iconImage = typedArray.getResourceId(R.styleable.MenuItem_iconImage, 0);
            iconText = typedArray.getString(R.styleable.MenuItem_iconText);
        }finally {
            typedArray.recycle();
        }

        setProperty(context);
    }

    private void setProperty(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.item_menu, this, true);

        ImageView iconImageView = (ImageView)findViewById(R.id.iconImageView);
        iconImageView.setImageResource(iconImage);

        TextView iconTextView = (TextView)findViewById(R.id.iconTextView);
        iconTextView.setText(iconText);
    }
}
