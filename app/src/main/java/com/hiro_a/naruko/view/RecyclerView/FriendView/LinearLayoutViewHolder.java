package com.hiro_a.naruko.view.RecyclerView.FriendView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.mikhaellopez.circularimageview.CircularImageView;

public class LinearLayoutViewHolder extends RecyclerView.ViewHolder {
    final CircularImageView friendImageView;
    final TextView friendNameView;
    final TextView friendIdView;

    public LinearLayoutViewHolder(@NonNull View itemView){
        super(itemView);

        friendImageView = (CircularImageView) itemView.findViewById(R.id.friendImage);
        friendNameView = (TextView)itemView.findViewById(R.id.friendName);
        friendIdView = (TextView)itemView.findViewById(R.id.friendId);
    }
}
