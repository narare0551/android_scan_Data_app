package com.example.ble_gatt_example.Common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class FACNROLL_HttpSocket extends Thread{
    public String url = "";
    public String encoding = "UTF-8";

    public FACNROLL_HttpSocket(){
    }

    public FACNROLL_HttpSocket(String url, String encoding){
        this.url = url;
        this.encoding = encoding;
    }
    public void setURL(String _url){
        url = _url;
    }

    private MultipartEntityBuilder request = MultipartEntityBuilder.create();
    //private ArrayList<BasicNameValuePair> request = new ArrayList<>();
    public void setContextType(ContentType type){
        request.seContentType(type);
    }
    public void addText(String key,String value){
        //request.add(new BasicNameValuePair(key,value));
        try {
//            request.addTextBody(URLDecoder.decode(key,encoding),URLDecoder.decode(value,encoding));
            request.addTextBody(key,value, ContentType.create("multipart/form-data","UTF-8"));
            //한글 데이터 안깨지도록 UTF-8 설정
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addText(String[] key_arr,String[] value_arr){
        for(int i=0;i<key_arr.length;i++){
            //request.add(new BasicNameValuePair(key_arr[i],value_arr[i]));
            try {
                //request.addTextBody(URLDecoder.decode(key_arr[i],encoding),URLDecoder.decode(value_arr[i],encoding));
                addText(key_arr[i],value_arr[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addFile(String file_path,String file_name){
        try {
            request.addPart(file_name,new FileBody(new File(file_path + file_name), ContentType.create("application/octet-stream"), URLEncoder.encode(file_name,encoding)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addFile(String[] file_path,String[] file_name){
        for(int i=0;i<file_path.length;i++){
            this.addFile(file_path[i],file_name[i]);
        }
    }

    public void addFile(String file_name,byte[] byte_arr){
        request.addBinaryBody(file_name,byte_arr);
    }

    public void addFile(String name ,String file_name,byte[] byte_arr){
        request.addBinaryBody(name,byte_arr, ContentType.parse("multipart/form-data"),file_name);
    }

    public void addFile(String[] file_name_arr,byte[][] file_arr){
        for(int i=0;i<file_name_arr.length;i++){
            addFile(file_name_arr[i],file_arr[i]);
        }
    }

    private int mili_sec = 10*1000;
    public void setTimeout(int mili_sec){
        this.mili_sec = mili_sec;
    }

    private Map<String,String> header = new HashMap<>();
    public void addPostHeader(String key,String value){
        header.put(key,value);
    }

    public String excute(){
        String response_text = "";
        if(this.url == null ||this.url.isEmpty()){
            return null;
        }

        if(this.encoding== null || this.encoding.isEmpty()){
            return null;
        }

        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.protocol.expect-continue", false);
        client.getParams().setParameter("http.connection.timeout", mili_sec );// 원격 호스트와 연결을 설정하는 시간
        client.getParams().setParameter("http.socket.timeout",  mili_sec);//데이터를 기다리는 시간
        client.getParams().setParameter("http.connection-manager.timeout",  mili_sec);// 연결 및 소켓 시간 초과
        client.getParams().setParameter("http.protocol.head-body-timeout",  mili_sec);
        HttpPost post = new HttpPost(this.url);

        post.addHeader("enctype","multipart/form-data");


        //if(header.size() 0)
        Iterator<String> iterator = header.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String value = header.get(key);
            post.addHeader(key,value);
        }

        try {
            //post.setEntity(new UrlEncodedFormEntity(request,encoding));
            request.setCharset(Charset.forName(encoding));
            post.setEntity(request.build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = client.execute(post);

            if(response.getStatusLine().getStatusCode() == 200){
                HttpEntity resEntityGet = response.getEntity();
                response_text = EntityUtils.toString(resEntityGet,encoding);
            }else{
                return response.getStatusLine().getStatusCode()+":error";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response_text;
    }
    public String response_data = "";
    @Override
    public void run() {
        response_data = this.excute();
    }
    public String getResponseData(){
        return response_data;
    }
}
