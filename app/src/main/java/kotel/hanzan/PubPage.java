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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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

import java.util.HashMap;

import kotel.hanzan.Data.DrinkInfo;
import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.DrinkSelector;
import kotel.hanzan.view.JActivity;

public class PubPage extends JActivity {
    final public static int REQUEST_OPENPUBPAGE=10;
    final public static int RESULT_FAVORITECHANGED=11;

    private HashMap<String,String> map;

    private DrinkSelector drinkSelector;

    private PubInfo pubInfo;

    private TextView upperTitle, title, address, phoneNumber, workingHour_weekday,workingHour_weekend, dayOff, description;
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
        share = (ImageView)findViewById(R.id.pubpage_share);
        address = (TextView) findViewById(R.id.pubpage_address);
        phoneNumber = (TextView) findViewById(R.id.pubpage_phoneNumber);
        workingHour_weekday = (TextView) findViewById(R.id.pubpage_workingHour_weekday);
        workingHour_weekend = (TextView) findViewById(R.id.pubpage_workingHour_weekend);
        dayOff = (TextView) findViewById(R.id.pubpage_dayOff);
        description = (TextView) findViewById(R.id.pubpage_description);
        back = (ImageView) findViewById(R.id.pubpage_back);
        favorite = (ImageView) findViewById(R.id.pubpage_favorite);
        pubImage = (ImageView) findViewById(R.id.pubpage_pubImage);
        call = (ImageView) findViewById(R.id.pubpage_call);
        location = (ImageView) findViewById(R.id.pubpage_location);

