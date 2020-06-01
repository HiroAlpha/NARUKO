package com.hiro_a.naruko.view.IconRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class IconRecyclerViewHolder extends RecyclerView.ViewHolder{
    final CircleImageView imageView;
    final TextView textView;

    public IconRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (CircleImageView) itemView.findViewById(R.id.menuChatImageView);
        textView = (TextView)itemView.findViewById(R.id.menuChatTextView);
    }
}
