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

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.view.DrinkSelector;

public class PubPage extends AppCompatActivity {
    public static int RESULT_FAVORITECHANGED=1;

    private DrinkSelector drinkSelector;

    private PubInfo pubInfo;

    private String[] drinkType;
    private String[][] drinkList;

    private TextView upperTitle, title, address, phoneNumber, workingHour, dayOff, description;
    private ImageView back, share, favorite, pubImage, call, location;

    private Dialog drinkSelectorDialog;
    private boolean isNowFirstStep=true;



    //*****************Dialog*****************
    private ImageView drinkImage;
    private TextView dialogText1;
    private TextView dialogText2;
    private TextView button1;
    private TextView button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubpage);

        pubInfo = (PubInfo) getIntent().getSerializableExtra("info");

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

        Picasso.with(this).load(pubInfo.imageAddress[0]).into(pubImage);

        upperTitle.setText(pubInfo.name);
        title.setText(pubInfo.name);
        address.setText(pubInfo.address);
        phoneNumber.setText(pubInfo.phone);
        if(pubInfo.favorite){
            favorite.setImageResource(R.drawable.favorite_clicked);
        }else{
            favorite.setImageResource(R.drawable.favorite);
        }

        back.setOnClickListener(view -> finish());

        location.setOnClickListener(view -> {
            Intent intent = new Intent(PubPage.this,LocationViewer.class);
            intent.putExtra("info",pubInfo);
            startActivity(intent);
        });

        call.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                return;
            } else {
                try {
                    String phoneNumberString = phoneNumber.getText().toString().replace("-","");
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberString));
                    startActivity(intent);
                }catch (Exception e){e.printStackTrace();}
            }
        });

        pubImage.setOnClickListener(view -> {
            ImageViewer.Builder builder = new ImageViewer.Builder(this, new String[]{"https://cdn.pixabay.com/photo/2015/12/09/04/27/a-single-person-1084191_1280.jpg",
                    "https://cdn.pixabay.com/photo/2013/04/06/11/50/image-editing-101040_1280.jpg",
                    "https://cdn.pixabay.com/photo/2013/02/16/16/34/crystal-82296_1280.jpg"
            });
            builder.show();
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

        drinkImage = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkImage);
        dialogText1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text1);
        dialogText2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text2);
        button1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button1);
        button2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button2);

        dialogText1.setText(title.getText().toString() + "에서 제공하는\n"+drinkName+"로 하시곘습니까?");
        dialogText2.setText(drinkName+"\n"+"한잔 주세요!");
        dialogText2.setVisibility(View.INVISIBLE);

        button1.setOnClickListener(view -> {
            if(isNowFirstStep){
                drinkImage.setVisibility(View.VISIBLE);
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
