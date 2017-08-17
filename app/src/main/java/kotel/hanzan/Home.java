package kotel.hanzan;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.listener.TapBarItemClickListener;
import kotel.hanzan.view.DrinkCalendar;
import kotel.hanzan.view.JRecyclerView;
import kotel.hanzan.view.ProfileCircleImageView;
import kotel.hanzan.view.TapBar;

import static kotel.hanzan.R.drawable.favorite;

public class Home extends AppCompatActivity {
    TapBar tapBar;
    RelativeLayout container;

    ImageView upperBarLeftIcon,upperBarMap,upperBarSearch;
    TextView upperBarMainText,upperBarSubText;

    NMapLocationManager locationManager;
    NGeoPoint myLocation;

    //************************Home Tab, MyFavorite Tab************************
    JRecyclerView pubInfoRecyclerView;
    ArrayList<PubInfo> pubInfoArray;
    HomeRecyclerViewAdapter homeAdapter;
    private class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>{
        LinearLayout.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder=null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image,favorite;
            TextView text1,text2,text3;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView)itemView.findViewById(R.id.pubitem_image);
                image.setLayoutParams(imageParams);
                favorite = (ImageView)itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView)itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView)itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView)itemView.findViewById(R.id.pubitem_text3);
            }
        }

        public void setFavoriteButton(boolean clicked){
            try{
                pubInfoArray.get(lastClickedNumber).setFavorite(clicked);
                if(clicked){
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_clicked);
                }else{
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite);
                }
            }catch (Exception e){}
        }

        public HomeRecyclerViewAdapter() {
            imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth*3/5);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoArray.get(position);
            double distance = GeoHelper.getActualKilometer(myLocation.getLatitude(),myLocation.getLongitude(),pubInfo.latitude,pubInfo.longitude);
            String distanceString = Double.toString(distance);
            distanceString = distanceString.substring(0,distanceString.indexOf(".")+2);

            Picasso.with(Home.this).load(pubInfo.imageAddress[0]).into(holder.image);
            if(pubInfo.getFavorite()){
                holder.favorite.setImageResource(R.drawable.favorite_clicked);
            }else{
                holder.favorite.setImageResource(favorite);
            }
            holder.text1.setText(pubInfo.name+"," +distanceString+"km");
            holder.text2.setText(pubInfo.businessType);
            holder.text3.setText(pubInfo.address);


            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(Home.this,PubPage.class);
                intent.putExtra("info",pubInfo);
                startActivityForResult(intent,PubPage.REQUEST_OPENPUBPAGE);
            });


            holder.favorite.setOnClickListener(view -> {
                pubInfoArray.get(position).setFavorite(!pubInfoArray.get(position).getFavorite());
                if(pubInfoArray.get(position).getFavorite()){
                    holder.favorite.setImageResource(R.drawable.favorite_clicked);
                }else{
                    holder.favorite.setImageResource(R.drawable.favorite);
                }
            });

        }

        @Override
        public int getItemCount() {
            return pubInfoArray.size();
        }
    }
    boolean updatePubList=false;


    //************************MyPage Tab************************
    ProfileCircleImageView mypageProfileImage;
    TextView mypageProfileText1,mypageProfileText2,mypageProfileText3;
    ImageView mypageCurrentMembership;
    LinearLayout mypageMembership,mypageInquire,mypageSetting;
    RelativeLayout mypageCalendarContainer;
    DrinkCalendar mypageCalendar;


    //************************History Tab************************
    TextView historyPrice;
    JRecyclerView historyRecyclerView;
    ArrayList<PubInfo> historyInfoArray;
    HistoryRecyclerViewAdapter historyAdapter;
    private class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>{
        private ViewHolder lastClickedViewHolder=null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView dateAndDrink,name,address;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView)itemView.findViewById(R.id.historyItem_image);
                dateAndDrink = (TextView)itemView.findViewById(R.id.historyItem_dateAndDrink);
                name = (TextView)itemView.findViewById(R.id.historyItem_name);
                address = (TextView)itemView.findViewById(R.id.historyItem_address);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = historyInfoArray.get(position);

            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(Home.this,PubPage.class);
                intent.putExtra("info",pubInfo);
                startActivityForResult(intent,PubPage.REQUEST_OPENPUBPAGE);
            });
        }

        @Override
        public int getItemCount() {
            return historyInfoArray.size();
        }
    }


    //************************Event Tab************************
    private class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView dateAndDrink,name,address;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView)itemView.findViewById(R.id.historyItem_image);
                dateAndDrink = (TextView)itemView.findViewById(R.id.historyItem_dateAndDrink);
                name = (TextView)itemView.findViewById(R.id.historyItem_name);
                address = (TextView)itemView.findViewById(R.id.historyItem_address);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return historyInfoArray.size();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locationManager = new NMapLocationManager(this);
        locationManager.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {
            @Override
            public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
                if(updatePubList) {
                    onMyLocationUpdated(nGeoPoint);
                    updatePubList=false;
                }
//                locationManager.disableMyLocation();
                return false;
            }

            @Override
            public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

            }

            @Override
            public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

            }
        });

        upperBarLeftIcon = (ImageView)findViewById(R.id.home_upperBarLeftIcon);
        upperBarMap = (ImageView)findViewById(R.id.home_upperBarMapIcon);
        upperBarSearch = (ImageView)findViewById(R.id.home_upperBarSearchIcon);
        upperBarMainText = (TextView) findViewById(R.id.home_upperBarMainText);
        upperBarSubText = (TextView) findViewById(R.id.home_upperBarSubText);
        container = (RelativeLayout)findViewById(R.id.home_contentContainer);
        tapBar = (TapBar)findViewById(R.id.home_tapbar);


        upperBarSearch.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this,Search.class);
            startActivity(intent);
        });

        upperBarMap.setOnClickListener(view->{
            Intent intent = new Intent(Home.this,NearbyPlaces.class);
            startActivity(intent);
        });

        tapBar.setItems(new String[]{"홈","즐겨찾기","나의 한잔 기록","공지/이벤트","마이페이지"},
                new String[]{"home",
                        "favorite",
                        "history",
                        "event",
                        "profile"
                });

        tapBar.setListener(new TapBarItemClickListener() {
            @Override
            public void onClick(String title, int number) {
                switch (number){
                    case 0:
                        openHomeTab();
                        break;
                    case 1:
                        openMyFavoriteTab();
                        break;
                    case 2:
                        openHistoryTab();
                        break;
                    case 3:
                        openEventTab();
                        break;
                    case 4:
                        openMyPageTab();
                        break;
                }
            }

            @Override
            public void onClickStated(String title, int number) {
                if(number==4) {
                    upperBarLeftIcon.setVisibility(View.INVISIBLE);
                    upperBarMap.setVisibility(View.INVISIBLE);
                    upperBarSearch.setVisibility(View.INVISIBLE);
                    upperBarSubText.setVisibility(View.INVISIBLE);
                    upperBarMainText.setText("마이페이지");
                    container.removeAllViews();
                }
            }
        });

        if(StaticData.currentUser.expireYYYY==0){
            openMembershipPopup();
        }

        openHomeTab();
    }

    private void openMembershipPopup(){
        Dialog dialog = new Dialog(this);

        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.home_membershippopup,null);
        TextView no = (TextView)layout.findViewById(R.id.homeMembershipPopup_no);
        TextView yes = (TextView)layout.findViewById(R.id.homeMembershipPopup_yes);

        no.setOnClickListener(view -> dialog.cancel());
        yes.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this,Membership.class);
            startActivity(intent);
            dialog.cancel();
        });

        dialog.setContentView(layout);
        dialog.show();
    }


    private void openHomeTab(){
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.VISIBLE);
        upperBarMap.setVisibility(View.VISIBLE);
        upperBarSearch.setVisibility(View.VISIBLE);
        upperBarSubText.setVisibility(View.VISIBLE);

        upperBarMainText.setText("내 주변");

        updatePubList = true;

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_home,null);
        pubInfoRecyclerView = (JRecyclerView)layout.findViewById(R.id.home_homeRecycler);
        layout.setBackgroundColor(Color.WHITE);

        pubInfoArray=new ArrayList<>();
        homeAdapter = new HomeRecyclerViewAdapter();

        pubInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pubInfoRecyclerView.setAdapter(homeAdapter);
        pubInfoRecyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                JLog.v("...", "Refreshing Item Wow !");
                new Handler(getMainLooper()).post(()->{
                    updatePubList = true;
                    pubInfoArray=new ArrayList<>();
                    retrievePubList();
                    pubInfoRecyclerView.finishRefreshing();
                });
            }

            @Override
            public void onLoadMore() {
                JLog.v("...", "Last Item !");
                new Handler(getMainLooper()).post(()->{
                    retrievePubList();
                    pubInfoRecyclerView.finishLoadmore();
                });
            }
        });

        requestGPSPermission();

        container.addView(layout);
    }

    private void openMyFavoriteTab(){
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("즐겨찾기");


        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_favorite,null);
        pubInfoRecyclerView = (JRecyclerView)layout.findViewById(R.id.home_favoriteRecycler);
        layout.setBackgroundColor(Color.WHITE);

        pubInfoArray=new ArrayList<>();

        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter();

        pubInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pubInfoRecyclerView.setAdapter(adapter);


        container.addView(layout);


    }

    private void openHistoryTab(){
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("내가 마신 한잔");


        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_history,null);
        historyPrice = (TextView) layout.findViewById(R.id.history_price);
        historyRecyclerView = (JRecyclerView)layout.findViewById(R.id.history_recycler);
        layout.setBackgroundColor(Color.WHITE);

        historyInfoArray=new ArrayList<>();

        historyInfoArray.add(new PubInfo()); //TEST
        historyInfoArray.add(new PubInfo()); //TEST

        historyAdapter = new HistoryRecyclerViewAdapter();

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);

        container.addView(layout);
    }

    private void openEventTab(){
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("공지 / 이벤트");


        RelativeLayout layout = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.home_event,null);
        JRecyclerView recyclerView = (JRecyclerView)layout.findViewById(R.id.home_eventRecycler);

        historyInfoArray=new ArrayList<>();

        historyInfoArray.add(new PubInfo()); //TEST
        historyInfoArray.add(new PubInfo()); //TEST

        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        container.addView(layout);
    }

    private void openMyPageTab(){
        container.removeAllViews();

//        upperBarLeftIcon.setVisibility(View.INVISIBLE);
//        upperBarMap.setVisibility(View.INVISIBLE);
//        upperBarSearch.setVisibility(View.INVISIBLE);
//        upperBarSubText.setVisibility(View.INVISIBLE);
//
//        upperBarMainText.setText("마이페이지");

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_mypage,null);
        mypageProfileImage = (ProfileCircleImageView)layout.findViewById(R.id.mypage_profileImage);
        mypageProfileText1 = (TextView)layout.findViewById(R.id.mypage_profileText1);
        mypageProfileText2 = (TextView)layout.findViewById(R.id.mypage_profileText2);
        mypageProfileText3 = (TextView)layout.findViewById(R.id.mypage_profileText3);
        mypageCurrentMembership = (ImageView)layout.findViewById(R.id.mypage_currentMembership);
