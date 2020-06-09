package com.hiro_a.naruko.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class Dialog extends DialogFragment implements DialogInterface.OnClickListener {

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("エラー");
        dialogBuilder.setMessage("予期しないエラーが発生しました。\nアプリケーションを終了します。\n" + context);
        dialogBuilder.setPositiveButton("終了", this);

        return dialogBuilder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        getActivity().moveTaskToBack(true);
    }
}
