package com.hiro_a.naruko.activity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiro_a.naruko.R;
import com.hiro_a.naruko.common.DeviceInfo;
import com.hiro_a.naruko.common.MenuRoomData;
import com.hiro_a.naruko.common.NarukoMessageData;
import com.hiro_a.naruko.fragment.Dialog;
import com.hiro_a.naruko.task.ButtonColorChangeTask;
import com.hiro_a.naruko.task.MakeStoargeUri;
import com.hiro_a.naruko.view.CustomImageView;
import com.hiro_a.naruko.view.NarukoView.NarukoView_NewMessage;
import com.hiro_a.naruko.view.NarukoView.NarukoView_OldMessage;
import com.hiro_a.naruko.view.NarukoView.NarukoView_TopCircle;
import com.hiro_a.naruko.view.NarukoView.NarukoView_UserIconLine;
import com.hiro_a.naruko.view.NarukoView.NarukoView_UserIconPopup;
import com.isseiaoki.simplecropview.callback.CropCallback;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ActivityChat extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private String TAG = "NARUKO_DEBUG @ ActivityChat";
    private Context context;

    private Point screenSize;
    private String userName_original;
    private String userId_original;
    private Boolean userImageIs_original;
    private String  userColor_original;
    private int lastSpokeIconNum;
    private boolean chatTextShown = false;
    private boolean menuShown = true;
    private boolean firstLoad = true;

    private ArrayList<String> userIdArray;
    private ArrayList<NarukoMessageData> messageArrray;

    private View bottomMenu;
    private EditText editText_message;
    private NarukoView_TopCircle narukoView_topCircle;
    private NarukoView_NewMessage narukoView_newMessage;
    private NarukoView_OldMessage narukoView_oldMessage;
    private NarukoView_UserIconLine narukoView_userIconLine;

    private PopupWindow historyView;

    private StorageReference firebaseStorage;
    private CollectionReference messageRef;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naruko);

        //コンテキスト
        context = getApplicationContext();

        //ユーザーリスト作成
        userIdArray = new ArrayList<>();

        //フォント設定
        Typeface typeface = Typeface.createFromAsset(getAssets(), "anzu_font.ttf");

        //DeviceInfoからユーザー情報を取得
        DeviceInfo userInfo = new DeviceInfo();
        userId_original = userInfo.getUserId(context);
        userName_original = userInfo.getUserName(context);
        userImageIs_original = userInfo.getUserImageIs(context);
        userColor_original = userInfo.getUserColor(context);
        Log.d(TAG, "*** User_Info ***");
        Log.d(TAG, "UserName: " + userName_original);
        Log.d(TAG, "UserId: " + userId_original);
        Log.d(TAG, "UserImageIs: " + userImageIs_original);
        Log.d(TAG, "---------------------------------");

        //DeviceInfoから画面情報を取得
        int screenWidth = (int) userInfo.getScreenWidth(context);
        int screenHeight = (int) userInfo.getScreenHeight(context);
        screenSize = new Point(screenWidth, screenHeight);

        //MenuRoomから渡されたRoomIdを取得
        Intent room = getIntent();
        String roomId = room.getStringExtra("roomId");
        Log.d(TAG, "*** ChatRoom_Info ***");
        Log.d(TAG, "RoomId: " + roomId);
        Log.d(TAG, "---------------------------------");

        //メッセージ入力スペース
        editText_message = findViewById(R.id.naruko_editText_message);
        editText_message.setTypeface(typeface);
        editText_message.setWidth(screenWidth-20);

        //送信ボタン
        ImageView imageView_sendMessageButton = findViewById(R.id.naruko_imageView_sendMessage);
        imageView_sendMessageButton.setOnClickListener(this);
        imageView_sendMessageButton.setOnLongClickListener(this);

        //下部メニュー
        bottomMenu = findViewById(R.id.naruko_view_bottomMenu);

        //下部メニュースライドボタン
        ImageView imageView_slideButton = findViewById(R.id.bottomMenu_button_slide);
        imageView_slideButton.setOnClickListener(this);

        //上部円及び履歴ボタン
        narukoView_topCircle = findViewById(R.id.naruko_view_topCircle);
        narukoView_topCircle.setOnClickListener(this);

        //最新メッセージ表示領域
        narukoView_newMessage = findViewById(R.id.naruko_view_newMesage);

        //他メッセージ表示領域
        narukoView_oldMessage = findViewById(R.id.naruko_view_oldMessage);

        //アイコンからメッセージへの白線
        narukoView_userIconLine = findViewById(R.id.naruko_view_userIconLine);

        //*** Firebase ***
        //FirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        //メッセージレファレンス
        //ルームIDがnullだった場合
        if (roomId == null){
            //ルームIDを仮設定
            roomId = "RoomId is Null";
            //フラグメントマネージャー
            FragmentManager fragmentManager = getSupportFragmentManager();
            //エラーダイアログ
            new Dialog().show(fragmentManager, TAG);
        }
        messageRef = FirebaseFirestore.getInstance().collection("rooms").document(roomId).collection("messages");

        //メッセージ更新
        updateMessage();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        listenerRegistration.remove();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //メッセージ送信ボタン
            case R.id.naruko_imageView_sendMessage:
                //文字入力画面が表示されている場合
                if (chatTextShown){
                    //メッセージが空でない場合のみ送信
                    if (!(TextUtils.isEmpty(editText_message.getText().toString()))){
                        sendMessage();
                    }
                } else {
                    //文字入力画面表示
                    findViewById(R.id.naruko_layout_editText).setVisibility(View.VISIBLE);

                    chatTextShown = true;
                }

                break;

            //メッセージ履歴表示ボタン
            case R.id.naruko_view_topCircle:
                //メッセージ履歴表示
                FrameLayout frameLayout = findViewById(R.id.naruko_layout_main);
                createHistoryView(frameLayout);

                break;

            //下部メニュースライドボタン
            case R.id.bottomMenu_button_slide:
                //下部メニュースライドアニメーション
                viewSlide();
                break;

        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (chatTextShown){
            //文字入力画面非表示
            findViewById(R.id.naruko_layout_editText).setVisibility(View.INVISIBLE);

            chatTextShown = false;
        }

        return false;
    }

    //メッセージ送信
    public void sendMessage(){
        //送信日時
        SimpleDateFormat SD = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.JAPAN);
        String time = SD.format(new Date());

        //グローバルIP
        String globalIP = getPublicIPAddress();

        //メッセージ
        String text = editText_message.getText().toString();

        //送信内容
        Map<String, Object> message = new HashMap<>();
        message.put("Datetime", time);
        message.put("GlobalIP", globalIP);
        message.put("UserName", userName_original);
        message.put("UserId", userId_original);
        message.put("UserImageIs", userImageIs_original);
        message.put("UserColor", userColor_original);
        message.put("Message", text);

        //Firestoreに送信
        messageRef.add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "SUCSESS: Document Added");
                Log.d(TAG, "---------------------------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ERROR: Adding Document Failed", e);
                Log.w(TAG, "---------------------------------");
            }
        });

        //メッセージ入力領域をリセット
        editText_message.setText("");
    }

    //メッセージ更新
    public void updateMessage(){
        //日時でソートして表示
        listenerRegistration = messageRef.orderBy("Datetime", Query.Direction.DESCENDING).limit(25).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                //更新失敗
                if (e != null) {
                    Log.w(TAG, "ERROR: Listen Failed", e);
                    Log.w(TAG, "---------------------------------");

                    //更新終了
                    return;
                }

                //メッセージリスト作成
                messageArrray = new ArrayList<>();

                //更新成功
                for (DocumentChange document : Objects.requireNonNull(snapshots).getDocumentChanges()) {
                    switch (document.getType()){
                        //メッセージ追加時
                        case ADDED:
                            //送信日時取得
                            String datetime = document.getDocument().getString("Datetime");

                            //グローバルIP取得
                            String globalIP = document.getDocument().getString("GlobalIP");

                            //ユーザー名取得
                            String userName = document.getDocument().getString("UserName");

                            //ユーザーID取得
                            String userId = document.getDocument().getString("UserId");

                            //ユーザー画像の有無取得
                            Boolean userImageIs = document.getDocument().getBoolean("UserImageIs");

                            //ユーザー画像が存在する場合画像パス生成
                            String userImage = "Default_Image";
                            if (userImageIs){
                                userImage = "Images/UserImages/" + userId + ".png";
                            }

                            //ユーザーカラー取得
                            String color = document.getDocument().getString("UserColor");

                            //メッセージ取得
                            String text = document.getDocument().getString("Message");

                            //メッセージが空でない場合
                            if (!TextUtils.isEmpty(text)) {
                                //メッセージデータを新規作成
                                NarukoMessageData messageData = new NarukoMessageData();
                                messageData.setPostedTime(datetime);
                                messageData.setGlobalIp(globalIP);
                                messageData.setUserName(userName);
                                messageData.setUserId(userId);
                                messageData.setUserImage(userImage);
                                messageData.setUserColor(color);
                                messageData.setMessage(text);

                                //配列に追加
                                messageArrray.add(messageData);

//                                Log.d(TAG, "SnapshotSize: "+snapshots.size());
//                                Log.d(TAG, "MessageArraySize: "+messageArrray.size());

                                //メッセージ配列のサイズとスナップショットサイズが同じ場合
                                if (messageArrray.size() == snapshots.size() && firstLoad){
                                    //メッセージバー表示用配列作成
                                    ArrayList<NarukoMessageData> messageArray8Line = new ArrayList<>();
                                    int count = 0;
                                    for (int i=0;i<8 && i<messageArrray.size();i++){
                                        messageArray8Line.add(messageArrray.get(count));

                                        //23文字以上だった場合
                                        if (messageArrray.get(count).getMessage().length() > 23){
                                            //iをプラスして枠をつぶす
                                            i++;
                                        }
                                        count++;
                                    }

                                    Collections.reverse(messageArrray);
                                    Collections.reverse(messageArray8Line);

                                    firstLoad = !firstLoad;

                                    //メッセージ表示
                                    showMessage(messageArray8Line);
                                } else if (!firstLoad){
                                    showMessage(messageArrray);
                                }
                            }
                            break;

                        //メッセージ削除時
                        case REMOVED:
                            break;
                    }
                }
            }
        });
    }

    private void showMessage(ArrayList<NarukoMessageData> messageArray8Line){
        NarukoView_UserIconPopup narukoUserIconPopoutView = findViewById(R.id.naruko_view_userIcon);
        for (int i=0;i<messageArray8Line.size();i++){
            //メッセージデータ
            NarukoMessageData messageData = messageArray8Line.get(i);

            //内部のデータ取得
            String userName = messageData.getUserName();
            String userId = messageData.getUserId();
            String userImage = messageData.getUserImage();
            String userColor = messageData.getUserColor();
            String text = messageData.getMessage();

            Log.d(TAG, "*** Message_Info ***");
//            Log.d(TAG, "PostedTime: "+datetime);
//            Log.d(TAG, "GlobalIP: "+globalIP);
            Log.d(TAG, "UserName: "+userName);
            Log.d(TAG, "UserId: "+userId);
            Log.d(TAG, "UserImage: "+userImage);
            Log.d(TAG, "UserColor: "+userColor);
            Log.d(TAG, "Message: "+text);
            Log.d(TAG, "---------------------------------");

            //メッセージを表示
            (narukoView_newMessage).getMessage(context, text, userColor);
            viewRotate();

            //古いメッセージ欄を更新
            narukoView_oldMessage.getMessage(context, text, userColor);

            //送信者のアイコンがまだ表示されていない場合アイコンを表示
            if (!userIdArray.contains(userId)){
                //ユーザーをuserIdArrayに追加
                userIdArray.add(userId);

                //ユーザーアイコンを表示
                narukoUserIconPopoutView.addUserIcon(screenSize, (RelativeLayout) findViewById(R.id.naruko_layout_userIcon), userName, userImage, userColor);
            }

            //最終発言者の配列番号を記録
            lastSpokeIconNum = userIdArray.indexOf(userId);

            //白線を表示
            narukoUserIconPopoutView.setLastSpeaker(narukoView_userIconLine, lastSpokeIconNum, text.length()>23);
        }
    }

    //送信者のグローバルIP取得
    public String getPublicIPAddress(){
        String value = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> result = es.submit(new Callable<String>() {
            public String call() throws Exception {
                try {
                    URL url = new URL("http://whatismyip.akamai.com/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        urlConnection.disconnect();
                        return total.toString();
                    }finally {
                        urlConnection.disconnect();
                    }
                }catch (IOException e){
                    Log.e("Public IP: ",e.getMessage());
                }
                return null;
            }
        });
        try {
            value = result.get();
        } catch (Exception e) {
            Log.w(TAG, "ERROR: Exception Occured", e);
            Log.w(TAG, "---------------------------------");
        }
        es.shutdown();
        return value;
    }

    //履歴ビュー表示
    public void createHistoryView(View parentView) {
        //HistoryViewウィンドウ
        historyView = new PopupWindow(context);
        View view = this.getLayoutInflater().inflate(R.layout.activity_naruko_popup, null);
        historyView.setContentView(view);
        historyView.setOutsideTouchable(true);
        historyView.setFocusable(true);

        //横幅
        historyView.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        //高さ
        historyView.setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        //位置
        historyView.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        //履歴配列作成
        ArrayList<String> messageHistoryArray = new ArrayList<>();
        for (int i=0;i<messageArrray.size();i++){
            String userName = messageArrray.get(i).getUserName();
            String message = messageArrray.get(i).getMessage();

            messageHistoryArray.add(userName + ": " + message);
        }

        //履歴リストビュー
        ListView historyListView = view.findViewById(R.id.naruko_listView_history);
        ArrayAdapter historyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messageHistoryArray){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView)super.getView(position, convertView, parent);
                view.setHeight(50) ;
                view.setMinimumHeight(50) ;

                return view;
            }
        };
        historyListView.setAdapter(historyAdapter);


    }

    //------以下アニメーション------

    //メッセージ欄アニメーション
    private void viewRotate(){
        //文字列回転アニメーション
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.view_rotation);    //アニメーションはR.anim.view_rotationから
        narukoView_newMessage.startAnimation(rotate);

        //履歴回転アニメーション（ずらす）
        Animation rotate_instant = AnimationUtils.loadAnimation(this, R.anim.view_rotation_instant);    //アニメーションはR.anim.view_rotation_instantから
        narukoView_oldMessage.startAnimation(rotate_instant);
    }

    //下部メニュースライドアニメーション
    private void viewSlide(){
        //移動距離
        int bottomMenubar_slideLength = -(screenSize.x/2)+20;

        if (menuShown){ //下部メニューが出ている場合
            ObjectAnimator translate = ObjectAnimator.ofFloat(bottomMenu, "translationX", 0, bottomMenubar_slideLength);
            translate.setDuration(700);
            translate.start();
            menuShown = false;

        } else if (!menuShown){ //下部メニューが隠されている場合
            ObjectAnimator translate = ObjectAnimator.ofFloat(bottomMenu, "translationX", bottomMenubar_slideLength, 0);
            translate.setDuration(700);
            translate.start();
            menuShown = true;
        }
    }
}
