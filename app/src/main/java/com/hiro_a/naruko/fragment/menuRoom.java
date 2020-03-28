package com.hiro_a.naruko.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityChat;
import com.hiro_a.naruko.common.MenuRoomData;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewAdapter;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewLayoutManger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class menuRoom extends Fragment implements View.OnClickListener {
    private int favAreaHeight;
    private List<MenuRoomData> dataList;

    private LinearLayout favArea;
    private Button favButton;

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference roomRef;
    private StorageReference mStorageRefernce;

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

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        roomRef = mFirebaseDatabase.collection("rooms");

        mStorageRefernce = FirebaseStorage.getInstance().getReference();

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
                    final String roomName = document.getDocument().getString("roomName");
                    final String roomId = document.getDocument().getId();
                    boolean imageIs = document.getDocument().getBoolean("imageIs");

                    switch (document.getType()){
                        case ADDED:
                            Log.w(TAG, "Event Occurred: " + roomName + " ADDED");
                            if (!TextUtils.isEmpty(roomName)) {
                                StorageReference imageReference = mStorageRefernce.child("Images/RoomImages/" + roomId + ".jpg");

                                if (imageIs){
                                    imageReference = mStorageRefernce.child("Images/RoomImages/" + roomId + ".jpg");
                                }

                                MenuRoomData data = new MenuRoomData();
                                data.setImage(imageReference);
                                data.setTitle(roomName);
                                data.setId(roomId);

                                dataList.add(data);
                            }

                            break;

                        case REMOVED:
                            Log.w(TAG, "Event Occurred: " + roomName + " REMOVED");
                            break;

                        case MODIFIED:
                            Log.w(TAG, "Event Occurred: " + roomName + " MODIFIED");
                            break;

                    }
                }
                dataCheck(view, progressDialog);
                Log.d(TAG, "---------------------------------");
            }
        });
    }

    //データ有無確認
    private void dataCheck(View view, ProgressDialog progressDialog){
        TextView no_room = (TextView) view.findViewById(R.id.no_room);
        if (!dataList.isEmpty()){
            no_room.setVisibility(View.GONE);
            roomRecycleView(view, progressDialog);
        } else {
            Log.w(TAG, "WARNING DATALIST is EMPTY");
            no_room.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }
    }

    //RecycleView
    private void roomRecycleView(View view, ProgressDialog progressDialog){
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
