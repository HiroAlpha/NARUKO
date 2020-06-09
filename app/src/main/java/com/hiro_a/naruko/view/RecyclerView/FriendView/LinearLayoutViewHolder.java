package com.hiro_a.naruko.view.RecyclerView.FriendView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class LinearLayoutViewHolder extends RecyclerView.ViewHolder {
    final CircleImageView friendImageView;
    final TextView friendNameView;
    final TextView friendIdView;

    public LinearLayoutViewHolder(@NonNull View itemView){
        super(itemView);

        friendImageView = (CircleImageView) itemView.findViewById(R.id.itemFriend_imageView_friendImage);
        friendNameView = (TextView)itemView.findViewById(R.id.itemFriend_textView_friendName);
        friendIdView = (TextView)itemView.findViewById(R.id.itemFriend_textView_friendId);
    }
}
