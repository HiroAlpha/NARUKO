package com.hiro_a.naruko.view.MenuRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.MenuChatData;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {
    private List<MenuChatData> list;

    public MenuAdapter(List<MenuChatData> list){
        this.list = list;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);

        final MenuViewHolder menuHolder = new MenuViewHolder(view);
        menuHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = menuHolder.getAdapterPosition();
                final String title = (list.get(position)).getString();
                onMenuClicked(title);
            }
        });
        return menuHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.imageView.setImageResource(list.get(position).getInt());
        holder.textView.setText(list.get(position).getString());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected void onMenuClicked(@NonNull String title){

    }
}
