package com.hiro_a.naruko.view.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;

public class LinearLayoutViewHolder extends RecyclerView.ViewHolder {
    final ImageView friendImageView;
    final TextView friendNameView;
    final TextView friendIdView;

    public LinearLayoutViewHolder(@NonNull View itemView){
        super(itemView);

        friendImageView = (ImageView)itemView.findViewById(R.id.friendImage);
        friendNameView = (TextView)itemView.findViewById(R.id.friendName);
        friendIdView = (TextView)itemView.findViewById(R.id.friendId);
    }
}
