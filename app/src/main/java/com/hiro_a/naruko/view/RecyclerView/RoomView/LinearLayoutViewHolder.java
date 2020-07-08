package com.hiro_a.naruko.view.RecyclerView.RoomView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class LinearLayoutViewHolder extends RecyclerView.ViewHolder {
    final CircleImageView imageView;
    final TextView topDetail;
    final TextView bottomDetail;
    final ImageView optionButton;

    public LinearLayoutViewHolder(@NonNull View itemView){
        super(itemView);

        imageView = (CircleImageView)itemView.findViewById(R.id.itemRoomSearch_imageView_roomImage);
        topDetail = (TextView)itemView.findViewById(R.id.itemRoomSearch_textView_topDetail);
        bottomDetail = (TextView)itemView.findViewById(R.id.itemRoomSearch_textView_bottomDetail);
        optionButton = (ImageView)itemView.findViewById(R.id.itemRoomSearch_imageView_option);
    }
}
