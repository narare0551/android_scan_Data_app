package com.example.ble_gatt_example.Settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ble_gatt_example.R;
import com.example.ble_gatt_example.Common.FACNROLL_Top_Actionbar;

public class barcode_time extends AppCompatActivity implements FACNROLL_Top_Actionbar.IViewListener{
    private ActionBar actionBar;
    private FACNROLL_Top_Actionbar facnroll_top_actionbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_time);actionBar = getSupportActionBar();
        facnroll_top_actionbar = new FACNROLL_Top_Actionbar(this,this.actionBar,R.layout.actionbar);
        facnroll_top_actionbar.setIViewListener(this);
        facnroll_top_actionbar.init();
    }

    private ImageView left_menu,bluetooth;
    private TextView title;
    @Override
    public void onView(View view) {
        left_menu = view.findViewById(R.id.left_menu);
        title = view.findViewById(R.id.title);
        bluetooth = view.findViewById(R.id.bluetooth);
        title.setText("시간설정");
        bluetooth.setVisibility(View.GONE);
        left_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
