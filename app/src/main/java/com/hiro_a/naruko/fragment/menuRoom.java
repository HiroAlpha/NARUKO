package com.hiro_a.naruko.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityChat;
import com.hiro_a.naruko.common.MenuRoomData;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewAdapter;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewLayoutManger;

import java.util.ArrayList;
import java.util.List;

public class menuRoom extends Fragment implements View.OnClickListener {
    private int favAreaHeight;
    private List<MenuRoomData> dataList;

    private LinearLayout favArea;
    private Button favButton;

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference roomRef;

    private String TAG = "NARUKO_DEBUG";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        favArea = (LinearLayout) view.findViewById(R.id.menu_room_favArea);
//        favAreaHeight = favArea.getHeight();
//
//        favButton = (Button) view.findViewById(R.id.menu_room_favButton);
//        favButton.setOnClickListener(this);

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        roomRef = mFirebaseDatabase.collection("rooms");
        updateRoom(view);
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.menu_room_favButton:
//                //お気に入りが開いている時
//                if (favArea.getHeight() > 0){
//                    Log.d(TAG, "お気に入り開き");
//                    favButton.setBackgroundResource(R.drawable.ic_expand_less_black_24dp);
//
//                    //圧縮アニメーション
//                    AccordionAnimation closeAnimation = new AccordionAnimation(favArea, -favAreaHeight, favAreaHeight);
//                    closeAnimation.setDuration(500);
//                    favArea.startAnimation(closeAnimation);
//
//                } else {
//                    Log.d(TAG, "お気に入り閉まり");
//                    favButton.setBackgroundResource(R.drawable.ic_expand_more_black_24dp);
//
//                    //展開アニメーション
//                    AccordionAnimation openAnimation = new AccordionAnimation(favArea, favAreaHeight, 0);
//                    openAnimation.setDuration(500);
//                    favArea.startAnimation(openAnimation);
//
//                }
//                break;
//        }
    }

    //グループ取得
    public void updateRoom(final View view){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("チャットルーム読み込み中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        dataList = new ArrayList<>();
        roomRef.orderBy("datetime", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Error Listening Room", e);
                    return;
                }
                Log.d(TAG, "チャットルーム読み込み");
                for (DocumentChange document : snapshots.getDocumentChanges()) {
                    switch (document.getType()){
                        case ADDED:
                            String roomName = document.getDocument().getString("roomName");
                            String roomId = document.getDocument().getId();

                            if (!TextUtils.isEmpty(roomName)) {
                                MenuRoomData data = new MenuRoomData();
                                data.setInt(R.drawable.ic_launcher_background);
                                data.setTitle(roomName);
                                data.setId(roomId);

                                dataList.add(data);
                                Log.d(TAG, "RoomName: "+roomName);
                            }
                            break;
                    }
                }
                Log.d(TAG, "---------------------------------");

                TextView no_room = (TextView) view.findViewById(R.id.no_room);
                if (!dataList.isEmpty()){
                    no_room.setVisibility(View.GONE);
                    updateFavMenu(view, progressDialog);
                } else {
                    no_room.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }

            }
        });
    }

    //お気に入り生成
    public void updateFavMenu(View view, ProgressDialog progressDialog){
        //RecyclerView
        final RecyclerView menuChatRecyclerView = (RecyclerView)view.findViewById(R.id.roomFavRecyclerView);

        //Adapter
        IconRecyclerViewAdapter adapter = new IconRecyclerViewAdapter(dataList){
            @Override
            protected void onMenuClicked(@NonNull int position){
                super.onMenuClicked(position);
                String roomId = (dataList.get(position)).getid();

                Intent room = new Intent(getContext(), ActivityChat.class);
                room.putExtra("roomId", roomId);
                startActivity(room);
            }
        };
        menuChatRecyclerView.setAdapter(adapter);

        //LayoutManager
        IconRecyclerViewLayoutManger layoutManager = new IconRecyclerViewLayoutManger();
        menuChatRecyclerView.setLayoutManager(layoutManager);

        menuChatRecyclerView.setHasFixedSize(true);

        progressDialog.dismiss();
    }
}
