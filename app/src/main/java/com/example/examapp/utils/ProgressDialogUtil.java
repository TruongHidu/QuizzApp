package com.example.examapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.examapp.R;

public class ProgressDialogUtil {
    private Dialog dialog;
    private TextView dialogText;

    public ProgressDialogUtil(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = dialog.findViewById(R.id.txtDialog);
    }

    public void show(String message) {
        dialogText.setText(message);
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}