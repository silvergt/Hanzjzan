package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.facebook.AccessToken;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.Data.UserInfo;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;

public class Initial extends AppCompatActivity {
    HashMap<String,String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;

        Fresco.initialize(this);

//        AccessToken.setCurrentAccessToken(null);    //TEST

        if(AccessToken.getCurrentAccessToken()==null) {
            Intent intent = new Intent(Initial.this, Login.class);
            startActivity(intent);
            finish();
        }else{
            tryLogin();
        }
    }


    private void tryLogin(){
        new Thread(()->{
            map = new HashMap<>();
            map.put("fb_key",AccessToken.getCurrentAccessToken().getUserId());
            map = ServerConnectionHelper.connect("checking account existence","login",map);

            if(map.get("signup_history")==null){
                JLog.e("Connection failed!");
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
                return;
            }

            if (map.get("signup_history").equals("TRUE")) {
                makeUserInfoAndLogin(map);
            } else if (map.get("signup_history").equals("FALSE")) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }

    private void makeUserInfoAndLogin(HashMap<String,String> map){
        StaticData.currentUser = new UserInfo(map);
        Intent intent = new Intent(getApplicationContext(),Home.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        try{
            Thread.sleep(2000);
        }catch (Exception e){e.printStackTrace();}
        super.startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {}
}
