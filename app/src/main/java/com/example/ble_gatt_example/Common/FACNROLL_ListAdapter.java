package com.example.ble_gatt_example.Common;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FACNROLL_ListAdapter extends BaseAdapter {
    public ArrayList data_list;
    private LayoutInflater layoutInflater;
    public Activity activity;
    public int view_layout;
    public FACNROLL_Custom_GetViewListener custom_getView;

    public FACNROLL_ListAdapter(Activity activity, int list_row_layout, FACNROLL_Custom_GetViewListener custom_getView) {
        this.data_list = new ArrayList();
        this.activity = activity;
        this.view_layout = list_row_layout;
        this.layoutInflater = LayoutInflater.from(activity);
        this.custom_getView = custom_getView;
    }
    public FACNROLL_ListAdapter(){
    }

    public FACNROLL_ListAdapter(Activity activity){
        setActivity(activity);
    }

    public void dataClear(){
        if(this.data_list != null){
            this.data_list.clear();
        }
        this.listRefresh();
    }

    public void setCustomView(FACNROLL_Custom_GetViewListener custom_getView){
        this.custom_getView = custom_getView;
    }
    public void setActivity(Activity activity){
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
    }
    public void setListRowLayoutId(int list_row_layout){
        this.view_layout = list_row_layout;
    }
    public void setDataList(ArrayList data_list){
        this.data_list = data_list;
    }
    public void addArrayList(ArrayList list){
        if(this.data_list != null){
            for(int i=0;i<list.size();i++){
                data_list.add(list.get(i));
            }
        }else{
            data_list = list;
        }
    }
    @Override
    public int getCount() {
        return this.data_list.size();
    }
    @Override
    public Object getItem(int i) {
        return this.data_list.get(i);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = this.layoutInflater.inflate(this.view_layout,null);
        return custom_getView.customView(i,view,viewGroup,this.data_list,this,this.activity,this.layoutInflater);
    }
    public void addRowItem(Object row_item){
        this.data_list.add(row_item);
    }
    public void addJSONArrayToArrayList(JSONArray array){
        this.data_list = null;
        this.data_list = JSONToArrayList(array);
        System.out.println(data_list);
    }

    public void listRefresh(){
        this.notifyDataSetChanged();
    }
    public void showList(ArrayList list){
        this.data_list = list;
        this.listRefresh();
    }
    public void showList(ArrayList list,Activity activity,int list_row_layout, FACNROLL_Custom_GetViewListener custom_getView){
        setActivity(activity);
        setCustomView(custom_getView);
        setListRowLayoutId(list_row_layout);
        showList(list);
    }

    //================================================================================
    public ArrayList<Map> JSONToArrayList(JSONArray json_arr){
        ArrayList<Map> temp_data_list = new ArrayList();
        try{
            for(int i =0;i<json_arr.length();i++){
                Map<String,Object> temp_row_data = new HashMap<>();
                JSONObject json_obj = json_arr.getJSONObject(i);

                Iterator<String> iter = json_obj.keys();
                while(iter.hasNext()){
                    String key = iter.next();
                    String value = json_obj.getString(key);
                    temp_row_data.put(key,value);
                }
                temp_data_list.add(temp_row_data);
            }
        }catch (Exception e){
            System.out.println("-----JSONToArrayList-----");
            e.printStackTrace();
        }
        return temp_data_list;
    }

    public interface FACNROLL_Custom_GetViewListener {
        View customView(int i, View view, ViewGroup viewGroup, ArrayList data_list, FACNROLL_ListAdapter facnrollBaseAdapter, Activity activity, LayoutInflater layoutInflater);
        String[] getColumns();
    }

}
