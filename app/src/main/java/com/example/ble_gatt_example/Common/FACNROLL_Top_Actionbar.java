package com.example.ble_gatt_example.Common;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

public class FACNROLL_Top_Actionbar {
    private ActionBar actionBar;
    private Activity activity;
    private int top_layout;
    private View mCustomView;
    private TextView message_count;
    public FACNROLL_Top_Actionbar(Activity activity, ActionBar actionBar, int top_layout){
        this.activity = activity;
        this.actionBar = actionBar;
        this.top_layout = top_layout;
    }
    private IViewListener iViewListener;
    public void setIViewListener(IViewListener iViewListener){
        this.iViewListener = iViewListener;
        if(iViewListener != null){
            if(mCustomView != null){
                iViewListener.onView(mCustomView);
            }
        }
    }

    public void init(){
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(0,0,0,0);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        View mCustomView = LayoutInflater.from(activity).inflate(this.top_layout, null);
        actionBar.setCustomView(mCustomView, layoutParams);
        if(iViewListener != null){
            iViewListener.onView(mCustomView);
        }
    }

    public interface IViewListener{
        void onView(View view);
    }
}
