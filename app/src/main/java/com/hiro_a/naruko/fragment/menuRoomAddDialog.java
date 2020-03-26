package com.hiro_a.naruko.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiro_a.naruko.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class menuRoomAddDialog extends DialogFragment {

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        final Fragment fragment = getTargetFragment();

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("確認");
        dialogBuilder.setMessage(getString(R.string.roomAddDialog));

        //OK
        dialogBuilder.setPositiveButton("作成", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                menuRoomAdd activity = (menuRoomAdd) getParentFragment();
                activity.createRoom();
            }
        });

        //キャンセル
        dialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 何もしないで閉じる
            }
        });

        AlertDialog dialog = dialogBuilder.create();

        return dialog;
    }
}
