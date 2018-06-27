package kotel.hanzan.function;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import kotel.hanzan.R;
import kotel.hanzan.kakao.GlobalApplication;

public class ServerConnectionHelper {
    private final static String REGULAR_SERVER = "elbhanjan-1863892783.ap-northeast-2.elb.amazonaws.com";
    private final static String TEST_SERVER = "ec2-52-78-207-207.ap-northeast-2.compute.amazonaws.com";

    private final static String serverIP = REGULAR_SERVER;
    private int connectTimeout=10000;

    public static HashMap<String,String> connect(String recognizer, String phpAddress, HashMap<String,String> data, String imageName, byte[] image){
        return new ServerConnectionHelper().interactWithServer(recognizer,phpAddress,data,imageName,image);
    }

    public static HashMap<String,String> connect(String recognizer, String phpAddress, HashMap<String,String> data){
        return new ServerConnectionHelper().interactWithServer(recognizer,phpAddress,data,null,null);
    }

    private HashMap<String,String> interactWithServer(String recognizer,String phpAddress,HashMap<String,String> data, @Nullable String imageName, @Nullable byte[] image){
        final HashMap<String, String> returnValue = new HashMap<>();
        Thread th = new Thread(() -> {
            JLog.v("<"+recognizer+">");
            String boundary = "-----KOTEL-----" + System.currentTimeMillis();
            try {
                URL url = new URL("http://" + serverIP +"/"+phpAddress+".php");
//                URL url = new URL(serverIP +"/"+phpAddress+".php");
                HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                if (uc != null) {
                    uc.setConnectTimeout(connectTimeout);
                    uc.setRequestMethod("POST");
                    uc.setDoOutput(true);
                    uc.setDoInput(true);
                    uc.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    try {
                        uc.connect();
                    }catch (Exception e){
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(()->{
                            Toast.makeText(GlobalApplication.getGlobalApplicationContext().getApplicationContext(),
                                    GlobalApplication.getGlobalApplicationContext().getString(R.string.networkFailure),Toast.LENGTH_SHORT).show();
                        });
                    }

                    MultipartEntityBuilder MEB = MultipartEntityBuilder.create();
                    MEB.setBoundary(boundary);
                    MEB.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    if(image!=null) {
                        JLog.v("image size : ", image.length);
                        MEB.addBinaryBody(imageName, image, ContentType.create("image/png"), "imagefile.png");
                    }
                    Iterator<String> keySet = data.keySet().iterator();
                    for(int i=0;i<data.size();i++){
                        String key=keySet.next();
                        MEB.addTextBody(key, data.get(key), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }

                    HttpEntity entity = MEB.build();
                    entity.writeTo(uc.getOutputStream());

                    BufferedReader BR = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    String temp;
                    while ((temp = BR.readLine()) != null) {
                        String[] map = temp.split(" ",2);
                        if(map.length!=2){continue;}
                        JLog.v("<"+recognizer+"> returned ", map[0] + " -> " + map[1]);
                        returnValue.put(map[0],map[1]);
                    }

                    BR.close();
                    uc.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        th.start();
        try {
            th.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }


    public static boolean isRegularServer(){
        return serverIP.equals(REGULAR_SERVER);
    }
}