        LinearLayout.LayoutParams pubImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth*3/5);
        pubImage.setLayoutParams(pubImageParams);

        Picasso.with(this).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(pubImage);

        upperTitle.setText(pubInfo.name);
        title.setText(pubInfo.name);
        address.setText(pubInfo.address);
        phoneNumber.setText(pubInfo.phone);
        if(pubInfo.getFavorite()){
            favorite.setImageResource(R.drawable.pubpage_favorite_selected);
        }else{
            favorite.setImageResource(R.drawable.pubpage_favorite_unselected);
        }

        
        back.setOnClickListener(view -> finish());

        share.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_TEXT,pubInfo.name+" "+getString(R.string.shallWeHaveDrink)+"\n"+pubInfo.imageAddress.get(0));

            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,getString(R.string.share)));

        });

        location.setOnClickListener(view -> {
            Intent intent = new Intent(PubPage.this,LocationViewer.class);
            intent.putExtra("info",pubInfo);
            startActivity(intent);
        });

        call.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.makeCall));
            builder.setNegativeButton(getString(R.string.no),null);
            builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                    return;
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber.getText().toString()));
                        startActivity(intent);
                    }catch (Exception e){e.printStackTrace();}
                }
            });
            builder.show();

        });

        pubImage.setOnClickListener(view -> {
            String[] images = new String[pubInfo.imageAddress.size()];
            for(int i=0;i<pubInfo.imageAddress.size();i++){
                images[i] = pubInfo.imageAddress.get(i);
            }
            ImageViewer.Builder builder = new ImageViewer.Builder(this, images);

            TextView imageCounter = new TextView(this);
            imageCounter.setPadding(0,50,0,0);
            imageCounter.setGravity(Gravity.CENTER);
            imageCounter.setTextColor(Color.WHITE);
            imageCounter.setWidth(StaticData.displayWidth);

            builder.setImageChangeListener(position -> {
                imageCounter.setText(Integer.toString(position+1)+"/"+Integer.toString(pubInfo.imageAddress.size()));
            });

            builder.setOverlayView(imageCounter);
            builder.show();
        });

        favorite.setOnClickListener(view -> {
            pubInfo.setFavorite(!pubInfo.getFavorite());
            if(pubInfo.getFavorite()){
                favorite.setImageResource(R.drawable.pubpage_favorite_selected);
            }else{
                favorite.setImageResource(R.drawable.pubpage_favorite_unselected);
            }
            Intent data = new Intent();
            data.putExtra("favorite", pubInfo.getFavorite());
            setResult(RESULT_FAVORITECHANGED,data);
        });

        drinkSelector.setListener(this::openDrinkSelectDialog);

        retrieveDetailInfo();
    }

    private void openDrinkSelectDialog(DrinkInfo drinkInfo){
        drinkSelectorDialog = new Dialog(this);
        drinkSelectorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogStep=0;

//        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayHeight*7/10);
        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.drinkselector_popup,null);

        drinkImage = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkImage);
        dialogText1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text1);
        dialogText2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_text2);
        button1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button1);
        button2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button2);

        if(StaticData.currentUser.expireYYYY==0){
            //아직 멤버가 아닌 경우 - 0
            Picasso.with(getApplicationContext()).load(R.drawable.purchasesuccess).into(drinkImage);
            dialogText1.setText(getString(R.string.pubPagePopup_joinMembership));
            dialogText2.setVisibility(View.INVISIBLE);
            button1.setText(getString(R.string.yesJoin));
            button2.setText(getString(R.string.noLater));
            dialogStep = 0;
        }else if(StaticData.currentUser.isHanjanAvailableToday){
            //멤버이고, 오늘 한잔을 사용하지 않은 경우 - 1
            Picasso.with(getApplicationContext()).load(drinkInfo.drinkImageAddress).placeholder(R.drawable.hite).into(drinkImage);
            dialogText1.setText(title.getText().toString() +getString(R.string.providing)+"\n"+drinkInfo.drinkName+getString(R.string.isYourChoice));
            dialogText1.setVisibility(View.VISIBLE);
            dialogText2.setVisibility(View.INVISIBLE);
            button1.setText(getString(R.string.yes));
            button2.setText(getString(R.string.no));
            dialogStep = 1;
        }else{
            //멤버이고, 오늘 한잔을 사용한 경우 - 2
            Picasso.with(getApplicationContext()).load(R.drawable.purchasesuccess).into(drinkImage);
            dialogText1.setText(getString(R.string.alreadyUsedTicket));
            dialogText1.setVisibility(View.VISIBLE);
            dialogText2.setVisibility(View.INVISIBLE);
            button1.setText(getString(R.string.willComebackLater));
            button2.setVisibility(View.GONE);
            dialogStep = 2;
        }


        button1.setOnClickListener(view -> {
            switch (dialogStep){

                case 1:
                    dialogText2.setText(drinkInfo.drinkName+"\n"+getString(R.string.oneGlassPlease));
                    dialogText1.setVisibility(View.INVISIBLE);
                    dialogText2.setVisibility(View.VISIBLE);
                    button1.setBackgroundResource(R.drawable.roundbox_gray);
                    button2.setBackgroundResource(R.drawable.roundbox_maincolor);
                    button1.setText(getString(R.string.cancel));
                    button2.setText(getString(R.string.clerkCheck));
                    dialogStep = 3;
                    break;
                case 0:
                    Intent intent = new Intent(getApplicationContext(),Membership.class);
                    startActivity(intent);
                case 2:
                case 3:
                case 4:
                    drinkSelectorDialog.cancel();
                    break;
            }
        });

        button2.setOnClickListener(view -> {
            switch (dialogStep) {
                case 3:
                    useVoucher(drinkInfo.drinkName,drinkInfo.drinkType);
                    break;
                default:
                    drinkSelectorDialog.cancel();
                    break;
            }
        });


        drinkSelectorDialog.setContentView(dialogLayout,dialogParams);

        drinkSelectorDialog.show();
    }


    private synchronized void useVoucher(String drinkName,String drinkType){
        new Thread(()->{
            HashMap<String, String> map = new HashMap<>();

            map.put("id_member", Long.toString(StaticData.currentUser.id));
            map.put("id_place", Long.toString(pubInfo.id));
            map.put("name_drink",drinkName);
            map.put("category_drink",drinkType);
            map = ServerConnectionHelper.connect("using today's hanzan", "usevoucher", map);

            String availability = map.get("availabletoday");
            String voucherUsedSuccessfully = map.get("usevoucher_result");

            new Handler(getMainLooper()).post(()->{
                button1.setBackgroundResource(R.drawable.roundbox_maincolor);
                button2.setBackgroundResource(R.drawable.roundbox_gray);
                if(availability==null||availability.equals("FALSE")){
                    dialogText1.setText(getString(R.string.alreadyUsedTicket));
                    dialogText1.setVisibility(View.VISIBLE);
                    dialogText2.setVisibility(View.INVISIBLE);
                    button1.setText(getString(R.string.willComebackLater));
                    button2.setVisibility(View.GONE);
                    dialogStep = 2;
                }else if(availability.equals("TRUE")&& voucherUsedSuccessfully.equals("TRUE")){
                    StaticData.currentUser.isHanjanAvailableToday = false;

                    dialogText2.setText(getString(R.string.SuccessfullyUsed)+"\n"+drinkName+getString(R.string.isComing));
                    drinkImage.setVisibility(View.VISIBLE);
                    dialogText1.setVisibility(View.INVISIBLE);
                    dialogText2.setVisibility(View.VISIBLE);
                    button1.setText("확인");
                    button2.setVisibility(View.GONE);
                    dialogStep = 4;
                }else{
                    //Connection with server failed
                    drinkSelectorDialog.cancel();
                }
            });
        }).start();
    }


    private synchronized void retrieveDetailInfo(){

        new Thread(()->{
            map = new HashMap<>();
            map.put("id_place",Long.toString(pubInfo.id));
            map = ServerConnectionHelper.connect("retrieving pub's detail info","placeinfo",map);

            pubInfo.phone = map.get("call_place");
            pubInfo.dayoff = map.get("dayoff");
            pubInfo.description = map.get("description");
            pubInfo.work_weekday = map.get("weektime");
            pubInfo.work_weekend = map.get("weekendtime");

            int i=0;
            while(true){
                if(map.get("imgadd_place_" + Integer.toString(i++))!=null){
                    pubInfo.imageAddress.add(map.get("imgadd_place_" + Integer.toString(i++)));
                }else{
                    break;
                }
            }
            i = 0;
            while (true){
                if(map.get("category_drink_"+Integer.toString(i))!=null) {
                    pubInfo.drinkList.add(new DrinkInfo(map.get("category_drink_" + Integer.toString(i)), map.get("name_drink_" + Integer.toString(i)),map.get("imgadd_drink_" + Integer.toString(i++))));
                }else{
                    break;
                }
            }
            new Handler(getMainLooper()).post(()->{
                phoneNumber.setText(pubInfo.phone);
                dayOff.setText(pubInfo.dayoff);
                description.setText(pubInfo.description);
                workingHour_weekday.setText(pubInfo.work_weekday);
                workingHour_weekend.setText(pubInfo.work_weekend);

                drinkSelector.setDrinkList(pubInfo.drinkList);
            });

        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber.getText().toString()));
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
