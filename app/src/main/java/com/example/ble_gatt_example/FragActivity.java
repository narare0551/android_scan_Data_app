package com.example.ble_gatt_example;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.ble_gatt_example.Common.FACNROLL_BottomNavigation;
import com.example.ble_gatt_example.fragment.DashBoardFragment;
import com.example.ble_gatt_example.fragment.HistoryFragment;
import com.example.ble_gatt_example.fragment.ScanFragment;
import com.example.ble_gatt_example.fragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class FragActivity extends AppCompatActivity  {
    private BottomNavigationView bottomNavigationView;
    private ScanFragment scanFragment;
    private HistoryFragment historyFragment;
    private SettingFragment settingFragment;
    private DashBoardFragment dashBoardFragment;
    private boolean is_check = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_activity);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent;
        if (mBluetoothAdapter.isEnabled()) {
            // 블루투스 관련 실행 진행
        } else {
            // 블루투스 활성화 하도록
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1: // 25번 줄에서 requestCode 값 1
                if(resultCode==RESULT_OK){
                    // 블루투스 기능을 켰을 때
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /* 화면의 포커스가 변경될 경우 실행되는 함수 */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!is_check)
        {
            setBottomNav(); // 바텀 네비게이션 뷰 시작
            is_check = true;
        }
    }

    private void setBottomNav() {
        try{
            /* Fragment 연결 및 TopActionbar 연동 */
            scanFragment = new ScanFragment(getSupportActionBar());
            historyFragment = new HistoryFragment(getSupportActionBar());
            settingFragment = new SettingFragment(getSupportActionBar());
            dashBoardFragment = new DashBoardFragment(getSupportActionBar());
            /* Bottom Navigation Bar */
            bottomNavigationView = findViewById(R.id.bottom_bar);
            /* 바텀 네비게이션 뷰에 메뉴 아이템 지정을 위한 ArrayList 생성 */
            ArrayList<FACNROLL_BottomNavigation.BottomNavItem> menu_list = new ArrayList<>();
            /* 바텀 네비게이션 Fragment 연결 */
            FACNROLL_BottomNavigation.BottomNavItem scan = new FACNROLL_BottomNavigation.BottomNavItem(R.id.scan, scanFragment);
            FACNROLL_BottomNavigation.BottomNavItem history = new FACNROLL_BottomNavigation.BottomNavItem(R.id.history, historyFragment);
            FACNROLL_BottomNavigation.BottomNavItem setting = new FACNROLL_BottomNavigation.BottomNavItem(R.id.setting, settingFragment);
            FACNROLL_BottomNavigation.BottomNavItem dashboard = new FACNROLL_BottomNavigation.BottomNavItem(R.id.dashboard, dashBoardFragment);
            /* 연결된 메뉴 아이템들을 ArrayList 에 추가 */
            menu_list.add(scan);
            menu_list.add(history);
            menu_list.add(setting);
            menu_list.add(dashboard);
            /* 바텀 네비게이션 사용 선언 */
            FACNROLL_BottomNavigation navigation = new FACNROLL_BottomNavigation();
            /* setting (FragmentManager(), 바텀네비게이션뷰(레이아웃에 생성한 뷰), Fragment 를 보여줄 프레임레이아웃 */
            navigation.setting(getSupportFragmentManager(), bottomNavigationView, R.id.frame_layout);
            /* 바텀 네비게이션 메뉴 지정 */
            navigation.setItemList(menu_list);
            /* 처음 시작한 메뉴 설정 */
            navigation.start(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
