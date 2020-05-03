package com.accurascan.accurasdk.sample.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.accurascan.accurasdk.sample.R;

/**
 * Created by richa on 27/4/17.
 */

public abstract class AlertDialogAbstract {

    protected AlertDialogAbstract(@NonNull final Activity context, String message, String pos_btn, String neg_btn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.app_name));
        builder.setMessage(message);
        builder.setCancelable(false);

        if (!TextUtils.isEmpty(pos_btn)) {
            builder.setPositiveButton(pos_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    positive_negativeButtonClick(1);
                    dialog.dismiss();
                }
            });
        }

        if (!TextUtils.isEmpty(neg_btn)) {
            builder.setNegativeButton(neg_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    positive_negativeButtonClick(0);
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

//        if(context.isFinishing()) {
            alertDialog.show();
//        }
    }

    public abstract void positive_negativeButtonClick(int pos_neg_id);


}
