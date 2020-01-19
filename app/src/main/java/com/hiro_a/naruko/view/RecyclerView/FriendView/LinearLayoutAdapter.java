package com.hiro_a.naruko.view.RecyclerView.FriendView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuFriendData;
import com.hiro_a.naruko.task.DownloadImageTask;

import java.util.List;

public class LinearLayoutAdapter extends RecyclerView.Adapter<LinearLayoutViewHolder> {
    private List<MenuFriendData> list;

    public LinearLayoutAdapter(List<MenuFriendData> list){
        this.list = list;
    }

    @NonNull
    @Override
    public LinearLayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        final LinearLayoutViewHolder friendHolder = new LinearLayoutViewHolder(view);
        friendHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = friendHolder.getAdapterPosition();
                onMenuClicked(position);
            }
        });

        return friendHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LinearLayoutViewHolder holder, int position) {
        String imageUrl = list.get(position).getFriendImage();
        if (imageUrl.equals("noImage")){
            holder.friendImageView.setImageResource(R.drawable.ic_person_black_24dp);
        }else {
            new DownloadImageTask(holder.friendImageView).execute(imageUrl);
        }
        holder.friendNameView.setText(list.get(position).getFriendName());
        holder.friendIdView.setText(list.get(position).getFriendId());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected void onMenuClicked(@NonNull int position){

    }
}
