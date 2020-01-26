package com.hiro_a.naruko.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.FriendId;
import com.hiro_a.naruko.common.MenuFriendData;
import com.hiro_a.naruko.view.RecyclerView.FriendView.LinearLayoutAdapter;

import java.util.ArrayList;
import java.util.List;

public class menuFriend extends Fragment {
     FriendId friendData;
    private List<MenuFriendData> friendList;

    private FirebaseAuth mFirebaseAuth;
    private String userId;

    private FirebaseFirestore mFirebaseDatabase;
    private DocumentReference friendRef;
    private CollectionReference userRef;

    private String TAG = "NARUKO_DEBUG";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        friendData = new FriendId();

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        userRef = mFirebaseDatabase.collection("users");
        friendRef = mFirebaseDatabase.collection("users").document(userId);
        getFriend(view);
    }

    //フレンド取得（自動更新はしない）
    private void getFriend(final View view){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("勇者部員の情報を読み込み中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();


        friendList = new ArrayList<>();
        friendRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()){
                        final List<String> friendArray = (List<String>) document.get("friend");

                        if (friendArray == null){
                            return;
                        }

                        friendData.setFriendArraySize(friendArray.size());
                        friendData.setView(view);

                        Log.d(TAG, "フレンド数:"+friendArray.size());

                        for (int i=0;i<friendArray.size();i++){

                            int friendImage = R.drawable.ic_launcher_background;
                            String friendId = friendArray.get(i);

                            friendData.setFriendId(friendId);
                            friendData.setCounter(i);

                            if (!TextUtils.isEmpty(friendId)) {
                                getFriendInfo();

                                progressDialog.dismiss();
                            }
                        }
                    }else {
                        Log.w(TAG, "No such document");
                    }
                }else {
                    Log.w(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getFriendInfo(){
        DocumentReference friendDocument = userRef.document(friendData.getFriendId());
        friendDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()){
                    String friendImage = document.getString("userImage"); //画像
                    String friendName = document.getString("userName"); //名前
                    String friendId = friendData.getFriendId(); //ID

                    MenuFriendData data = new MenuFriendData();
                    if (friendImage ==null){
                        data.setFriendImage("noImage");
                    }else {
                        data.setFriendImage(friendImage);
                    }
                    data.setFriendName(friendName);
                    data.setFriendId(friendId);

                    friendList.add(data);
                    Log.d(TAG, friendName+":"+friendId);

                    if (friendData.getCounter() == friendData.getFriendArraySize()-1){
                        updateMenu(friendData.getView());
                    }
                }
            }
        });
    }

    //RecyclerView生成
    public void updateMenu(View view){
        //RecyclerView
        final RecyclerView friendRecyclerView = (RecyclerView)view.findViewById(R.id.friendRecyclerView);

        //Adapter
        RecyclerView.Adapter adapter = new LinearLayoutAdapter(friendList){
            @Override
            protected void onMenuClicked(@NonNull int position){
                super.onMenuClicked(position);

            }
        };
        friendRecyclerView.setAdapter(adapter);

        //LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        friendRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration devideLine = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        friendRecyclerView.addItemDecoration(devideLine);

        friendRecyclerView.setHasFixedSize(true);
    }
}
