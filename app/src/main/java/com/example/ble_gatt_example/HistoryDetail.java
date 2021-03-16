package com.example.ble_gatt_example;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ble_gatt_example.Common.Common;
import com.example.ble_gatt_example.Common.FACNROLL_HttpSocket;
import com.example.ble_gatt_example.Common.FACNROLL_Top_Actionbar;

public class HistoryDetail extends AppCompatActivity implements FACNROLL_Top_Actionbar.IViewListener{
    private ActionBar actionBar;
    private FACNROLL_Top_Actionbar F_actionbar;
    private FACNROLL_HttpSocket socket;
    private String idx;
    private Common common = new Common();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_detail);
        idx = getIntent().getStringExtra("idx");
        System.out.println(idx);
        actionBar = getSupportActionBar();
        F_actionbar = new FACNROLL_Top_Actionbar(this,this.actionBar,R.layout.actionbar);
        F_actionbar.setIViewListener(this);
        F_actionbar.init();
        init();
        getDetail();
    }

    private TextView detail_users,detail_mac,detail_barcode,detail_dttm;
    private ImageView detail_img;
    private void init() {
        detail_users = findViewById(R.id.detail_users);
        detail_img = findViewById(R.id.detail_img);
        detail_barcode = findViewById(R.id.detail_barcode);
        detail_mac = findViewById(R.id.detail_mac);
        detail_dttm = findViewById(R.id.detail_dttm);
    }

    /* actionbar.xml */
    private ImageView left_menu,bluetooth;
    private TextView title;
    @Override
    public void onView(View view) {
        left_menu = view.findViewById(R.id.left_menu);
        title = view.findViewById(R.id.title);
        bluetooth = view.findViewById(R.id.bluetooth);
        title.setText("모드설정");
        bluetooth.setVisibility(View.GONE);
        left_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getDetail() {
        socket = new FACNROLL_HttpSocket();
        socket.setURL("http://192.168.0.153/aiaipi/history_detail");
        socket.addText("idx",idx);
        socket.start();
        try {
            socket.join();
            String result = socket.response_data;
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
