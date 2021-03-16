package com.example.ble_gatt_example.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.example.ble_gatt_example.Common.Common;
import com.example.ble_gatt_example.Common.FACNROLL_Top_Actionbar;
import com.example.ble_gatt_example.MainActivity;
import com.example.ble_gatt_example.R;
import com.example.ble_gatt_example.Settings.barcode_mode;
import com.example.ble_gatt_example.Settings.barcode_time;

public class SettingFragment extends Fragment implements FACNROLL_Top_Actionbar.IViewListener, View.OnClickListener {
    private ActionBar actionBar;
    private FACNROLL_Top_Actionbar F_actionbar;
    private Activity activity;
    private Common common = new Common();
    public SettingFragment(ActionBar supportActionBar) {
        this.actionBar = supportActionBar;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_fragment,container,false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //화면이 시작된 후 화면동작 이벤트 설정 부분은 여기서 부터 시작
        super.onActivityCreated(savedInstanceState);
        //Fragment 에서 context or activity 사용을 위해 getActivity 받아오기
        this.activity = getActivity();
        //Top Action Bar 라이브러리 연결
        F_actionbar = new FACNROLL_Top_Actionbar(activity,this.actionBar,R.layout.actionbar);
        F_actionbar.setIViewListener(this);
        F_actionbar.init();
        init();
    }

    /* actionbar.xml */
    private ImageView left_menu,bluetooth;
    private TextView title;
    @Override
    public void onView(View view) {
        //FACNROLL_Top_Actionbar 뷰 세팅 부분
        left_menu = view.findViewById(R.id.left_menu);
        title = view.findViewById(R.id.title);
        bluetooth = view.findViewById(R.id.bluetooth);
        left_menu.setVisibility(View.GONE);
        title.setText("설정");
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    private TextView user_info, device_name, device_sn, device_mac, device_battery;
    private LinearLayout device_mode, device_time;
    private Button sign_out;
    private void init() {
        user_info = activity.findViewById(R.id.user_info);
        device_name = activity.findViewById(R.id.device_name);
        device_sn = activity.findViewById(R.id.device_sn);
        device_mac = activity.findViewById(R.id.device_mac);
        device_battery = activity.findViewById(R.id.device_battery);
        device_mode = activity.findViewById(R.id.device_mode);
        device_time = activity.findViewById(R.id.device_time);
        sign_out = activity.findViewById(R.id.sign_out);

        user_info.setText(common.user_name + "(" + common.user_id + ")");
        device_name.setText("알수없음");
        device_mac.setText("알수없음");
        device_sn.setText("알수없음");
        device_battery.setText("알수없음");

        device_mode.setOnClickListener(this);
        device_time.setOnClickListener(this);
        sign_out.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == device_time)
        {
            Intent intent = new Intent(activity, barcode_time.class);
            startActivity(intent);
        }
        else if (v == device_mode)
        {
            Intent intent = new Intent(activity, barcode_mode.class);
            startActivity(intent);
        }
        else if(v == sign_out)
        {
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            activity.finish();
        }
    }
}
