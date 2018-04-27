package kotel.hanzan;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import kotel.hanzan.data.StaticData;
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
            requestUpdateInfo();
        }).start();
    }

    private void requestUpdateInfo(){
        try{
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionCode = Integer.toString(pInfo.versionCode);

            HashMap<String,String> map =new HashMap<>();
            map.put("version",versionCode);
            map = ServerConnectionHelper.connect("Testing version validity","versioncheck",map);

            if(map.get("update_required")==null){
                new Handler(getMainLooper()).post(this::openServerCheckingDialog);
            }else{
                if(map.get("update_required").equals("TRUE")) {
                    new Handler(getMainLooper()).post(this::openUpdateDialog);
                }else{
                    new Handler(getMainLooper()).post(()->{
                        Intent intent = new Intent(getApplicationContext(),Login.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            new Handler(getMainLooper()).post(()->{
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void openUpdateDialog(){
        Dialog dialog = new Dialog(Initial.this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.popupbox_normal, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        TextView text = layout.findViewById(R.id.popupBox_text);
        TextView yes = layout.findViewById(R.id.popupBox_yes);
        TextView no = layout.findViewById(R.id.popupBox_no);

        text.setText(getString(R.string.updateNeeded));
        yes.setText(getString(R.string.update));
        no.setText(getString(R.string.cancel));

        yes.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kotel.hanzan")));
            finish();
        });

        no.setOnClickListener(view -> {
            finish();
        });

        dialog.setContentView(layout);
        dialog.show();
    }


    private void openServerCheckingDialog(){
        Dialog dialog = new Dialog(Initial.this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.popupbox_normal,null);
        TextView text = layout.findViewById(R.id.popupBox_text);
        TextView confirm = layout.findViewById(R.id.popupBox_yes);
        TextView no = layout.findViewById(R.id.popupBox_no);
        no.setVisibility(View.GONE);

        text.setText(getString(R.string.serverChecking));
        confirm.setText(getString(R.string.confirm));

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
