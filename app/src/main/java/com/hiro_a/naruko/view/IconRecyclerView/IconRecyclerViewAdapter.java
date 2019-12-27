package com.hiro_a.naruko.view.IconRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuRoomData;

import java.util.List;

public class IconRecyclerViewAdapter extends RecyclerView.Adapter<IconRecyclerViewHolder>{
    private List<MenuRoomData> list;

    public IconRecyclerViewAdapter(List<MenuRoomData> list){
        this.list = list;
    }

    @NonNull
    @Override
    public IconRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);

        final IconRecyclerViewHolder menuHolder = new IconRecyclerViewHolder(view);
        menuHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = menuHolder.getAdapterPosition();
                final String title = (list.get(position)).getTitle();
                onMenuClicked(position);
            }
        });
        return menuHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull IconRecyclerViewHolder holder, int position) {
        holder.imageView.setImageResource(list.get(position).getInt());
        holder.textView.setText(list.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected void onMenuClicked(@NonNull int position){

    }
}
