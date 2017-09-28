package kotel.hanzan;

import android.Manifest;
import android.animation.ObjectAnimator;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.HashMap;

import kotel.hanzan.Data.DrinkInfo;
import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.DrinkSelectorListener;
import kotel.hanzan.listener.SlideListener;
import kotel.hanzan.view.DrinkSelector;
import kotel.hanzan.view.HorizontalSlideView;
import kotel.hanzan.view.HorizontalSlideViewChild;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.SlideCountView;

public class PubPage extends JActivity {
    final public static int REQUEST_OPENPUBPAGE=10;
    final public static int RESULT_FAVORITECHANGED=11;

    private HashMap<String,String> map;

    private DrinkSelector drinkSelector;

    private PubInfo pubInfo;

    private RelativeLayout mainLayout;
    private TextView upperTitle, title, address, phoneNumber, workingHour_weekday,workingHour_weekend, dayOff, description;
    private ImageView back, share, favorite, call, location;
    private HorizontalSlideView pubImage;
    private SlideCountView slideCount;

    private Dialog drinkSelectorDialog;
    private int dialogStep=0;


    //*****************Dialog*****************
    private ImageView dialogDrinkImage,dialogCheckIcon;
    private TextView dialogText,dialogPubName,dialogDrinkName,dialogReuseInfo;
    private TextView dialogButton1;
    private TextView dialogButton2;


