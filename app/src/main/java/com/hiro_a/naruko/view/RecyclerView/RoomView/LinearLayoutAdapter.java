package com.hiro_a.naruko.view.RecyclerView.RoomView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.common.MenuRoomData;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LinearLayoutAdapter extends RecyclerView.Adapter<LinearLayoutViewHolder> {
    private String TAG = "NARUKO_DEBUG @ menuRoomSearch.fragment.LinearlayoutAdapter";
    private Context context;

    private String filter;
    private List<MenuRoomData> dataList;

    public LinearLayoutAdapter(Context context, List<MenuRoomData> dataList, String filter){
        this.context = context;
        this.filter = filter;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public LinearLayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_search, parent, false);

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
    public void onBindViewHolder(@NonNull final LinearLayoutViewHolder holder, final int pos) {
        final int position = pos;

        //画像
        if (dataList.get(position).getImage() == null){
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Glide.with(holder.imageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(dataList.get(position).getImage())
                    .into(holder.imageView);
        }

        //詳細上側
        holder.topDetail.setText(dataList.get(position).getTitle());

        //詳細下側
        SimpleDateFormat original_sd = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        SimpleDateFormat new_sd = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.JAPAN);
        try{
            Date date = original_sd.parse(dataList.get(position).getCreatedTime());
            String time = new_sd.format(date);
            holder.bottomDetail.setText(time);
        }catch (Exception e){
            Log.w(TAG, "ERROR: CONVERTING DATE", e);
            Log.w(TAG, "---------------------------------");
        }

        //オプションメニュー
        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ポップアップメニュー
                PopupMenu popup = new PopupMenu(context, holder.optionButton);
                popup.inflate(R.menu.fragment_menu_room_search_option);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.fRoomSearch_item_fav:
                                //SharedPreferences
                                SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = userData.edit();

                                //お気に入り取得
                                ArrayList<String> favRooms = new DeviceInfo().getUserFavRooms(context);

                                //ルームが5以下の場合は追加可
                                if (favRooms.size() < 6){
                                    //追加
                                    favRooms.add(dataList.get(position).getId());

                                    //JSON配列に変換
                                    JSONArray jsonArray = new JSONArray();
                                    for (int i=0;i<favRooms.size();i++){
                                        jsonArray.put(favRooms.get(i));
                                    }

                                    //配列が空でない場合
                                    if (!favRooms.isEmpty()){
                                        //Sharedprefarencesに追加
                                        editor.putString("UserFavRooms", jsonArray.toString());
                                        editor.apply();

                                        //完了トースト
                                        Toast.makeText(context, "NARUKO画面に追加されました", Toast.LENGTH_SHORT).show();
                                    }

                                }else {
                                    //警告トースト
                                    Toast.makeText(context, "NARUKO画面には7以上のルームは追加できません", Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            case R.id.fRoomSearch_item_copy:
                                //クリップボードマネージャー
                                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (clipboardManager != null){
                                    //IDをクリップボードにコピー
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", dataList.get(position).getId()));

                                    //完了トースト
                                    Toast.makeText(context, "ルームIDがクリップボードにコピーされました", Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //ポップアップ表示
                popup.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    protected void onMenuClicked(@NonNull int position){

    }
}
