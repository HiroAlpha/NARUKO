package com.hiro_a.naruko.view.MenuRecyclerView;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.mikhaellopez.circularimageview.CircularImageView;

public class MenuViewHolder extends RecyclerView.ViewHolder {
    final CircularImageView imageView;
    final TextView textView;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (CircularImageView) itemView.findViewById(R.id.menuChatImageView);
        textView = (TextView)itemView.findViewById(R.id.menuChatTextView);
    }
}
