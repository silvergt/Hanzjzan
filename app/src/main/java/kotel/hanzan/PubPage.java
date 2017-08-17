package kotel.hanzan;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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
    public static int REQUEST_OPENPUBPAGE=10;
    public static int RESULT_FAVORITECHANGED=11;

    private DrinkSelector drinkSelector;

    private PubInfo pubInfo;

    private String[] drinkType;
    private String[][] drinkList;

    private TextView upperTitle, title, address, phoneNumber, workingHour, dayOff, description;
    private ImageView back, share, favorite, pubImage, call, location;

    private Dialog drinkSelectorDialog;
    private int dialogStep=0;


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
        if(pubInfo.getFavorite()){
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
            ImageViewer.Builder builder = new ImageViewer.Builder(this, pubInfo.imageAddress);

            TextView imageCounter = new TextView(this);
            imageCounter.setPadding(0,50,0,0);
            imageCounter.setGravity(Gravity.CENTER);
            imageCounter.setTextColor(Color.WHITE);
            imageCounter.setWidth(StaticData.displayWidth);

            builder.setImageChangeListener(position -> {
                imageCounter.setText(Integer.toString(position+1)+"/"+Integer.toString(pubInfo.imageAddress.length));
            });

            builder.setOverlayView(imageCounter);
            builder.show();
        });

        favorite.setOnClickListener(view -> {
            pubInfo.setFavorite(!pubInfo.getFavorite());
            if(pubInfo.getFavorite()){
                favorite.setImageResource(R.drawable.favorite_clicked);
            }else{
                favorite.setImageResource(R.drawable.favorite);
            }
            Intent data = new Intent();
            data.putExtra("favorite", pubInfo.getFavorite());
            setResult(RESULT_FAVORITECHANGED,data);
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

        dialogStep=0;

        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*7/10);
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.drinkselector_popup,null);

        drinkImage = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkImage);
        dialogText1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text1);
        dialogText2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text2);
        button1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button1);
        button2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button2);

        if(StaticData.currentUser.expireYYYY==0){
            dialogText1.setText("아직 한잔 멤버가 아니시네요!\n지금 가입하실래요?");
            dialogText2.setVisibility(View.INVISIBLE);
            button1.setText("네 가입할래요!");
            button2.setText("아니요 나중에 할게요");
            dialogStep = 0;
        }else{
            dialogText1.setText(title.getText().toString() + "에서 제공하는\n"+drinkName+"로 하시겠습니까?");
            dialogText2.setVisibility(View.INVISIBLE);
            button1.setText("네, 맞아요");
            button2.setText("다시 생각해볼게요");
            dialogStep = 1;
        }


        button1.setOnClickListener(view -> {
            switch (dialogStep){
                case 0:
                    Intent intent = new Intent(getApplicationContext(),Membership.class);
                    startActivity(intent);
                    drinkSelectorDialog.cancel();
                    break;
                case 1:
                    dialogText2.setText(drinkName+"\n"+"한잔 주세요!");
                    drinkImage.setVisibility(View.VISIBLE);
                    dialogText1.setVisibility(View.INVISIBLE);
                    dialogText2.setVisibility(View.VISIBLE);
                    button1.setText("직원 확인");
                    button2.setText("취소하기");
                    dialogStep = 2;
                    break;
                case 2:
                    break;
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
