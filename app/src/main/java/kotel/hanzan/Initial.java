package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;

import com.facebook.drawee.backends.pipeline.Fresco;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.view.JActivity;

public class Initial extends JActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;

        if(!Fresco.hasBeenInitialized()) {
            Fresco.initialize(this);
        }


        new Thread(()->{
            try{
                Thread.sleep(1500);
            }catch (Exception e){e.printStackTrace();}
            new Handler(getMainLooper()).post(()->{
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            });
        }).start();
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
