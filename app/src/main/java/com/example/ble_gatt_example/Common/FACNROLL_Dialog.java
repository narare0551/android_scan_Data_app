package com.example.ble_gatt_example.Common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class FACNROLL_Dialog {
    public interface IDialogAction{
        void okAction(Object data);
        void cancelAction(Object data);
    }
    public void show(Activity activity, String contents, final Object ok_data, final Object cancel_data, final IDialogAction iDialogAction){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(contents).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                iDialogAction.okAction(ok_data);
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                iDialogAction.cancelAction(cancel_data);
            }
        }).show();
    }

}
