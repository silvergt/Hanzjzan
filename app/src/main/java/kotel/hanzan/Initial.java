package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.facebook.AccessToken;
import com.facebook.drawee.backends.pipeline.Fresco;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.Data.UserInfo;

public class Initial extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;

        Fresco.initialize(this);

        StaticData.currentUser = new UserInfo();

//        AccessToken.setCurrentAccessToken(null);    //TEST

        if(AccessToken.getCurrentAccessToken()==null) {
            Intent intent = new Intent(Initial.this, Login.class);
            startActivity(intent);
            finish();
        }else{
            if(tryLogin()){
                Intent intent = new Intent(Initial.this,Home.class);
                startActivity(intent);
                finish();
            }
        }
    }


    private boolean tryLogin(){


        return true;
    }
}
