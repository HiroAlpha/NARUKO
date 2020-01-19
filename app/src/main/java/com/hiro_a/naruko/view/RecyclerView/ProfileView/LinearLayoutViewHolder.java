package com.hiro_a.naruko.view.RecyclerView.ProfileView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;

public class LinearLayoutViewHolder extends RecyclerView.ViewHolder {
    final TextView profileNameView;
    final TextView profileDetailView;

    public LinearLayoutViewHolder(@NonNull View itemView){
        super(itemView);

        profileNameView = (TextView)itemView.findViewById(R.id.settingTitle);
        profileDetailView = (TextView)itemView.findViewById(R.id.settingContent);
    }
}
