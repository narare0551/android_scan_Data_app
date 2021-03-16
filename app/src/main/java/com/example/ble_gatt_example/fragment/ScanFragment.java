package com.example.ble_gatt_example.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ble_gatt_example.Common.BleGattServer;
import com.example.ble_gatt_example.Common.CentralCallback;
import com.example.ble_gatt_example.Common.Common;
import com.example.ble_gatt_example.Common.FACNROLL_HttpSocket;
import com.example.ble_gatt_example.Common.FACNROLL_Top_Actionbar;
import com.example.ble_gatt_example.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.ble_gatt_example.Common.Constants.REQUEST_ENABLE_BT;
import static com.example.ble_gatt_example.Common.Constants.REQUEST_FINE_LOCATION;

public class ScanFragment extends Fragment implements FACNROLL_Top_Actionbar.IViewListener, View.OnKeyListener {
    private FACNROLL_Top_Actionbar F_actionbar;
    private ActionBar actionBar;
    private Activity activity;
    private Common common = new Common();
    public ScanFragment(ActionBar supportActionBar) {
        this.actionBar = supportActionBar;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //화면이 시작 되었을때 보이게할 Layout 을 inflater 로 연결해주는 부분
        return inflater.inflate(R.layout.scan_fragment,container,false);
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

    private EditText scan_text;
    private TextView scan_barcode, barcode_history;
    private ImageView scan_img;
    private Handler handler;
    private void init() {
        scan_text = activity.findViewById(R.id.scan_text);
        scan_barcode = activity.findViewById(R.id.scan_barcode);
        scan_img = activity.findViewById(R.id.scan_img);
        barcode_history = activity.findViewById(R.id.barcode_history);

        barcode_history.setMovementMethod(new ScrollingMovementMethod());
        scan_text.setOnKeyListener(this);
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
        title.setText("스캔");
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    /* 스캔시 엔터값 체크 해서 서버에 데이터 저장 */
    String old_barcode;
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v == scan_text && keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
            if(!barcode_history.getText().toString().equals("") || !barcode_history.getText().toString().equals("null")) {
                 old_barcode = barcode_history.getText().toString().trim();
            }else{
                 old_barcode = null;
            }
            String barcode = scan_text.getText().toString().trim();
            scan_barcode.setText(barcode);
            barcode_history.setText(old_barcode + "\n" + barcode);
            final int scrollAmount = barcode_history.getLayout().getLineTop(barcode_history.getLineCount()) - barcode_history.getHeight() + 15;
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                barcode_history.scrollTo(0, scrollAmount);
            }
            else {
                barcode_history.scrollTo(0, 0);
            }
            save_server();
            handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    scan_text.setText(null);
                }
            });
        }
        return false;
    }

    private void save_server(){
        String barcode = scan_barcode.getText().toString().trim();
        String reg_user_id = common.user_id;
        FACNROLL_HttpSocket socket = new FACNROLL_HttpSocket();
        socket.setURL("http://192.168.0.52/aiapi/save_data");
        socket.addText("barcode",barcode);
        socket.addText("reg_user_id",reg_user_id);
        socket.start();
        try {
            socket.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
