package com.hiro_a.naruko.view.IconRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.mikhaellopez.circularimageview.CircularImageView;


public class IconRecyclerViewHolder extends RecyclerView.ViewHolder{
    final CircularImageView imageView;
    final TextView textView;

    public IconRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (CircularImageView) itemView.findViewById(R.id.menuChatImageView);
        textView = (TextView)itemView.findViewById(R.id.menuChatTextView);
    }
}
