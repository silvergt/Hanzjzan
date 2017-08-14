package kotel.hanzan;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.view.DrinkSelector;

public class PubPage extends AppCompatActivity {
    private DrinkSelector drinkSelector;

    private String[] drinkType;
    private String[][] drinkList;

    private TextView upperTitle, title, address, phoneNumber, workingHour, dayOff, description;
    private ImageView back, share, favorite, pubImage, call, location;

    private Dialog drinkSelectorDialog;
    private boolean isNowFirstStep=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubpage);

        drinkSelector = (DrinkSelector) findViewById(R.id.pubpage_drinkSelector);
        upperTitle = (TextView) findViewById(R.id.pubpage_upperTitle);
        title = (TextView) findViewById(R.id.pubpage_title);
        address = (TextView) findViewById(R.id.pubpage_address);
        phoneNumber = (TextView) findViewById(R.id.pubpage_phoneNumber);
        workingHour = (TextView) findViewById(R.id.pubpage_workingHour);
        dayOff = (TextView) findViewById(R.id.pubpage_dayOff);
        description = (TextView) findViewById(R.id.pubpage_description);
        back = (ImageView) findViewById(R.id.pubpage_back);
        share = (ImageView) findViewById(R.id.pubpage_share);
        favorite = (ImageView) findViewById(R.id.pubpage_favorite);
        pubImage = (ImageView) findViewById(R.id.pubpage_pubImage);
        call = (ImageView) findViewById(R.id.pubpage_call);
        location = (ImageView) findViewById(R.id.pubpage_location);

        LinearLayout.LayoutParams pubImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth / 2);
        pubImage.setLayoutParams(pubImageParams);


        back.setOnClickListener(view -> finish());

        call.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                return;
            } else {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "010911187680"));
                startActivity(intent);
            }
        });


//        drinkType=new String[]{"aa","bb","cc","dd","ee","ff","gg","hh"};
        drinkType = new String[]{"aa", "bb", "cc"};
        drinkList = new String[][]{
                {"aa1", "1", "cc3", "dd4", "ee5", "ff6", "gg7", "hh8", "hh8", "hh8", "hh8", "hh8", "hh8"},
                {"aa1", "2", "cc3", "dd4", "ee5"},
                {"aa1", "3", "cc3", "dd4", "ee5", "ff6", "gg7", "hh8"},
                {"aa1", "4", "cc3", "dd4", "ee5", "ff6", "gg7", "hh81232353535141124"},
                {"aa1", "5"},
                {"aa1", "6", "cc3", "dd4", "ee5", "ff6"},
                {"aa1", "7", "cc3", "dd4", "ee5", "ff6", "gg7"},
                {"aa1", "8", "hh8"},
                {"aa1", "9", "cc3", "dd4", "ee5", "ff6", "gg7", "hh8"}
        };

        drinkSelector.setDrinkList(drinkType, drinkList);

        drinkSelector.setListener(this::openDrinkSelectDialog);
    }

    private void openDrinkSelectDialog(String drinkName){
        drinkSelectorDialog = new Dialog(this);
        drinkSelectorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        drinkSelectorDialog.setCancelable(false);

        isNowFirstStep=true;

        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*7/10);
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.drinkselector_popup,null);

        ImageView drinkImage = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkImage);
        TextView dialogText1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text1);
        TextView dialogText2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text2);
        TextView button1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button1);
        TextView button2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button2);

        dialogText1.setText(title.getText().toString() + "에서 제공하는\n"+drinkName+"로 하시곘습니까?");
        dialogText2.setText(drinkName+"\n"+"한잔 주세요!");
        dialogText2.setVisibility(View.INVISIBLE);

        button1.setOnClickListener(view -> {
            if(isNowFirstStep){
                dialogText1.setVisibility(View.INVISIBLE);
                dialogText2.setVisibility(View.VISIBLE);
                button1.setText("직원 확인");
                button2.setText("취소하기");
                isNowFirstStep=false;
            }else{
                requestService();
            }
        });

        button2.setOnClickListener(view -> drinkSelectorDialog.cancel());


        drinkSelectorDialog.setContentView(dialogLayout,dialogParams);

        drinkSelectorDialog.show();
    }

    private boolean requestService(){


        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "010911187680"));
                startActivity(intent);
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(drinkSelector.isDrinkListViewIsVisible()){
            drinkSelector.closeDrinkListView();
            return;
        }else if(drinkSelectorDialog!=null && drinkSelectorDialog.isShowing()){
            drinkSelectorDialog.cancel();
            return;
        }
        super.onBackPressed();
    }
}
