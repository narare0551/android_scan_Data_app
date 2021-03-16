package com.example.ble_gatt_example.Common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Common {
    public static String user_id;
    public static String user_name;
    public static String device_serial;

    //파일저장
    public void writeFile(String filename, String contents, Context context)
    {
        try {
            FileOutputStream outputStream;
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(contents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //파일읽기
    public String readFile(String filename,Context context)
    {
        try{
            //파일에서 읽어오기 위한 스트림 객체 얻어오기
            FileInputStream fis = context.openFileInput(filename);
            //바이트 단위로 읽어오기 위한 배열 준디
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            //byte 배열을 문자열로 변환한다.
            String readedStr = new String(buffer);
            //읽어온 문장을 출력하기
            return readedStr;
        }catch(IOException e){
            return "";
        }
    }

    public Map<String, ArrayList> JSONToMap(String[] columns, JSONArray json_arr){
        Map<String,ArrayList> temp_data_map = new HashMap<>();
        try{
            for(int c = 0;c<columns.length;c++){
                String col = columns[c];
                ArrayList<String> col_arr = new ArrayList<>();
                for(int i=0;i<json_arr.length();i++){
                    JSONObject json_data = json_arr.getJSONObject(i);
                    String value = json_data.getString(col);
                    col_arr.add(value);
                }
                temp_data_map.put(col,col_arr);
            }
        }catch (Exception e){
            System.out.println("-----JSONToMap-----");
            e.printStackTrace();
        }
        return temp_data_map;
    }

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

    public void showToast(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public void showToast2(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    //  비트맵이미지를 비율로 위아래로 자르는 기능
    public Bitmap cutBitmap(Bitmap bitmap, int whole_y, int start_y, int view_y){
        int wid = bitmap.getWidth();
        int hei = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap,0,0,wid,hei/2);

//        int whole_percent = 100;
//        float start_percent_y = whole_percent * (float)start_y / whole_y;
//        float view_percent_y =  whole_percent * (float)view_y / whole_y;
//        //System.out.println(String.format("(%f , %f )",start_percent_y,view_percent_y));
//
//        float real_start_y = start_percent_y*hei / (float)whole_percent;
//        float realView_height = view_percent_y*hei / (float)whole_percent;
//
////        real_start_y = hei * start_y / (float)whole_y;
////        realView_height = hei * view_y / (float)whole_y;
//
//        // System.out.println("!+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++!");
//        //System.out.println(String.format("(%d , %d )%d / %d / %d / %d",wid,hei,0,(int)real_start_y,wid,(int)realView_height));
//        return Bitmap.createBitmap(bitmap,0,(int)real_start_y,wid,(int)realView_height);
    }

    //  비트맵이미지를 다운사이징하는 기능
    public Bitmap resizeBitmap(Bitmap bitmap,int resize){
        int reWidth = resize;
        float ratio = bitmap.getHeight() / (float)bitmap.getWidth();
        int targerHeight = (int)(ratio * reWidth);
        Bitmap result = Bitmap.createScaledBitmap(bitmap,reWidth,targerHeight,false);
        return result;
    }


    //실수값을를 절대값으로 바꾸는 기능
    public float floatABS(float input){
        if(input < 0){
            return input * (-1);
        }
        return input;
    }


    //  화면비율에 최적화된 카메라 해상도를 구하는 기능
    public Camera.Size getRealSize(Camera tCam, int real_width, int real_height){
        Camera.Size size = null;

        float ratio = real_width / (float)real_height;
        //float ratio = real_height / (float)real_width;
        float min_ratio = 1000000;
        float max_width = 0;
        int target_idx = -1;

        try{

            Camera.Parameters parameters = tCam.getParameters();
            List<Camera.Size> arr_size =  parameters.getSupportedPreviewSizes();
            Collections.sort(arr_size, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size size, Camera.Size t1) {
                    return   t1.height - size.height;
                }
            });
            if(arr_size.size() >0){
                for(int i=0;i<arr_size.size();i++){
                    //if(real_width >= arr_size.get(i).width && real_height >= arr_size.get(i).height)
                    {
                        float temp_ratio = ((Camera.Size)arr_size.get(i)).width / (float)((Camera.Size)arr_size.get(i)).height;
                        float diff = floatABS (ratio - temp_ratio);
                        if(diff  < min_ratio){
                            min_ratio = diff;
                            target_idx = i;
                        }
//                        int temp_width = ((Camera.Size)arr_size.get(i)).width;
//                        if(temp_width > max_width){
//                            max_width = temp_width;
//                            target_idx = i;
//                        }
                        //System.out.println(String.format(" wid: %d / hei: %d /stand : %f / ratio : %f ",arr_size.get(i).width,arr_size.get(i).height,ratio,temp_ratio));
                    }
                }
                //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ratio");
                if(target_idx != -1){
                    size = arr_size.get(target_idx);
                }
            }
        }catch(Exception e){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            e.printStackTrace();
        }
        if(size != null){
            //System.out.println(String.format("return size : %d / %d ",size.width,size.height));
        }else{
            //System.out.println(String.format("size = null"));
        }
        return size;
    }

    public Bitmap RotationBitmap(Bitmap o_bit,int angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap scale = Bitmap.createScaledBitmap(o_bit,o_bit.getWidth(),o_bit.getHeight(),true);
        return Bitmap.createBitmap(scale,0,0,o_bit.getWidth(),o_bit.getHeight(),matrix,true);
    }


    public byte[] bitmapToByteArray( Bitmap $bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        $bitmap.compress( Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray() ;
        return byteArray;
    }
    public byte[] bitmapToByteArray( Bitmap $bitmap,int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        $bitmap.compress( Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] byteArray = stream.toByteArray() ;
        return byteArray;
    }
    public JSONObject stringToJSONObject(String str_data){
        JSONObject tmep_json_object = null;
        try {
            tmep_json_object = new JSONObject(str_data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tmep_json_object;
    }
    public JSONArray stringToJSONArray(String str_data){
        JSONArray temp_json_arr = null;
        try {
            temp_json_arr = new JSONArray(str_data);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return temp_json_arr;
    }
    //2020-10-19
    public void ImageSave(String file_path,String file_name,Bitmap bitmap){
        File tempFile = new File(file_path+file_name);

        FileOutputStream out = null;
        try {
            tempFile.createNewFile();
            out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] loadFile(String file_path,String file_name){
        byte[] buffer = null;
        File file = new File(file_path+file_name);
        FileInputStream fis = null;
        long file_length = file.length();
        long readlen = 0;
        buffer = new byte[(int)file_length];
        try {
            fis = new FileInputStream(file_path+file_name);
            readlen = fis.read(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }
    public Bitmap byteArrayToBitmap(byte[] arr){
        return BitmapFactory.decodeByteArray(arr,0,arr.length);
    }

    public Bitmap loadImageFile(String file_path,String file_name){
        byte[] arr = loadFile(file_path,file_name);
        return byteArrayToBitmap(arr);
    }

    public String getCurrentTime(){
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    //본인 디바이스의 MAC 주소 얻어오기.
    //안드로이드 Q 버전 부터 MAC 주소는 02:00:00:00:00 으로 고정되어 나옴
    public String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return "";
    }

    public static boolean fileWritePermission(Activity activity){
        final int MY_PERMISSION_REQUEST_CODE = 10001;
        int APIVersion = Build.VERSION.SDK_INT;
        //ActivityCompat.requestPermissions((Activity)activity,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CODE);
        if(APIVersion >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity)activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_CODE);
                return false;
            }else{
                return true;
            }
        }else{
            return true;
        }
    }

    public static boolean fileReadPermission(Activity activity){
        final int MY_PERMISSION_REQUEST_CODE = 10002;
        int APIVersion = Build.VERSION.SDK_INT;
        //ActivityCompat.requestPermissions((Activity)activity,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CODE);
        if(APIVersion >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity)activity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_CODE);
                return false;
            }else{
                return true;
            }
        }else{
            return true;
        }
    }

    public static String mode;
    public static FACNROLLBluetooth facnrollBluetooth;




    public String getBarcodeText(String barcode){
        String type = "";
        String code = "";

        if(barcode.length() >7){
            if(barcode.charAt(0) == '+' && barcode.charAt(4) == '+' && barcode.charAt(6) == '+' ){
                type = barcode.substring(1,3);
                code = barcode.charAt(5)+"";
                barcode = barcode.substring(7);
            }
        }
        return barcode;
    }
    public String getBarcodeType(String barcode){
        String type = "";
        String code = "";
        if(barcode.length() >7){
            if(barcode.charAt(0) == '+' && barcode.charAt(4) == '+' && barcode.charAt(6) == '+' ){
                type = barcode.substring(1,3);
                code = barcode.charAt(5)+"";
                barcode = barcode.substring(7);
            }
        }
        return type;
    }
    public String getBarcodeCode(String barcode){
        String type = "";
        String code = "";
        if(barcode.length() >7){
            if(barcode.charAt(0) == '+' && barcode.charAt(4) == '+' && barcode.charAt(6) == '+' ){
                type = barcode.substring(1,3);
                code = barcode.charAt(5)+"";
                barcode = barcode.substring(7);
            }
        }
        return code;
    }

    public void fileSave(byte[] arr,String fileName){
        //String make_folder_path = FACNROLL_File_Controller.androidExternalPath()+"/FACNROLL_FILE";
        String absolute_path = FACNROLL_File_Controller.makePicturesFolder("/FACNROLL");
        System.out.println(">>>>>>>>>>>>>>>>>>>>> file : "+"폴더 생성 완료 : "+absolute_path);

        FACNROLL_File_Controller fileController = new FACNROLL_File_Controller();
        fileController.setFileName(absolute_path+"/"+fileName);
        System.out.println(arr.length);
        fileController.writeByte(arr,0,arr.length);
        fileController.close();
    }

    public void save_log(String error){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        long now_t = System.currentTimeMillis();
        Date now = new Date(now_t);
        String date = format.format(now);
        File log = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/Exception_log_"+date+".txt");
        if(!log.exists()){
            try{
                log.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try{
            BufferedWriter buf = new BufferedWriter(new FileWriter(log,true));
            buf.append(error);
            buf.newLine();
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //태블릿인지 여부
    public boolean IsTablet(Context context) {
        //화면 사이즈 종류 구하기
        int screenSizeType = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if(screenSizeType==Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSizeType==Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return true;
        }
        return false;
    }

    public static String MAC_ADDR;
    public final ArrayList messageList = new ArrayList();
    public static int count = 0;
    private static Common instance;
    public static Common getInstance(){
        if(instance == null){
            instance = new Common();
        }
        return instance;
    }
}