//        mypageCalendarContainer = (RelativeLayout)layout.findViewById(R.id.mypage_calendarContainer);
        mypageCalendar = (DrinkCalendar)layout.findViewById(R.id.mypage_calendar);
        mypageMembership = (LinearLayout)layout.findViewById(R.id.mypage_joinMembership);
        mypageInquire = (LinearLayout)layout.findViewById(R.id.mypage_inquire);
        mypageSetting = (LinearLayout)layout.findViewById(R.id.mypage_setting);

        Picasso.with(this).load(StaticData.currentUser.profileImageAddress).into(mypageProfileImage);
        mypageProfileText1.setText(StaticData.currentUser.name);

        mypageProfileImage.setOnClickListener(view -> {
            openProfilePhotoPopup();
        });


        mypageMembership.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this,Membership.class);
            startActivity(intent);
        });

//        LinearLayout.LayoutParams calendarContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayWidth*4/5);
//        mypageCalendar.setLayoutParams(calendarContainerParams);

        container.addView(layout);

    }


    //****Home Tab****
    private void retrievePubList(){
        if(tapBar.getCurrentlyFocusedTapNumber()!=0){
            JLog.v("tab moved!");
            return;
        }

        AsyncTask<Integer,Integer,Integer> async =new AsyncTask<Integer, Integer, Integer>() {
            HashMap<String,String> map;
            @Override
            protected Integer doInBackground(Integer... strings) {
                JLog.v("Log","retrieving!!");
                JLog.v("Lat",Double.toString(myLocation.getLatitude()));
                JLog.v("Lng",Double.toString(myLocation.getLongitude()));

                HashMap<String,String> temp = new HashMap<>();
                temp.put("at",Integer.toString(pubInfoArray.size()));
                temp.put("id_member",Long.toString(StaticData.currentUser.id));
                temp.put("user_lat",Double.toString(myLocation.getLatitude()));
                temp.put("user_lat",Double.toString(myLocation.getLongitude()));
                map = ServerConnectionHelper.connect("retrieve nearby places","nearbyplace",temp);

                if(map.get("datalefts")==null || map.get("datalefts").equals("FALSE")){
                    publishProgress(0);
                    return null;
                }
                int i=0;
                while(true){
                    try{
                        String num = Integer.toString(i++);
                        long id = Long.parseLong(map.get("id_place_"+num));
                        String name = map.get("name_place_"+num);
                        String address = map.get("address_place_"+num);
                        String imageAddress = map.get("imgadd_place_"+num);
                        boolean favorite=false;
                        if(map.get("likeornot_"+num).equals("1")){
                            favorite=true;
                        }
                        double lat = Double.parseDouble(map.get("lat_"+num));
                        double lng = Double.parseDouble(map.get("lng_"+num));

//                        JLog.v("retrieving number "+num);

                        pubInfoArray.add(new PubInfo(id,name,address,"주점",new String[]{imageAddress},favorite,lat,lng));
                    }catch (Exception e){
                        break;
                    }
                }
                publishProgress(1);

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if(values[0]==0){
                    pubInfoRecyclerView.setEnableLoadmore(false);
                }else if(values[0]==1){
                    homeAdapter.notifyDataSetChanged();
                }
            }
        };
        async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void requestGPSPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }else{
                getMyLocation();
            }
        }else{
            getMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getMyLocation();
    }

    private void getMyLocation(){
        try {
            locationManager.enableMyLocation(false);
            if(locationManager.getMyLocation()!=null){
                onMyLocationUpdated(locationManager.getMyLocation());
            }
        }catch (Exception e){
            e.printStackTrace();
            JLog.e("failed to retrieve my location with Naver location manager");
            myLocation = new NGeoPoint(37.518775,127.050081);
            retrievePubList();
        }
    }

    private void onMyLocationUpdated(NGeoPoint geoPoint){
        JLog.v("myLoc",Double.toString(geoPoint.getLatitude()));
        JLog.v("myLoc",Double.toString(geoPoint.getLongitude()));
        myLocation = locationManager.getMyLocation();
        retrievePubList();
    }


    //****My Page Tab****

    private void openProfilePhotoPopup(){
        Dialog dialog = new Dialog(this);

        LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.mypage_profilephoto_popup,null);
        TextView see = (TextView)layout.findViewById(R.id.profilePhotoPopup_see);
        TextView change = (TextView)layout.findViewById(R.id.profilePhotoPopup_change);
        TextView delete = (TextView)layout.findViewById(R.id.profilePhotoPopup_delete);

        see.setOnClickListener(view -> {
            ImageViewer.Builder viewer = new ImageViewer.Builder(Home.this,new String[]{StaticData.currentUser.profileImageAddress});
            viewer.show();
            dialog.cancel();
        });

        change.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, ImageCropper.IMAGE_CROP_IMAGESELECT);
            dialog.cancel();
        });

        delete.setOnClickListener(view -> {

        });

        dialog.setContentView(layout);

        dialog.show();
    }


    //****Other Tab****






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ImageCropper.IMAGE_CROP_IMAGESELECT){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                Intent intent =  new Intent(Home.this,ImageCropper.class);
                intent.setData(uri);
                startActivityForResult(intent,ImageCropper.IMAGE_CROP_REQUEST);
            }
        }else if(requestCode == ImageCropper.IMAGE_CROP_REQUEST){
            if(resultCode == RESULT_OK){
                Drawable drawable = ImageCropper.croppedImage;
            }
        }else if(requestCode == PubPage.REQUEST_OPENPUBPAGE){
            if(resultCode == PubPage.RESULT_FAVORITECHANGED){
                switch (tapBar.getCurrentlyFocusedTapNumber()){
                    case 0:
                    case 1:
                        homeAdapter.setFavoriteButton(data.getBooleanExtra("favorite",false));
                        break;
                }
            }
        }
    }


}
