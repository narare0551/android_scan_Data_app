package com.example.ble_gatt_example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_gatt_example.Common.Common;
import com.example.ble_gatt_example.Common.FACNROLL_HttpSocket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Common common = new Common();
    private EditText user_id,user_pw;
    private CheckBox auto_login;
    private TextView sign_in;
    private Button login;
    private String get_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        /* Top Action Bar 숨기기 */
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //아이디 저장되어있는지 체크 (저장된 파일 읽어오기)
        get_user_id = common.readFile("savelogin.txt",this);
        if(get_user_id.equals("")){
            auto_login.setChecked(false);
        }else{
            auto_login.setChecked(true);
            user_id.setText(get_user_id);
        }
    }

    /* Layout View Setting */
    private void init() {
        user_id = findViewById(R.id.user_id);
        user_pw = findViewById(R.id.user_pw);
        auto_login = findViewById(R.id.auto_login);
        sign_in = findViewById(R.id.sign_in);
        login = findViewById(R.id.login);

        sign_in.setOnClickListener(this); // 회원가입 클릭 이벤트 연결
        login.setOnClickListener(this); // 로그인 클릭 이벤트 연결
        auto_login.setOnClickListener(this); // 체크박스 클릭 이벤트 연결
    }

    /* View Click Event */
    @Override
    public void onClick(View v) {
        if(v == sign_in)
        {
            //회원가입 클릭시 실행되는 이벤트
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
            finish();
        }
        else if(v == login)
        {
            //로그인 버튼 클릭시 실행되는 이벤트
            common.user_id = user_id.getText().toString().trim();
            if(Login()) {
                Intent intent = new Intent(this, FragActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else if(v == auto_login)
        {
            //아이디 저장 체크박스 클릭시 실행되는 이벤트
            if(auto_login.isChecked())
            {
                auto_login.setChecked(true);
            }
            else
            {
                auto_login.setChecked(false);
            }
        }
    }

    /* 로그인 */
    private boolean Login(){
        boolean login_chk = false;
        FACNROLL_HttpSocket socket = new FACNROLL_HttpSocket();
        socket.setURL("http://192.168.0.153/user/login");
        socket.addText("user_id",user_id.getText().toString().trim());
        socket.addText("user_pw",user_pw.getText().toString().trim());
        try{
            socket.start();
            socket.join();
            String result = socket.response_data;
            if(result.equals("error")){
                Toast.makeText(this, "아이디와 비밀번호가 일치하지 않습니다.\n확인 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show();
                login_chk = false;
            }else{
                JSONArray array = new JSONArray(result);
                JSONObject obj = array.getJSONObject(0);
                common.user_id = obj.getString("user_id");
                common.user_name = obj.getString("name");
                if(!obj.getString("device_seri").isEmpty() || !obj.getString("device_seri").equals("")){
                    common.device_serial = obj.getString("device_seri");
                }
                login_chk = true;
                if(auto_login.isChecked()){
                    //자동로그인 체크되었을때 파일쓰기
                    common.writeFile("savelogin.txt",common.user_id +"\n"+user_pw.getText().toString().trim(),this);
                }
            }
        } catch (Exception e) {
            common.showToast(this,"로그인에 실패하였습니다. 인터넷연결을 확인해주세요.");
            e.printStackTrace();
        }
        return login_chk;
    }

    /* 기기 뒤로가기 버튼 클릭이벤트 */
    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            super.onBackPressed();
        }
    }


}
