package com.hiro_a.naruko.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityNaruko;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.common.MenuRoomData;
import com.hiro_a.naruko.task.Hash;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewAdapter;
import com.hiro_a.naruko.view.IconRecyclerView.IconRecyclerViewLayoutManger;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class MenuRoomFav extends Fragment implements View.OnClickListener {
    private String TAG = "NARUKO_DEBUG @ MenuRoomFav.fragment";
    private Context context;

    View mainView;
    TextView favTitle;

    private List<MenuRoomData> dataList;

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference roomRef;
    private StorageReference storageReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_room_fav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //メインビュー定義
        this.mainView = view;

        //コンテキスト
        context = getContext();

        favTitle = (TextView) view.findViewById(R.id.fRoomFav_textView_title);

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        roomRef = mFirebaseDatabase.collection("rooms");

        storageReference = FirebaseStorage.getInstance().getReference();

        updateRoom();
    }

    @Override
    public void onClick(View view) {

    }

    //ルーム取得
    public void updateRoom(){

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("チャットルーム読み込み中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //SharedPreferences
        SharedPreferences userData = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        //お気に入り取得
        final ArrayList<String> favRooms = new DeviceInfo().getUserFavRooms(context);

        dataList = new ArrayList<>();

        if (!favRooms.isEmpty()){
            for (int i=0;i<favRooms.size();i++){
                roomRef.document(favRooms.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (documentSnapshot.exists()){
                                //ルーム名
                                String roomName = documentSnapshot.getString("RoomName");

                                //ルームID
                                String roomId = documentSnapshot.getId();

                                //パスワード
                                boolean passwordIs = documentSnapshot.getBoolean("PasswordIs");
                                String password = "";
                                if (passwordIs){
                                    password = documentSnapshot.getString("Password");
                                }

                                //画像
                                boolean imageIs = documentSnapshot.getBoolean("ImageIs");
                                StorageReference imageReference = null;
                                if (imageIs){
                                    imageReference = storageReference.child("Images/RoomImages/" + roomId + ".jpg");
                                }

                                MenuRoomData data = new MenuRoomData();
                                data.setTitle(roomName);
                                data.setId(roomId);
                                data.setPassword(password);
                                data.setImage(imageReference);

                                Log.d(TAG, roomName);
                                Log.d(TAG, roomId);
                                Log.d(TAG, password);
                                Log.d(TAG, "---------------------------------");

                                dataList.add(data);

                                if (dataList.size() >= favRooms.size()){
                                    dataCheck(mainView, progressDialog);
                                }
                            }
                        }
                    }
                });
            }
        }

    }

    //データ有無確認
    private void dataCheck(View view, ProgressDialog progressDialog){
        TextView no_room = (TextView) view.findViewById(R.id.fRoomFav_textView_noRoom);
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
        final RecyclerView menuChatRecyclerView = (RecyclerView)view.findViewById(R.id.fRoomFav_recyclerView_rooms);

        //Adapter
        IconRecyclerViewAdapter adapter = new IconRecyclerViewAdapter(dataList){
            @Override
            protected void onMenuClicked(@NonNull int position){
                super.onMenuClicked(position);
                String roomId = (dataList.get(position)).getId();
                String password = (dataList.get(position)).getPassword();

                if (!password.isEmpty()){
                    passAuthWindow(roomId, password);
                } else {
                    Intent room = new Intent(getContext(), ActivityNaruko.class);
                    room.putExtra("RoomId", roomId);
                    startActivity(room);
                }
            }
        };
        menuChatRecyclerView.setAdapter(adapter);

        //LayoutManager
        IconRecyclerViewLayoutManger layoutManager = new IconRecyclerViewLayoutManger();
        menuChatRecyclerView.setLayoutManager(layoutManager);

        menuChatRecyclerView.setHasFixedSize(true);

        progressDialog.dismiss();
    }

    private void passAuthWindow(final String roomId, final String password){

        final PopupWindow passAuthPopup = new PopupWindow(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_menu_room_fav_popup, null);
        passAuthPopup.setContentView(view);
        passAuthPopup.setOutsideTouchable(true);
        passAuthPopup.setFocusable(true);

        passAuthPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        passAuthPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        passAuthPopup.showAsDropDown(favTitle);

        final EditText passwordEdittext = (EditText) view.findViewById(R.id.fRoomFav_editText_password);

        Button enterPassword = (Button) view.findViewById(R.id.fRoomFav_button_finish);
        enterPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = passwordEdittext.getText().toString();
                String hashed_enterdPassword = new Hash().doHash(enteredPassword);

                if (hashed_enterdPassword.equals(password)){
                    Intent room = new Intent(getContext(), ActivityNaruko.class);
                    room.putExtra("RoomId", roomId);
                    startActivity(room);

                    passAuthPopup.dismiss();
                } else {
                    passwordEdittext.setText("");
                }
            }
        });
    }
}
