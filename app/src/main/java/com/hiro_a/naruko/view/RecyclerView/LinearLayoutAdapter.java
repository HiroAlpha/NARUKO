package com.hiro_a.naruko.view.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuFriendData;

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
        holder.friendImageView.setImageResource(list.get(position).getFriendImage());
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
