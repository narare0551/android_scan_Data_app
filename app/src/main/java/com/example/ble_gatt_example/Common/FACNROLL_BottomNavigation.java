package com.example.ble_gatt_example.Common;

import android.app.Activity;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class FACNROLL_BottomNavigation implements  BottomNavigationView.OnNavigationItemSelectedListener{
    private int frame_layout_id;
    private BottomNavigationView bottom_bar;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Activity activity;

    public FACNROLL_BottomNavigation(){
    }
    public FACNROLL_BottomNavigation(FragmentManager fragmentManager, BottomNavigationView bottom_bar, int frame_layout_id){
        setting( fragmentManager,  bottom_bar, frame_layout_id);
    }

    public void setItemList( ArrayList<BottomNavItem> item_list){
        this.item_list =item_list;
    }


    public void setting(FragmentManager fragmentManager, BottomNavigationView bottom_bar, int frame_layout_id){
        this.bottom_bar = bottom_bar;
        this.fragmentManager = fragmentManager;
        fragmentTransaction = fragmentManager.beginTransaction();
        this.frame_layout_id = frame_layout_id;
        bottom_bar.setOnNavigationItemSelectedListener(this);
    }

    public void start(BottomNavItem item){
        fragmentTransaction.replace(frame_layout_id,item.fragment).commitAllowingStateLoss();
    }

    public ArrayList<BottomNavItem> item_list;
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        fragmentTransaction = fragmentManager.beginTransaction();

        for(int i=0;i<item_list.size();i++){
            BottomNavItem row_item  = item_list.get(i);
            if(row_item.item_id == item.getItemId()){
                fragmentTransaction.replace(frame_layout_id,row_item.fragment).commitAllowingStateLoss();
                return true;
            }
        }
        return false;
    }

    public static class BottomNavItem{
        public BottomNavItem(int item_id, Fragment fragment){
            this.item_id = item_id;
            this.fragment = fragment;
        }

        public BottomNavItem(){
        }

        public int item_id;
        public Fragment fragment;
    }
}
