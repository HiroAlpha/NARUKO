package com.hiro_a.naruko.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import com.hiro_a.naruko.activity.ActivityMenu;
import com.hiro_a.naruko.common.MenuChatData;
import com.hiro_a.naruko.view.MenuRecyclerView.MenuAdapter;
import com.hiro_a.naruko.view.MenuRecyclerView.MenuLayoutManger;

import java.util.ArrayList;
import java.util.List;

public class menuChat extends Fragment {
    List<MenuChatData> dataList;

    ImageView mGroupAddButton;

    FirebaseFirestore mFirebaseDatabase;
    CollectionReference roomRef;

    String TAG = "NARUKO_DEBUG";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroupAddButton = (ImageView)view.findViewById(R.id.buttonGroupAdd);
        mGroupAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        roomRef = mFirebaseDatabase.collection("rooms");
        updateRoom(view);
    }

    //グループ取得
    public void updateRoom(final View view){
        dataList = new ArrayList<>();
        roomRef.orderBy("datetime", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (DocumentChange document : snapshots.getDocumentChanges()) {
                    switch (document.getType()){
                        case ADDED:
                            String roomName = document.getDocument().getString("roomName");
                            String roomId = document.getDocument().getId();

                            if (!TextUtils.isEmpty(roomName)) {
                                MenuChatData data = new MenuChatData();
                                data.setInt(R.drawable.ic_launcher_background);
                                data.setTitle(roomName);
                                data.setId(roomId);

                                dataList.add(data);
                                Log.w(TAG, roomName+":"+roomId);
                            }
                            break;
                    }
                }

                updateMenu(view);

            }
        });
    }

    //View生成
    public void updateMenu(View view){
        final RecyclerView menuChatRecyclerView = (RecyclerView)view.findViewById(R.id.menuChatRecyclerView);
        MenuAdapter adapter = new MenuAdapter(dataList){
            @Override
            protected void onMenuClicked(@NonNull int position){
                super.onMenuClicked(position);
                String roomId = (dataList.get(position)).getid();

                Intent room = new Intent(getContext(), ActivityChat.class);
                room.putExtra("roomId", roomId);
                Log.w(TAG, roomId);
                startActivity(room);
            }
        };

        MenuLayoutManger layoutManager = new MenuLayoutManger();

        menuChatRecyclerView.setHasFixedSize(true);
        menuChatRecyclerView.setLayoutManager(layoutManager);
        menuChatRecyclerView.setAdapter(adapter);
    }
}
