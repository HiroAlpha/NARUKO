package com.hiro_a.naruko.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityChat;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserIconView extends RelativeLayout {
    Context mContext;

    private int preDx, preDy;

    View layout;
    CircleImageView circleImageView;
    TextView textView;

    public UserIconView(@NonNull Context context) {
        this(context, null);
    }

    public UserIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        layout= inflater.inflate(R.layout.item_user, this, true);
        circleImageView = (CircleImageView) findViewById(R.id.item_user_imageview);
        textView = (TextView) findViewById(R.id.item_user_textview);
    }

    public void setData(StorageReference imageStorgeRefarence, String userName, int color){
        //画像
        setImage(imageStorgeRefarence);
        circleImageView.setBorderColor(color);

        //ユーザー名
        textView.setText(userName);

        invalidate();
    }

    private void setImage(StorageReference imageStorgeRefarence){
        if (imageStorgeRefarence == null){
            circleImageView.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Glide.with(circleImageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(imageStorgeRefarence)
                    .into(circleImageView);
        }
    }
}
