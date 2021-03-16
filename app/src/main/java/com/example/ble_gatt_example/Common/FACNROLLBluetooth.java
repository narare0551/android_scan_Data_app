package com.example.ble_gatt_example.Common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class FACNROLLBluetooth extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    Handler handler = new Handler();
    byte[] barcode;
    byte[] tg;
    byte[] image;
    private BluetoothSocket bluetoothSocket; // 클라이언트 소켓
    private InputStream mmInStream; // 입력 스트림
    private OutputStream mmOutStream; // 출력 스트림

    public Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    public BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    public static String connect_device_name = "";
    public static String connect_status = "disconnect";
    @SuppressLint("MissingPermission")
    public FACNROLLBluetooth(Context context)
    {
        this.context = context;
        //        블루투스 사용여부 확인 -
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (!ap.isEnabled()) {
//            ap.enable();
//        }
        // 블루투스 활성화하기
        if(bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            // 여기에 처리 할 코드를 작성하세요.
//            finish();
        }
        else { // 디바이스가 블루투스를 지원 할 때
            if(bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
//                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            }
            else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택한 값이 onActivityResult 함수에서 콜백된다.
                //startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    }


    public void setContext(Context context){
        this.context = context;
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        int pariedDeviceCount = devices.size();

        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                if(bluetoothDevice.getName().contains("G2-")) {
                    list.add(bluetoothDevice.getName());
                }
            }
            list.add("취소");
            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);
            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void connectDevice(String deviceName) {
        if(!deviceName.equals("취소"))
        {
            System.out.println("#####");
            // 페어링 된 디바이스들을 모두 탐색
            for (BluetoothDevice tempDevice : devices) {
                // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
                if (deviceName.equals(tempDevice.getName())) {
                    bluetoothDevice = tempDevice;
                    connect_device_name = tempDevice.getName();
                    break;
                }
            }
            System.out.println(bluetoothDevice);
            // UUID 생성
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.close();
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                System.out.println(bluetoothSocket.isConnected());
                // 데이터 송,수신 스트림을 얻어옵니다.
                SocketThread(bluetoothSocket);
                Runnable r = this;
                Thread t = new Thread(r);
                t.start();
                // 데이터 수신 함수 호출
                //receiveData();
            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String error = sw.toString();
                Common common = new Common();
                common.save_log(error);
            }
        }
    }

    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }
    public void SocketThread(BluetoothSocket socket) {
        bluetoothSocket = socket;

        // 입력 스트림과 출력 스트림을 구한다
        try {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        } catch (IOException e) {
            //showMessage("Get Stream error");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTG()
    {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        String formatDate = sdfNow.format(date);
        @SuppressLint("MissingPermission") String[] barcodeinfo = {
                bluetoothDevice.getAddress() + "/" + bluetoothDevice.getName(),
                formatDate
        };
        mBarcodeInfoListener.onBarcodeInfoEventListener(barcodeinfo);
    }
    private void getBarcode(final String barcode)
    {
        mBarcodeListener.onBarcodeEventListener(barcode);
    }
    private void getImage(byte[] image_bytes)
    {
        //이미지출력
        Bitmap bmp = BitmapFactory.decodeByteArray(image_bytes, 0, image_bytes.length);
        mImageListener.onImageEventListener(bmp);
    }
    private Handler handler1 = new Handler();
    // 소켓에서 수신된 데이터를 화면에 표시한다
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"MissingPermission", "LongLogTag"})
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        readBuffer = new byte[100000];
        int total_length = 0;
        while (true) {
            if(bluetoothSocket == null){
                if(!connect_status.equals("disconnect"))
                {
                    mConnectStatusChangeEventListener.onConnectStatusChangeEventListener("disconnect");
                }
                connect_status = "disconnect";
                continue;
            }
            else if(bluetoothSocket.isConnected())
            {
                if(connect_status.equals("disconnect"))
                {
                    handler1.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mConnectStatusChangeEventListener != null){
                                mConnectStatusChangeEventListener.onConnectStatusChangeEventListener(bluetoothDevice.getName());
                            }
                        }
                    });
                    connect_status = bluetoothDevice.getName();
                }
            }
            else
            {
                if(!connect_status.equals("disconnect"))
                {
                    if(mConnectStatusChangeEventListener!= null){
                        mConnectStatusChangeEventListener.onConnectStatusChangeEventListener("disconnect");
                        connect_status = "disconnect";
                        break;
                    }
                }
                connect_status = "disconnect";
            }
            try {
                // 데이터를 수신했는지 확인합니다.
                int byteAvailable = mmInStream.available();
                // 데이터가 수신 된 경우
                if (byteAvailable > 0) {
                    // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                    check_start = 0;
                    OnReceiveData2(byteAvailable);
                    total_length = byteAvailable;
                } else {
                    if (readBuffer[0] != 0x00) {
                        int s_idx = 0;
                        for (int i = 0; i < readBuffer.length; i++) {
                            byte tempByte = readBuffer[i];
                            if (tempByte == 0x0d) {
                                byte[] tempBytes = Arrays.copyOfRange(readBuffer, s_idx, i);
                                String temp_str = "";
                                try {
                                    temp_str = new String(tempBytes, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (temp_str.contains("/TG/")) {
                                    getTG();
                                } else if (temp_str.contains("/BC/")) {
                                    getBarcode(temp_str.replace("/BC/", "").trim());
                                }
                                s_idx = i + 1;
                            } else if (i > 4 && readBuffer[i - 1] == 0x2f && readBuffer[i - 2] == 0x47 && readBuffer[i - 3] == 0x49 && readBuffer[i - 1] == 0x2f) {
//                                byte[] tempBytes = Arrays.copyOfRange(readBuffer, i, readBuffer.length - 1);
                                byte[] tempBytes = Arrays.copyOfRange(readBuffer, i, readBufferPosition);
                                byte temp1 = tempBytes[tempBytes.length-1];
                                byte temp2 = tempBytes[tempBytes.length-2];
                                byte temp3 = tempBytes[tempBytes.length-3];
                                byte temp4 = tempBytes[tempBytes.length-4];
                                byte temp5 = tempBytes[tempBytes.length-5];
                                getImage(tempBytes);
                                readBuffer = new byte[100000];
                                readBufferPosition = 0;
                            }
                        }

                    }
                }
            }
            catch(Exception e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsStrting = sw.toString();
                Common common = new Common();
                common.save_log(exceptionAsStrting);
                Log.e("StackTraceExampleActivity", exceptionAsStrting);
            }
            try {
                // 1초마다 받아옴
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Thread.currentThread().interrupt();
                //break;
            }
        }
    }
    public static int check_start = 0;
    public void OnReceiveData(int byteAvailable ) throws IOException {
        byte[] bytes = new byte[byteAvailable];

        mmInStream.read(bytes);
        // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
        for (int i = 0; i < byteAvailable; i++) {
            byte tempByte = bytes[i];
            if (tempByte == 0x0d) {
                //trriger
                if (readBuffer[0] == 0x2f && readBuffer[1] == 0x54 && readBuffer[2] == 0x47 && readBuffer[3] == 0x2f) {

                    readBufferPosition = 0;
                    readBuffer = new byte[100000];
                    handler.post(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            // 텍스트 뷰에 출력
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formatDate = sdfNow.format(date);
                            @SuppressLint("MissingPermission") String[] barcodeinfo = {
                                    bluetoothDevice.getAddress() + "/" + bluetoothDevice.getName(),
                                    formatDate
                            };
                            mBarcodeInfoListener.onBarcodeInfoEventListener(barcodeinfo);
                        }
                    });
                }
                //trriger
                else if (readBuffer[0] == 0x2f && readBuffer[1] == 0x42 && readBuffer[2] == 0x43 && readBuffer[3] == 0x2f) {
                    //check_start++;
                    barcode = new byte[readBufferPosition];
                    System.arraycopy(readBuffer, 0, barcode, 0, barcode.length);
                    readBufferPosition = 0;
                    readBuffer = new byte[100000];
                    //바코드출력

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 텍스트 뷰에 출력
                            String text = "";
                            try {
                                text = new String(barcode, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            mBarcodeListener.onBarcodeEventListener(text);
                        }
                    });
                }
                else if (readBuffer[readBuffer.length-1] == 0xd9 && readBuffer[readBuffer.length-2] == 0xff) {
                    //check_start++;
                    image = new byte[readBufferPosition];
                    System.arraycopy(readBuffer, 0, image, 0, image.length);
                    readBufferPosition = 0;
                    readBuffer = new byte[100000];
                    //이미지출력
                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    //이미지이벤트호출
                    mImageListener.onImageEventListener(bmp);
                }
            }
            else {
                readBuffer[readBufferPosition++] = tempByte;
            }
        }
    }
    public void OnReceiveData2(int byteAvailable ) throws IOException {
        byte[] bytes = new byte[byteAvailable];

        mmInStream.read(bytes);
        // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
        for (int i = 0; i < byteAvailable; i++) {
            byte tempByte = bytes[i];
            readBuffer[readBufferPosition++] = tempByte;
        }
    }
    public interface BarcodeEventListener {
        void onBarcodeEventListener(String barcode);
    }
    public interface BarcodeInfoEventListener {
        void onBarcodeInfoEventListener(String[] barcodeinfo);
    }
    public interface ImageEventListener {
        void onImageEventListener(Bitmap bitmap);
    }
    public interface ConnectStatusChangeEventListener {
        void onConnectStatusChangeEventListener(String status);
    }
    public BarcodeInfoEventListener mBarcodeInfoListener;
    public BarcodeEventListener mBarcodeListener;
    public ImageEventListener mImageListener;
    public ConnectStatusChangeEventListener mConnectStatusChangeEventListener;
    public void setOnImageListener(ImageEventListener listener){
        mImageListener = listener;
    }
    public void setOnBarcodeListener(BarcodeEventListener listener){
        mBarcodeListener = listener;
    }
    public void setOnBarcodeInfoListener(BarcodeInfoEventListener listener){
        mBarcodeInfoListener = listener;
    }
    public void setOnConnectStatusChangeEventListener(ConnectStatusChangeEventListener listener){
        mConnectStatusChangeEventListener = listener;
    }




    // 데이터를 소켓으로 전송한다
    public void write(String strBuf) {
        try {
            // 출력 스트림에 데이터를 저장한다
            byte[] buffer = strBuf.getBytes();
            mmOutStream.write(buffer);
            //showMessage("Send: " + strBuf);
        } catch (IOException e) {
            //showMessage("Socket write error");
        }
    }


}