    //************************TUTORIAL Tab************************
    private RelativeLayout layout1,layout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubpage);

        mainLayout = (RelativeLayout)findViewById(R.id.pubpage_pubpage);
        drinkSelector = (DrinkSelector) findViewById(R.id.pubpage_drinkSelector);
        slideCount = (SlideCountView)findViewById(R.id.pubpage_slideCount);
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
        pubImage = (HorizontalSlideView) findViewById(R.id.pubpage_pubImage);
        call = (ImageView) findViewById(R.id.pubpage_call);
        location = (ImageView) findViewById(R.id.pubpage_location);

        RelativeLayout.LayoutParams pubImageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth*3/5);
        pubImage.setLayoutParams(pubImageParams);
        pubImage.setChildWidth(StaticData.displayWidth);
        pubImage.setOnSlideListener(new SlideListener() {
            @Override
            public void afterSlide() {
                slideCount.setCountTo(pubImage.getCurrentIndex());
            }

            @Override
            public void beforeSlide() {

            }

            @Override
            public void whileSlide() {

            }
        });

        back.setOnClickListener(view -> finish());

        share.setOnClickListener(view -> {
            if(pubInfo.tutorialPub){return;}
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_TEXT,pubInfo.name+" "+getString(R.string.shallWeHaveDrink)+"\nhttps://90labs.com/share_place/?provider=Drinkat&pubId="+Long.toString(pubInfo.id));

            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,getString(R.string.share)));

        });

        location.setOnClickListener(view -> {
            if(pubInfo.tutorialPub){return;}
            Intent intent = new Intent(PubPage.this,LocationViewer.class);
            intent.putExtra("info",pubInfo);
            startActivity(intent);
        });

        call.setOnClickListener(view -> {
            if(pubInfo.tutorialPub){return;}
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

        favorite.setOnClickListener(view -> {
            if(pubInfo.tutorialPub){return;}
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

        drinkSelector.setListener(new DrinkSelectorListener() {
            @Override
            public void itemSelected(DrinkInfo drinkInfo) {
                if(pubInfo.tutorialPub){
                    openTutorialDrinkSelectDialog(drinkInfo);
                }else {
                    openDrinkSelectDialog(drinkInfo);
                }
            }

            @Override
            public void typeSelected(String typeName) {
                if(pubInfo.tutorialPub){
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(layout2,"alpha",0,1).setDuration(900).start();
                }
            }
        });

        Uri uri = getIntent().getData();
        if(uri != null){
            if(StaticData.currentUser == null){
                reopenLoginPage();
                return;
            }
            //Case if user entered this activity by clicking pubPage link
            JLog.v("QUERY",uri.getQuery());
            JLog.v("QUERY pubID",uri.getQueryParameter("pubId"));
            long pubId = Long.parseLong(uri.getQueryParameter("pubId"));
            retrieveFullInfo(pubId);
        }else {
            //Case if user entered via our application
            pubInfo = (PubInfo) getIntent().getSerializableExtra("info");
            upperTitle.setText(pubInfo.name);
            title.setText(pubInfo.name);
            address.setText(pubInfo.address);
            phoneNumber.setText(pubInfo.phone);
            if(pubInfo.getFavorite()){
                favorite.setImageResource(R.drawable.pubpage_favorite_selected);
            }else{
                favorite.setImageResource(R.drawable.pubpage_favorite_unselected);
            }

            retrieveDetailInfo();
            if (pubInfo.tutorialPub) {
                openTutorial();
            }
        }

    }

    private void openTutorial(){
        RelativeLayout tutorialLayout = (RelativeLayout)getLayoutInflater().inflate(R.layout.pubpage_tutorial,null);
        layout1 = (RelativeLayout)tutorialLayout.findViewById(R.id.pubpage_tutorial_layout1);
        layout2 = (RelativeLayout)tutorialLayout.findViewById(R.id.pubpage_tutorial_layout2);

        ObjectAnimator.ofFloat(layout1,"alpha",0,1).setDuration(900).start();

        mainLayout.addView(tutorialLayout);
    }

    private void openTutorialDrinkSelectDialog(DrinkInfo drinkInfo){
        Dialog tutorialDrinkSelectorDialog = new Dialog(this);
        tutorialDrinkSelectorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        tutorialDrinkSelectorDialog.setCancelable(false);

        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.pubpage_tutorial_popup,null);

        ImageView tutorialDrinkImage = (ImageView)dialogLayout.findViewById(R.id.pubpagePopup_tutorial_drinkImage);
        TextView tutorialStartApp = (TextView)dialogLayout.findViewById(R.id.pubpagePopup_tutorial_start);

        Picasso.with(PubPage.this).load(drinkInfo.drinkImageAddress).placeholder(R.drawable.drinkselector_default).into(tutorialDrinkImage);

        tutorialStartApp.setOnClickListener(view -> {
            new Thread(()->{
                HashMap<String,String> map = new HashMap<>();
                map.put("id_member",Long.toString(StaticData.currentUser.id));
                map = ServerConnectionHelper.connect("tutorial finished","tutorialfinished",map);

                final String returned = map.get("tutorialfinished");

                new Handler(getMainLooper()).post(() -> {
                    if(returned == null || returned.equals("FALSE")){
                        Toast.makeText(getApplicationContext(),getString(R.string.networkFailure),Toast.LENGTH_SHORT).show();
                    }else if(returned.equals("TRUE")){
                        StaticData.currentUser.finishedTutorial = true;
                    }
                    tutorialDrinkSelectorDialog.cancel();
                    Intent intent = new Intent(getApplicationContext(),Home.class);
                    startActivity(intent);
                    finishAffinity();
                });

            }).start();

        });

        tutorialDrinkSelectorDialog.setContentView(dialogLayout,dialogParams);
        tutorialDrinkSelectorDialog.show();
    }

    private void openDrinkSelectDialog(DrinkInfo drinkInfo){
        drinkSelectorDialog = new Dialog(this);
        drinkSelectorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogStep=0;

        ViewGroup.LayoutParams dialogParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.drinkselector_popup,null);

        dialogDrinkImage = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkImage);
        dialogPubName = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_pubName);
        dialogDrinkName = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_drinkName);
        dialogCheckIcon = (ImageView)dialogLayout.findViewById(R.id.drinkSelectorDialog_checkIcon);
        dialogText = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_mainText);
        dialogReuseInfo = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_reuseInfo);
        dialogButton1 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button1);
        dialogButton2 = (TextView)dialogLayout.findViewById(R.id.drinkSelectorDialog_button2);

        Picasso.with(this).load(drinkInfo.drinkImageAddress).placeholder(R.drawable.drinkselector_default).into(dialogDrinkImage);
        dialogPubName.setText(pubInfo.name);
        dialogDrinkName.setText(drinkInfo.drinkName);

        if(StaticData.currentUser.expireYYYY==0){
            //아직 멤버가 아닌 경우 - 0
            Picasso.with(getApplicationContext()).load(R.drawable.purchasefailed).into(dialogDrinkImage);
            dialogText.setText(getString(R.string.pubPagePopup_joinMembership));
            dialogButton1.setText(getString(R.string.yesJoin));
            dialogButton2.setText(getString(R.string.noLater));
            dialogStep = 0;
        }else if(StaticData.currentUser.isHanjanAvailableToday){
            //멤버이고, 오늘 한잔을 사용하지 않은 경우 - 1
            Picasso.with(getApplicationContext()).load(drinkInfo.drinkImageAddress).placeholder(R.drawable.drinkselector_default).into(dialogDrinkImage);
            dialogStep = 1;
        }else{
            //멤버이고, 오늘 한잔을 사용한 경우 - 2
            Picasso.with(getApplicationContext()).load(R.drawable.purchasefailed).into(dialogDrinkImage);
            dialogText.setText(getString(R.string.alreadyUsedTicket));
            dialogButton1.setText(getString(R.string.willComebackLater));
            dialogButton2.setVisibility(View.GONE);
            dialogStep = 2;
        }


        dialogButton1.setOnClickListener(view -> {
            switch (dialogStep){
                case 1:
                    //한잔 사용 버튼을 누른 경우 - 3
                    Dialog dialog2 = new Dialog(PubPage.this);

                    dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.popupbox_normal, null);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layout.setLayoutParams(params);

                    TextView text = (TextView)layout.findViewById(R.id.popupBox_text);
                    TextView yes = (TextView) layout.findViewById(R.id.popupBox_yes);
                    TextView no = (TextView) layout.findViewById(R.id.popupBox_no);

                    text.setText(getString(R.string.DrinkSelectorPopup_text2));

                    yes.setText(getString(R.string.confirm));
                    no.setText(getString(R.string.cancel));

                    no.setOnClickListener(view1 -> dialog2.cancel());
                    yes.setOnClickListener(view1 -> {
                        useVoucher(drinkInfo.drinkName,drinkInfo.drinkType);
                        dialog2.cancel();
                    });

                    dialog2.setContentView(layout);
                    dialog2.show();

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

        dialogButton2.setOnClickListener(view -> {
            drinkSelectorDialog.cancel();
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

            if(map.get("availabletoday")==null){
                dialogText.setText(getString(R.string.networkFailure));
                dialogButton1.setText(getString(R.string.confirm));
                dialogButton2.setVisibility(View.GONE);
                dialogStep = 2;
                return;
            }
            String availability = map.get("availabletoday");
            String voucherUsedSuccessfully = map.get("usevoucher_result");

            new Handler(getMainLooper()).post(()->{
                if(availability==null||availability.equals("FALSE")){
                    dialogText.setText(getString(R.string.alreadyUsedTicket));
                    dialogButton1.setText(getString(R.string.willComebackLater));
                    dialogButton2.setVisibility(View.GONE);
                    dialogStep = 2;
                }else if(availability.equals("TRUE")&& voucherUsedSuccessfully.equals("TRUE")){
                    StaticData.currentUser.isHanjanAvailableToday = false;
                    dialogText.setText(getString(R.string.SuccessfullyUsed));
                    dialogButton1.setText(getString(R.string.confirm));
                    dialogButton2.setVisibility(View.GONE);
                    dialogCheckIcon.setVisibility(View.VISIBLE);
                    dialogReuseInfo.setVisibility(View.VISIBLE);
                    dialogStep = 4;
                }else{
                    //Connection with server failed
                    drinkSelectorDialog.cancel();
                }
            });
        }).start();
    }


    private synchronized void retrieveFullInfo(long pubId){
        new Thread(()->{
            map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("id_place",Long.toString(pubId));
            map = ServerConnectionHelper.connect("retrieving full pub info","shareplace",map);

            if(map.get("name_place")==null){
                return;
            }
            String name = map.get("name_place");
            String pubAddress = map.get("address_place");
            String imageAddress = map.get("imgadd_place");
            String district = map.get("district");
            boolean pubFavorite = false;
            if (map.get("like").equals("TRUE")) {
                pubFavorite = true;
            }
            double lat = Double.parseDouble(map.get("lat"));
            double lng = Double.parseDouble(map.get("lng"));

            pubInfo = new PubInfo(pubId,name,pubAddress,district,imageAddress,pubFavorite,lat,lng);
            new Handler(getMainLooper()).post(()->{
                upperTitle.setText(pubInfo.name);
                title.setText(pubInfo.name);
                address.setText(pubInfo.address);
                phoneNumber.setText(pubInfo.phone);
                if(pubInfo.getFavorite()){
                    favorite.setImageResource(R.drawable.pubpage_favorite_selected);
                }else{
                    favorite.setImageResource(R.drawable.pubpage_favorite_unselected);
                }
            });
            retrieveDetailInfo();
        }).start();
    }

    private synchronized void retrieveDetailInfo(){
        if(pubInfo.tutorialPub){
            map = new HashMap<>();
            pubInfo.phone = "";
            pubInfo.dayoff = "";
            pubInfo.description = getString(R.string.tutorial_pubDescription);
            pubInfo.work_weekday = "";
            pubInfo.work_weekend = "";
            pubInfo.drinkList.add(new DrinkInfo("beer", getString(R.string.tutorial_drinkName),"https://s3.ap-northeast-2.amazonaws.com/hanjan/drink_craft.png"));

            new Handler(getMainLooper()).post(()->{
                phoneNumber.setText(pubInfo.phone);
                dayOff.setText(pubInfo.dayoff);
                description.setText(pubInfo.description);
                workingHour_weekday.setText(pubInfo.work_weekday);
                workingHour_weekend.setText(pubInfo.work_weekend);

                drinkSelector.setDrinkList(pubInfo.drinkList);
            });
            return;
        }

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
                if(map.get("imgadd_place_" + Integer.toString(i))!=null){
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
                slideCount.initialize(pubInfo.imageAddress.size(), (int)getResources().getDimension(R.dimen.pubpage_slideCountWidth), 5);

                setPubImageView();

                drinkSelector.setDrinkList(pubInfo.drinkList);
            });

        }).start();
    }

    private void setPubImageView(){
        for(int i=0;i<pubInfo.imageAddress.size();i++){
            HorizontalSlideViewChild viewChild = new HorizontalSlideViewChild(this);
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            Picasso.with(this).load(pubInfo.imageAddress.get(i)).placeholder(R.drawable.loading_store).into(image);
            viewChild.addView(image,params);

            int finalI = i;
            viewChild.setSlideViewChildClickListener(() -> {
                if(pubInfo.tutorialPub){return;}
                String[] images = new String[pubInfo.imageAddress.size()];
                for(int j=0;j<pubInfo.imageAddress.size();j++){
                    images[j] = pubInfo.imageAddress.get(j);
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

                builder.setStartPosition(finalI);
                builder.setOverlayView(imageCounter);
                builder.show();
            });

            pubImage.addViewToList(viewChild);
        }
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

    private void reopenLoginPage() {
        Intent intent = new Intent(getApplicationContext(), Initial.class);
        startActivity(intent);
        finishAffinity();
    }
}
