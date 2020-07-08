package com.hiro_a.naruko.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.activity.ActivityNaruko;
import com.hiro_a.naruko.common.MenuRoomData;
import com.hiro_a.naruko.task.Hash;
import com.hiro_a.naruko.view.RecyclerView.RoomView.LinearLayoutAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class MenuRoomSearch extends Fragment {
    private String TAG = "NARUKO_DEBUG @ menuRoomSearch.fragment";

    Context context;

    private String filter = "作成日時";
    private Query.Direction direction = Query.Direction.DESCENDING;
    private List<MenuRoomData> dataList;

    private View mainView;
    private EditText editText_search;
    private Spinner spinner_filter;
    RecyclerView roomSearchRecyclerView;

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference roomRef;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_menu_room_search, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //メインビュー
        this.mainView = view;

        //コンテキスト
        context = getContext();

        //*** Firebase ***
        //FirebaseStore
        firebaseFirestore = FirebaseFirestore.getInstance();
        //ルームリファレンス
        roomRef = firebaseFirestore.collection("rooms");
        //ストレージリファレンス
        storageReference = FirebaseStorage.getInstance().getReference();

        //並び替え項目設定
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                context,
                R.layout.layout_room_search_spinner);
        spinnerAdapter.setDropDownViewResource(R.layout.layout_room_search_spinner_dialog);
        spinnerAdapter.add("作成日時");
        spinnerAdapter.add("ルーム名");


        //並び替えスピナー
        spinner_filter = view.findViewById(R.id.fRoomSearch_spinner_spinner);
        spinner_filter.setAdapter(spinnerAdapter);
        spinner_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner)parent;

                //選択アイテムを取得
                String selectedItem = (String)spinner.getSelectedItem();

                switch (selectedItem) {
                    case "作成日時":
                        filter = "CreatedTime";
                        direction = Query.Direction.DESCENDING;
                        break;

                    case "ルーム名":
                        filter = "RoomName";
                        direction = Query.Direction.ASCENDING;
                }

                //ルームリスト更新
                updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //ルーム検索ボックス
        editText_search = (EditText)view.findViewById(R.id.fRoomSearch_editText_search);

        //検索ボタン
        final TextView textView_roomSearch = (TextView)view.findViewById(R.id.fRoomSearch_textView_enter);
        textView_roomSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText_search.getText() != null && !editText_search.getText().toString().equals("")){
                    searchById(editText_search.getText().toString());
                }
            }
        });
        textView_roomSearch.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //タッチされたボタンの色
                int defaultButtonColor = getResources().getColor(R.color.colorHuu);

                switch (event.getAction()) {
                    //触れている場合
                    case MotionEvent.ACTION_DOWN:
                        //色を少し暗くする
                        float[] hsv = new float[3];
                        Color.colorToHSV(defaultButtonColor, hsv);
                        hsv[2] -= 0.2f;
                        textView_roomSearch.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));

                        break;

                    //離された場合
                    case MotionEvent.ACTION_UP:
                        //元の色に戻す
                        textView_roomSearch.setBackgroundTintList(ColorStateList.valueOf(defaultButtonColor));
                        break;
                }
                return false;
            }
        });

        //RecyclerView
        roomSearchRecyclerView = (RecyclerView)view.findViewById(R.id.fRoomSearch_recyclerView_rooms);
    }

    //IDで検索
    private void searchById(String searchId){
        roomRef.document(searchId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    //ドキュメントスナップショット
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()){
                        //ルームリスト初期化
                        dataList = new ArrayList<>();

                        //ルーム名取得
                        String roomName = documentSnapshot.getString("RoomName");

                        //ルームID取得
                        String roomId = documentSnapshot.getId();

                        //作成日時を取得
                        String createdTime = documentSnapshot.getString("CreatedTime");

                        //パスワード取得
                        boolean passwordIs = documentSnapshot.getBoolean("PasswordIs");
                        String password = "";
                        //パスワードがある場合
                        if (passwordIs){
                            password = documentSnapshot.getString("Password");
                        }

                        //ルーム画像取得
                        boolean imageIs = documentSnapshot.getBoolean("ImageIs");
                        StorageReference imageReference = null;
                        if (imageIs){
                            imageReference = storageReference.child("Images/RoomImages/" + roomId + ".jpg");
                        }

                        MenuRoomData data = new MenuRoomData();
                        data.setTitle(roomName);
                        data.setId(roomId);
                        data.setCreatedTime(createdTime);
                        data.setPassword(password);
                        data.setImage(imageReference);

                        dataList.add(data);

                        roomRecycleView();
                    }
                }
            }
        });
    }

    //ルームリスト更新
    private void updateList(){
        //ルームリスト初期化
        dataList = new ArrayList<>();

        roomRef.orderBy(filter, direction).limit(20).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //スナップショット数カウンター
                    int snapshotcount = 0;

                    //スナップショット
                    QuerySnapshot querySnapshot = task.getResult();

                    //スナップショットサイズ
                    int snapshotSize = querySnapshot.size();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        //ルーム名取得
                        String roomName = documentSnapshot.getString("RoomName");

                        //ルームID取得
                        String roomId = documentSnapshot.getId();

                        //作成日時を取得
                        String createdTime = documentSnapshot.getString("CreatedTime");

                        //パスワード取得
                        boolean passwordIs = documentSnapshot.getBoolean("PasswordIs");
                        String password = "";
                        //パスワードがある場合
                        if (passwordIs){
                            password = documentSnapshot.getString("Password");
                        }

                        //ルーム画像取得
                        boolean imageIs = documentSnapshot.getBoolean("ImageIs");
                        StorageReference imageReference = null;
                        if (imageIs){
                            imageReference = storageReference.child("Images/RoomImages/" + roomId + ".jpg");
                        }

                        MenuRoomData data = new MenuRoomData();
                        data.setTitle(roomName);
                        data.setId(roomId);
                        data.setCreatedTime(createdTime);
                        data.setPassword(password);
                        data.setImage(imageReference);

                        dataList.add(data);

                        snapshotcount++;

                        //最後の処理が終わった場合
                        if (snapshotcount >= snapshotSize){
                            //RecyclerView更新
                            dataCheck();
                        }
                    }
                }
            }
        });
    }

    //データ有無確認
    private void dataCheck(){
        TextView no_room = (TextView) mainView.findViewById(R.id.fRoomSearch_textView_noRoom);
        if (!dataList.isEmpty()){
            no_room.setVisibility(View.GONE);
            roomRecycleView();
        } else {
            Log.w(TAG, "WARNING DATALIST is EMPTY");
            no_room.setVisibility(View.VISIBLE);
        }
    }

    //ルームリストRecyclerView設定
    private void roomRecycleView(){
        //Adapter
        LinearLayoutAdapter adapter = new LinearLayoutAdapter(context, dataList, filter){
            @Override
            protected void onMenuClicked(@NonNull int position) {
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
        roomSearchRecyclerView.setAdapter(adapter);

        //LayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        roomSearchRecyclerView.setLayoutManager(linearLayoutManager);

        //間の線
        RecyclerView.ItemDecoration devideLine = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        roomSearchRecyclerView.addItemDecoration(devideLine);

        roomSearchRecyclerView.setHasFixedSize(true);
    }

    private void passAuthWindow(String Id, final String password){
        final String roomId = Id;

        //ウィンドウサイズ取得
        WindowManager wm = (WindowManager)getActivity().getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        int screenHeight = size.y;

        final PopupWindow passAuthPopup = new PopupWindow(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_menu_room_fav_popup, null);
        passAuthPopup.setContentView(view);
        passAuthPopup.setOutsideTouchable(true);
        passAuthPopup.setFocusable(true);

        passAuthPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        passAuthPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        passAuthPopup.showAsDropDown(spinner_filter);

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
