package com.example.ble_gatt_example.Common;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FACNROLL_File_Controller {


    private String file_name;
    private File file;

    private FileOutputStream fileOutputStream;
    //private FileInputStream fileInputStream;
    public void setFileName(String file_name){
        this.file_name = file_name;
        file = new File(file_name);

        try {
            fileOutputStream = new FileOutputStream(file);
            //fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String androidExternalPicturesPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }

    public static String makePicturesFolder(String folder_name){

        File temp_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),folder_name);
        String path = temp_file.getAbsolutePath();
        if(!temp_file.exists()){
            temp_file.mkdir();
        }else{
            return path;
        }
        return path;
    }

    public void writeByte(byte[] arr,int start,int len){
        try {
            fileOutputStream.write(arr,start,len);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        if(fileOutputStream != null){
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        if(fileInputStream != null){
//            try {
//                fileInputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }






}
