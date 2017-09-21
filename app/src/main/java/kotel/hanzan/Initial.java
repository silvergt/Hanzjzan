package kotel.hanzan;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;

public class Initial extends JActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        if(!ServerConnectionHelper.isRegularServer()){
            Toast.makeText(getApplicationContext(),"TEST SERVER",Toast.LENGTH_SHORT).show();
        }

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;
        StaticData.displayWidthWithoutMargin = (int)(StaticData.displayWidth - 2 * getResources().getDimension(R.dimen.leftRightMargin));
        StaticData.TOSSKEY = getString(R.string.tossApiKey);


        new Thread(()->{
            try{
                Thread.sleep(1500);
            }catch (Exception e){e.printStackTrace();}
            testServerConnection();
        }).start();
    }


    private void testServerConnection(){
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            JLog.v("VERSION",version);
            int code = pInfo.versionCode;
            JLog.v("VERSION CODE : ",code);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        HashMap<String,String> map =new HashMap<>();
        map.put("member_key","NULLKEY");
        map = ServerConnectionHelper.connect("server connection test","login",map);

        if(map.get("signup_history")==null){
            new Handler(getMainLooper()).post(this::openServerCheckingDialog);
        }else{
            new Handler(getMainLooper()).post(()->{
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void openServerCheckingDialog(){
        Dialog dialog = new Dialog(this);

        RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.initial_serverchecking,null);
        TextView confirm = (TextView)layout.findViewById(R.id.serverChecking_confirm);

        confirm.setOnClickListener(view -> finish());

        dialog.setContentView(layout);
        dialog.show();
    }

    @Override
    public void startActivity(Intent intent) {
        try{
            Thread.sleep(1500);
        }catch (Exception e){e.printStackTrace();}
        super.startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {}

}
