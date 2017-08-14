package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.AssetImageHelper;
import kotel.hanzan.view.DrinkCalendar;

public class Initial extends AppCompatActivity {

    DrinkCalendar cal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;

        ImageView image=(ImageView)findViewById(R.id.initial_test);
        AssetImageHelper.loadDrinkImage(this,"test").into(image);


        cal = (DrinkCalendar) findViewById(R.id.drinkcalendartest);
//        cal.setCalendar();
        cal.setListener((year, monthInNormal) -> {
            if(year == 2017 && monthInNormal == 8){
                cal.setDateChecked(new int[]{1,4,5,7,11,15});
            }
        });
        cal.setLowerText("한잔 멤버십이 15일 남았습니다");

        RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(StaticData.displayWidth*9/10,StaticData.displayWidth*4/5);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        cal.setLayoutParams(params);

        Intent intent=new Intent(Initial.this,Home.class);
        startActivity(intent);
        finish();
    }
}
