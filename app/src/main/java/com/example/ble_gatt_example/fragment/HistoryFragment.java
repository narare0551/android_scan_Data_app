package com.example.ble_gatt_example.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.example.ble_gatt_example.Common.Common;
import com.example.ble_gatt_example.Common.FACNROLL_HttpSocket;
import com.example.ble_gatt_example.Common.FACNROLL_ListAdapter;
import com.example.ble_gatt_example.Common.FACNROLL_Top_Actionbar;
import com.example.ble_gatt_example.HistoryDetail;
import com.example.ble_gatt_example.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryFragment extends Fragment implements FACNROLL_Top_Actionbar.IViewListener, DatePickerDialog.OnDateSetListener, View.OnClickListener, FACNROLL_ListAdapter.FACNROLL_Custom_GetViewListener {
    private Activity activity;
    private ActionBar actionBar;
    private FACNROLL_Top_Actionbar F_actionbar;
    private DatePickerDialog.OnDateSetListener listener = this;//달력리스너
    private DatePickerDialog.OnDateSetListener listener2 = this;//달력리스너

    public HistoryFragment(ActionBar supportActionBar) {
        this.actionBar = supportActionBar;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_fragment,container,false);
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
        get_list("", "");
    }

    /* actionbar.xml */
    private ImageView left_menu,bluetooth;
    private TextView title;
    private int check_date_picker=0; //어떤 데이터 피커인지 체크
    @Override
    public void onView(View view) {
        //FACNROLL_Top_Actionbar 뷰 세팅 부분
        left_menu = view.findViewById(R.id.left_menu);
        title = view.findViewById(R.id.title);
        bluetooth = view.findViewById(R.id.bluetooth);
        left_menu.setVisibility(View.GONE);
        title.setText("히스토리");
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    private TextView start_dttm, end_dttm;
    private ImageView btn_search;
    private ListView list;
    private ArrayList<DataBean> data;
    private FACNROLL_ListAdapter adapter;
    private void init() {
        start_dttm = activity.findViewById(R.id.start_dttm);
        end_dttm = activity.findViewById(R.id.end_dttm);
        btn_search = activity.findViewById(R.id.btn_search);
        list = activity.findViewById(R.id.list);

        start_dttm.setOnClickListener(this);
        end_dttm.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        adapter = new FACNROLL_ListAdapter(activity, R.layout.history_rows,this);
        data = new ArrayList();
        adapter.setDataList(data);
        list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v == start_dttm)
        {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(activity, listener, calendar.get(calendar.YEAR),  calendar.get(calendar.MONTH),  calendar.get(calendar.DAY_OF_MONTH));
            dialog.show();
            check_date_picker = 1;
        }
        else if(v == end_dttm)
        {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(activity, listener2, calendar.get(calendar.YEAR),  calendar.get(calendar.MONTH),  calendar.get(calendar.DAY_OF_MONTH));
            if(start_dttm.getText().toString() != ""){ //시작일 이후 일자만 선택가능하도록 설정
                try {
                    String st_time = start_dttm.getText().toString();
                    java.sql.Date a = java.sql.Date.valueOf(st_time);
                    dialog.getDatePicker().setMinDate(a.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dialog.show();
            check_date_picker = 2;
        }
        else if(v == btn_search)
        {
            //검색
            get_list(start_dttm.getText().toString().trim(), end_dttm.getText().toString().trim());
        }
    }


    /* 데이터피커 설정 */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String month_value="",day_value = "";
        month++;
        if(month < 10){
            month_value = "0"+month;
        }else{
            month_value = month+"";
        }
        if(dayOfMonth < 10){
            day_value = "0"+dayOfMonth;
        }else{
            day_value =  dayOfMonth+"";
        }

        if(check_date_picker ==1){
            this.start_dttm.setText(year+"-"+month_value+"-"+day_value);
        }
        else if(check_date_picker ==2){
            this.end_dttm.setText(year+"-"+month_value+"-"+day_value);
        }
    }

    private void get_list(String start_dttms, String end_dttms) {
        FACNROLL_HttpSocket socket = new FACNROLL_HttpSocket();
        socket.setURL("http://192.168.0.153/aiapi/history");
        if(!start_dttms.equals("")) {
            socket.addText("start_dttm", start_dttms);
        }
        if(!end_dttms.equals("")) {
            socket.addText("end_dttm", end_dttms);
        }
        socket.start();
        try {
            socket.join();
            String result = socket.response_data;
            if(!result.equals("[]")){
                data.clear();
                JSONArray array = new JSONArray(result);
                for(int i=0; i<array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    DataBean bean = new DataBean();
                    bean.idx = obj.getString("idx");
                    bean.barcode = obj.getString("barcode");
                    bean.device_mac = obj.getString("device_mac_addr");
                    bean.reg_dttm = obj.getString("reg_dttm");
                    bean.user_name = obj.getString("name");
                    bean.reg_user_id = obj.getString("reg_user_id");
                    this.data.add(bean);
                }
                adapter.listRefresh();
            }else{
                Common common = new Common();
                common.showToast(activity,"데이터가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View customView(int i, View view, ViewGroup viewGroup, ArrayList data_list, FACNROLL_ListAdapter facnrollBaseAdapter, Activity activity, LayoutInflater layoutInflater) {
        TextView users, mac_addr, barcode, dttm, idx;
        LinearLayout select_layout;

        users = view.findViewById(R.id.users);
        mac_addr = view.findViewById(R.id.mac_addr);
        barcode = view.findViewById(R.id.barcode);
        dttm = view.findViewById(R.id.dttm);
        idx = view.findViewById(R.id.idx);
        select_layout = view.findViewById(R.id.select_layout);

        ArrayList<DataBean> array = data_list;
        DataBean bean = array.get(i);
        users.setText("등록자 : " + bean.user_name);
        if(bean.device_mac.isEmpty() || bean.device_mac.equals("null")) {
            mac_addr.setText("Mac : No Data");
        }else{
            mac_addr.setText("Mac : "+bean.device_mac);
        }
        barcode.setText("바코드 : " + bean.barcode);
        dttm.setText(bean.reg_dttm);
        idx.setText(bean.idx);

        select_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = bean.idx;
                Intent intent = new Intent(activity, HistoryDetail.class);
                intent.putExtra("idx",idx);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public String[] getColumns() {
        return new String[0];
    }

    public class DataBean{
        public String idx;
        public String user_name;
        public String device_mac;
        public String barcode;
        public String reg_dttm;
        public String reg_user_id;
    }
}
