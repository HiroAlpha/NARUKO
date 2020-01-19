package com.hiro_a.naruko.view.RecyclerView.ProfileView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuFriendData;

import java.util.List;

public class LinearLayoutAdapter extends RecyclerView.Adapter<LinearLayoutViewHolder> {
    private List<String> titleList;
    private List<String> dataList;

    public LinearLayoutAdapter(List<String> titleList, List<String> dataList){
        this.titleList = titleList;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public LinearLayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_profile, parent, false);

        final LinearLayoutViewHolder profileHolder = new LinearLayoutViewHolder(view);
        profileHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = profileHolder.getAdapterPosition();
                onMenuClicked(position);
            }
        });

        return profileHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LinearLayoutViewHolder holder, int position) {
        holder.profileNameView.setText(titleList.get(position));
        holder.profileDetailView.setText(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

    protected void onMenuClicked(@NonNull int position){

    }
}
