package kotel.hanzan;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.DrinkInfo;
import kotel.hanzan.Data.PubHistoryInfo;
import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.NumericHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.listener.TapBarItemClickListener;
import kotel.hanzan.view.DrinkCalendar;
import kotel.hanzan.view.JRecyclerView;
import kotel.hanzan.view.ProfileCircleImageView;
import kotel.hanzan.view.TapBar;

public class Home extends AppCompatActivity {
    private TapBar tapBar;
    private RelativeLayout container;

    private ImageView upperBarLeftIcon, upperBarMap, upperBarSearch, upperBarFilter;
    private TextView upperBarMainText, upperBarSubText;

    private LocationHelper locationHelper = new LocationHelper();
    private NGeoPoint myLocation;

    //************************Home Tab************************
    private RelativeLayout homeLayout;
    private JRecyclerView pubInfoRecyclerView;
    private ArrayList<PubInfo> pubInfoArray = new ArrayList<>();
    private HomeRecyclerViewAdapter homeAdapter = new HomeRecyclerViewAdapter();

    private class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
        LinearLayout.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder = null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite, drinkProvidable;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                image.setLayoutParams(imageParams);
                drinkProvidable = (ImageView) itemView.findViewById(R.id.pubitem_drinkProvidable);
                favorite = (ImageView) itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView) itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView) itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView) itemView.findViewById(R.id.pubitem_text3);
            }
        }

        public void setFavoriteButton(boolean clicked) {
            try {
                if (clicked) {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_selected);
                } else {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            } catch (Exception e) {
            }
        }

        public HomeRecyclerViewAdapter() {
            imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth * 3 / 5);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoArray.get(position);
            double distance = GeoHelper.getActualKilometer(myLocation.getLatitude(), myLocation.getLongitude(), pubInfo.latitude, pubInfo.longitude);
            String distanceString = Double.toString(distance);
            distanceString = distanceString.substring(0, distanceString.indexOf(".") + 2) + "km";

            Picasso.with(Home.this).load(pubInfo.imageAddress.get(0)).into(holder.image);
            switch (pubInfo.drinkProvideType){
                case 1:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_1).into(holder.drinkProvidable);
                    break;
                case 2:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_2).into(holder.drinkProvidable);
                    break;
                case 3:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_infinite).into(holder.drinkProvidable);
                    break;
            }

            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_selected);
            } else {
                holder.favorite.setImageResource(R.drawable.favorite_unselected);
            }
            holder.text1.setText(pubInfo.name + "  " + distanceString);
            holder.text2.setText(pubInfo.businessType);
            holder.text3.setText(pubInfo.address);


            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(Home.this, PubPage.class);
                intent.putExtra("info", pubInfo);
                startActivityForResult(intent, PubPage.REQUEST_OPENPUBPAGE);
            });


            holder.favorite.setOnClickListener(view -> {
                pubInfoArray.get(position).setFavorite(!pubInfoArray.get(position).getFavorite());
                if (pubInfoArray.get(position).getFavorite()) {
                    holder.favorite.setImageResource(R.drawable.favorite_selected);
                } else {
                    holder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            });

        }

        @Override
        public int getItemCount() {
            return pubInfoArray.size();
        }
    }

    private boolean isFirstGPSRequest = true;
    //******FILTER******
    private boolean filterLayoutIsVisible = false;
    private RelativeLayout filterLayout;
    private boolean filter_checkbox1Checked = false, filter_checkbox2Checked = false, filter_checkbox3Checked = false;


    //************************My Favorite Tab************************
    private JRecyclerView pubInfoFavoriteRecyclerView;
    private ArrayList<PubInfo> pubInfoFavoriteArray = new ArrayList<>();
    FavoriteRecyclerViewAdapter pubInfoFavoriteAdapter = new FavoriteRecyclerViewAdapter();
    private class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder> {
        LinearLayout.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder = null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite, drinkProvidable;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                image.setLayoutParams(imageParams);
                drinkProvidable = (ImageView)itemView.findViewById(R.id.pubitem_drinkProvidable);
                favorite = (ImageView) itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView) itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView) itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView) itemView.findViewById(R.id.pubitem_text3);
            }
        }

        public void setFavoriteButton(boolean clicked) {
            try {
                if (clicked) {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_selected);
                } else {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            } catch (Exception e) {
            }
        }

        public FavoriteRecyclerViewAdapter() {
            imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth * 3 / 5);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoFavoriteArray.get(position);

            Picasso.with(Home.this).load(pubInfo.imageAddress.get(0)).into(holder.image);
            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_selected);
            } else {
                holder.favorite.setImageResource(R.drawable.favorite_unselected);
            }
            holder.text1.setText(pubInfo.name);
            holder.text2.setText(pubInfo.businessType);
            holder.text3.setText(pubInfo.address);

            switch (pubInfo.drinkProvideType){
                case 1:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_1).into(holder.drinkProvidable);
                    break;
                case 2:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_2).into(holder.drinkProvidable);
                    break;
                case 3:
                    Picasso.with(Home.this).load(R.drawable.drinkprovidable_infinite).into(holder.drinkProvidable);
                    break;
            }

            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(Home.this, PubPage.class);
                intent.putExtra("info", pubInfo);
                startActivityForResult(intent, PubPage.REQUEST_OPENPUBPAGE);
            });

            holder.favorite.setOnClickListener(view -> {
                pubInfoFavoriteArray.get(position).setFavorite(!pubInfoFavoriteArray.get(position).getFavorite());
                if (pubInfoFavoriteArray.get(position).getFavorite()) {
                    holder.favorite.setImageResource(R.drawable.favorite_selected);
                } else {
                    holder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            });

        }

        @Override
        public int getItemCount() {
            return pubInfoFavoriteArray.size();
        }
    }


    //************************History Tab************************
    private TextView historyPrice;
    private JRecyclerView historyRecyclerView;
    private ArrayList<PubHistoryInfo> historyInfoArray = new ArrayList<>();
    private HistoryRecyclerViewAdapter historyAdapter = new HistoryRecyclerViewAdapter();

    private class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
        private ViewHolder lastClickedViewHolder = null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, drinkImage;
            TextView dateAndDrink, name, address;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.historyItem_image);
                drinkImage = (ImageView) itemView.findViewById(R.id.historyItem_drinkImage);
                dateAndDrink = (TextView) itemView.findViewById(R.id.historyItem_dateAndDrink);
                name = (TextView) itemView.findViewById(R.id.historyItem_name);
                address = (TextView) itemView.findViewById(R.id.historyItem_address);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubHistoryInfo pubInfo = historyInfoArray.get(position);

            Picasso.with(getApplicationContext()).load(pubInfo.imageAddress.get(0)).into(holder.image);
            String dateText = Integer.toString(pubInfo.YYYY) + "/" + Integer.toString(pubInfo.MM) + "/" + Integer.toString(pubInfo.DD);
            holder.dateAndDrink.setText(dateText + "에 " + pubInfo.drinkInfo.drinkName + "한 잔");
            holder.name.setText(pubInfo.name);
            holder.address.setText(pubInfo.address);

            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(Home.this, PubPage.class);
                intent.putExtra("info", pubInfo);
                startActivityForResult(intent, PubPage.REQUEST_OPENPUBPAGE);
            });
        }

        @Override
        public int getItemCount() {
            return historyInfoArray.size();
        }
    }


    //************************Event Tab************************
    private class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView dateAndDrink, name, address;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.historyItem_image);
                dateAndDrink = (TextView) itemView.findViewById(R.id.historyItem_dateAndDrink);
                name = (TextView) itemView.findViewById(R.id.historyItem_name);
                address = (TextView) itemView.findViewById(R.id.historyItem_address);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
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


    //************************MyPage Tab************************
    private ProfileCircleImageView mypageProfileImage;
    private TextView mypageProfileText1, mypageProfileText2, mypageProfileText3;
    private ImageView mypageCurrentMembership;
    private LinearLayout mypageMembership, mypageInquire, mypageSetting, mypageLogout;
    private RelativeLayout mypageCalendarContainer;
    private DrinkCalendar mypageCalendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        upperBarLeftIcon = (ImageView) findViewById(R.id.home_upperBarLeftIcon);
        upperBarMap = (ImageView) findViewById(R.id.home_upperBarMapIcon);
        upperBarSearch = (ImageView) findViewById(R.id.home_upperBarSearchIcon);
        upperBarFilter = (ImageView) findViewById(R.id.home_upperBarFilterIcon);
        upperBarMainText = (TextView) findViewById(R.id.home_upperBarMainText);
        upperBarSubText = (TextView) findViewById(R.id.home_upperBarSubText);
        container = (RelativeLayout) findViewById(R.id.home_contentContainer);
        tapBar = (TapBar) findViewById(R.id.home_tapbar);

        upperBarFilter.setImageDrawable(filter_checkbox1Checked || filter_checkbox2Checked || filter_checkbox3Checked ? getDrawable(R.drawable.filtericon_active) : getDrawable(R.drawable.filtericon_inactive));

        upperBarFilter.setOnClickListener(view -> {
            if (filterLayoutIsVisible) {
                container.removeView(filterLayout);
                filterLayoutIsVisible = false;
            } else {
                openFilter();
            }

        });

        upperBarSearch.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Search.class);
            startActivity(intent);
        });

        upperBarMap.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, NearbyPlaces.class);
            startActivity(intent);
        });

        tapBar.setItems(new String[]{"홈", "즐겨찾기", "나의 한잔 기록", "공지/이벤트", "마이페이지"},
                new String[]{"home",
                        "favorite",
                        "history",
                        "event",
                        "profile"
                });

        tapBar.setListener(new TapBarItemClickListener() {
            @Override
            public void onClick(String title, int number) {
                filterLayoutIsVisible = false;
                switch (number) {
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
            public void onClickStarted(String title, int number) {
                if (number == 4) {
                    upperBarLeftIcon.setVisibility(View.INVISIBLE);
                    upperBarMap.setVisibility(View.INVISIBLE);
                    upperBarSearch.setVisibility(View.INVISIBLE);
                    upperBarSubText.setVisibility(View.INVISIBLE);
                    upperBarFilter.setVisibility(View.INVISIBLE);
                    upperBarMainText.setText("마이페이지");
                    container.removeAllViews();
                }
            }
        });

        if (StaticData.currentUser.expireYYYY == 0) {
            openMembershipPopup();
        }

        openHomeTab();
    }

    private void openMembershipPopup() {
        Dialog dialog = new Dialog(this);

        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.home_membershippopup, null);
        TextView no = (TextView) layout.findViewById(R.id.homeMembershipPopup_no);
        TextView yes = (TextView) layout.findViewById(R.id.homeMembershipPopup_yes);

        no.setOnClickListener(view -> dialog.cancel());
        yes.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Membership.class);
            startActivity(intent);
            dialog.cancel();
        });

        dialog.setContentView(layout);
        dialog.show();
    }


    private void openHomeTab() {
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.VISIBLE);
        upperBarMap.setVisibility(View.VISIBLE);
        upperBarSearch.setVisibility(View.VISIBLE);
        upperBarSubText.setVisibility(View.VISIBLE);
        upperBarFilter.setVisibility(View.VISIBLE);

        upperBarMainText.setText("내 주변");

        homeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_home, null);
        pubInfoRecyclerView = (JRecyclerView) homeLayout.findViewById(R.id.home_homeRecycler);
        homeLayout.setBackgroundColor(Color.WHITE);

        pubInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pubInfoRecyclerView.setAdapter(homeAdapter);
        pubInfoRecyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                JLog.v("...", "Refreshing Item Wow !");
                if (isFirstGPSRequest) {
                    isFirstGPSRequest = false;

                    pubInfoArray.clear();
                    homeAdapter.notifyDataSetChanged();
                    requestGPSPermission();
                }else{
                    retrievePubList(true);
                }

            }

            @Override
            public void onLoadMore() {
                JLog.v("...", "Last Item !");
                retrievePubList(false);
            }
        });

        pubInfoRecyclerView.startRefresh();

        container.addView(homeLayout);
    }

    private void openMyFavoriteTab() {
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);
        upperBarFilter.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("즐겨찾기");

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_favorite, null);
        pubInfoFavoriteRecyclerView = (JRecyclerView) layout.findViewById(R.id.home_favoriteRecycler);
        layout.setBackgroundColor(Color.WHITE);

        pubInfoFavoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pubInfoFavoriteRecyclerView.setAdapter(pubInfoFavoriteAdapter);
        pubInfoFavoriteRecyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                retrieveFavoriteHistoryList(true);
            }

            @Override
            public void onLoadMore() {
                retrieveFavoriteHistoryList(false);
            }
        });

        pubInfoFavoriteRecyclerView.startRefresh();

        container.addView(layout);
    }

    private void openHistoryTab() {
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);
        upperBarFilter.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("내가 마신 한잔");


        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_history, null);
        historyPrice = (TextView) layout.findViewById(R.id.history_price);
        historyRecyclerView = (JRecyclerView) layout.findViewById(R.id.history_recycler);
        layout.setBackgroundColor(Color.WHITE);

        historyInfoArray = new ArrayList<>();


        historyAdapter = new HistoryRecyclerViewAdapter();

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
        historyRecyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                retrieveHistoryList(true);
            }

            @Override
            public void onLoadMore() {
                retrieveHistoryList(false);
            }
        });

        historyRecyclerView.startRefresh();

        container.addView(layout);
    }

    private void openEventTab() {
        container.removeAllViews();

        upperBarLeftIcon.setVisibility(View.INVISIBLE);
        upperBarMap.setVisibility(View.INVISIBLE);
        upperBarSearch.setVisibility(View.INVISIBLE);
        upperBarSubText.setVisibility(View.INVISIBLE);
        upperBarFilter.setVisibility(View.INVISIBLE);

        upperBarMainText.setText("공지 / 이벤트");


        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_event, null);
        JRecyclerView recyclerView = (JRecyclerView) layout.findViewById(R.id.home_eventRecycler);

        historyInfoArray = new ArrayList<>();

        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        container.addView(layout);
    }

    private void openMyPageTab() {
        container.removeAllViews();

//        upperBarLeftIcon.setVisibility(View.INVISIBLE);
//        upperBarMap.setVisibility(View.INVISIBLE);
//        upperBarSearch.setVisibility(View.INVISIBLE);
//        upperBarSubText.setVisibility(View.INVISIBLE);
//        upperBarFilter.setVisibility(View.INVISIBLE);
//
//        upperBarMainText.setText("마이페이지");

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_mypage, null);
        mypageProfileImage = (ProfileCircleImageView) layout.findViewById(R.id.mypage_profileImage);
        mypageProfileText1 = (TextView) layout.findViewById(R.id.mypage_profileText1);
        mypageProfileText2 = (TextView) layout.findViewById(R.id.mypage_profileText2);
        mypageProfileText3 = (TextView) layout.findViewById(R.id.mypage_profileText3);
        mypageCurrentMembership = (ImageView) layout.findViewById(R.id.mypage_currentMembership);
//        mypageCalendarContainer = (RelativeLayout)layout.findViewById(R.id.mypage_calendarContainer);
        mypageCalendar = (DrinkCalendar) layout.findViewById(R.id.mypage_calendar);
        mypageMembership = (LinearLayout) layout.findViewById(R.id.mypage_joinMembership);
        mypageInquire = (LinearLayout) layout.findViewById(R.id.mypage_inquire);
        mypageSetting = (LinearLayout) layout.findViewById(R.id.mypage_setting);
        mypageLogout = (LinearLayout) layout.findViewById(R.id.mypage_logout);


        Picasso.with(this).load(StaticData.currentUser.profileImageAddress).into(mypageProfileImage);
        mypageProfileText1.setText(StaticData.currentUser.name);

        mypageProfileImage.setOnClickListener(view -> {
            openProfilePhotoPopup();
        });


        mypageMembership.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Membership.class);
            startActivity(intent);
        });

        mypageLogout.setOnClickListener(view -> logoutToLoginPage());

//        LinearLayout.LayoutParams calendarContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayWidth*4/5);
//        mypageCalendar.setLayoutParams(calendarContainerParams);

        container.addView(layout);

    }


    //****Home Tab****
    private synchronized void retrievePubList(boolean clearArray) {
        if(clearArray) {
            pubInfoArray.clear();
        }
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();

            map.put("at", Integer.toString(pubInfoArray.size()));
            map.put("id_member", Long.toString(StaticData.currentUser.id));
            map.put("user_lat", Double.toString(myLocation.getLatitude()));
            map.put("user_lat", Double.toString(myLocation.getLongitude()));
            String filter1,filter2,filter3;
            filter1 = filter_checkbox1Checked ? "TRUE" : "FALSE";
            filter2 = filter_checkbox2Checked ? "TRUE" : "FALSE";
            filter3 = filter_checkbox3Checked ? "TRUE" : "FALSE";
            map.put("filter_1",filter1);
            map.put("filter_2",filter2);
            map.put("filter_n",filter3);
            map = ServerConnectionHelper.connect("retrieve nearby places", "nearbyplace", map);

            int i = 0;
            while (true) {
                try {
                    String num = Integer.toString(i++);
                    long id = Long.parseLong(map.get("id_place_" + num));
                    String name = map.get("name_place_" + num);
                    String address = map.get("address_place_" + num);
                    String imageAddress = map.get("imgadd_place_" + num);
                    boolean favorite = false;
                    if (map.get("like_" + num).equals("TRUE")) {
                        favorite = true;
                    }
                    double lat = Double.parseDouble(map.get("lat_" + num));
                    double lng = Double.parseDouble(map.get("lng_" + num));
                    int drinkProvideType = Integer.parseInt(map.get("alcoholpertable_" + num));

//                        JLog.v("retrieving number "+num);

                    pubInfoArray.add(new PubInfo(id, name, address, "주점", imageAddress, favorite, lat, lng, drinkProvideType));
                } catch (Exception e) {
                    break;
                }
            }

            String dataleft = map.get("datalefts");

            new Handler(getMainLooper()).post(() -> {
                pubInfoRecyclerView.finishRefreshing();
                homeAdapter.notifyDataSetChanged();
                if(dataleft.equals("TRUE")){
                    pubInfoRecyclerView.finishLoadmore();
                }
            });
        }).start();

    }

    private void requestGPSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                getMyLocation();
            }
        } else {
            getMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        JLog.v("PERMISSION GRANTED");
        getMyLocation();
    }

    private void openFilter() {
        if (filterLayoutIsVisible) {
            return;
        }

        filterLayoutIsVisible = true;

        final boolean[] checkbox1Checked = {filter_checkbox1Checked};
        final boolean[] checkbox2Checked = {filter_checkbox2Checked};
        final boolean[] checkbox3Checked = {filter_checkbox3Checked};
        final boolean[] applyIsActive = {checkbox1Checked[0] || checkbox2Checked[0] || checkbox3Checked[0]};
        final boolean[] removeIsActive = {filter_checkbox1Checked || filter_checkbox2Checked || filter_checkbox3Checked};

        filterLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.home_filter, null);
        ImageView checkbox1 = (ImageView) filterLayout.findViewById(R.id.filter_checkbox1);
        ImageView checkbox2 = (ImageView) filterLayout.findViewById(R.id.filter_checkbox2);
        ImageView checkbox3 = (ImageView) filterLayout.findViewById(R.id.filter_checkbox3);
        TextView textbox1 = (TextView) filterLayout.findViewById(R.id.filter_textbox1);
        TextView textbox2 = (TextView) filterLayout.findViewById(R.id.filter_textbox2);
        TextView textbox3 = (TextView) filterLayout.findViewById(R.id.filter_textbox3);
        TextView filterApply = (TextView) filterLayout.findViewById(R.id.filter_apply);
        TextView filterRemove = (TextView) filterLayout.findViewById(R.id.filter_remove);
        View filterCancel = filterLayout.findViewById(R.id.filter_cancel);

        checkbox1.setImageDrawable(checkbox1Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));
        checkbox2.setImageDrawable(checkbox2Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));
        checkbox3.setImageDrawable(checkbox3Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));

        filterApply.setBackground(applyIsActive[0] ? getDrawable(R.drawable.button_active) : getDrawable(R.drawable.button_inactive));
        filterRemove.setBackground(removeIsActive[0] ? getDrawable(R.drawable.button_active) : getDrawable(R.drawable.button_inactive));
//        filterApply.setTextColor(applyIsActive[0] ? getResources().getColor(R.color.mainColor_dark) : getResources().getColor(R.color.inactive));
//        filterRemove.setTextColor(removeIsActive[0] ? getResources().getColor(R.color.mainColor_dark) : getResources().getColor(R.color.inactive));


        textbox1.setOnClickListener(view -> checkbox1.callOnClick());
        textbox2.setOnClickListener(view -> checkbox2.callOnClick());
        textbox3.setOnClickListener(view -> checkbox3.callOnClick());

        checkbox1.setOnClickListener(view -> {
            checkbox1Checked[0] = !checkbox1Checked[0];
            checkbox1.setImageDrawable(checkbox1Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));
            applyIsActive[0] = checkbox1Checked[0] || checkbox2Checked[0] || checkbox3Checked[0];
            filterApply.setBackground(applyIsActive[0] ? getDrawable(R.drawable.button_active) : getDrawable(R.drawable.button_inactive));
//            filterApply.setTextColor(applyIsActive[0] ? getResources().getColor(R.color.mainColor_dark) : getResources().getColor(R.color.inactive));
        });

        checkbox2.setOnClickListener(view -> {
            checkbox2Checked[0] = !checkbox2Checked[0];
            checkbox2.setImageDrawable(checkbox2Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));
            applyIsActive[0] = checkbox1Checked[0] || checkbox2Checked[0] || checkbox3Checked[0];
            filterApply.setBackground(applyIsActive[0] ? getDrawable(R.drawable.button_active) : getDrawable(R.drawable.button_inactive));
//            filterApply.setTextColor(applyIsActive[0] ? getResources().getColor(R.color.mainColor_dark) : getResources().getColor(R.color.inactive));
        });

        checkbox3.setOnClickListener(view -> {
            checkbox3Checked[0] = !checkbox3Checked[0];
            checkbox3.setImageDrawable(checkbox3Checked[0] ? getDrawable(R.drawable.checkbox_checked) : getDrawable(R.drawable.checkbox_unchecked));
            applyIsActive[0] = checkbox1Checked[0] || checkbox2Checked[0] || checkbox3Checked[0];
            filterApply.setBackground(applyIsActive[0] ? getDrawable(R.drawable.button_active) : getDrawable(R.drawable.button_inactive));
//            filterApply.setTextColor(applyIsActive[0] ? getResources().getColor(R.color.mainColor_dark) : getResources().getColor(R.color.inactive));
        });

        filterApply.setOnClickListener(view -> {
            if (applyIsActive[0]) {
                filter_checkbox1Checked = checkbox1Checked[0];
                filter_checkbox2Checked = checkbox2Checked[0];
                filter_checkbox3Checked = checkbox3Checked[0];

                upperBarFilter.setImageDrawable(filter_checkbox1Checked || filter_checkbox2Checked || filter_checkbox3Checked ? getDrawable(R.drawable.filtericon_active) : getDrawable(R.drawable.filtericon_inactive));

                filterLayoutIsVisible = false;
                container.removeView(filterLayout);

                pubInfoArray.clear();
                retrievePubList(true);
            }
        });

        filterRemove.setOnClickListener(view -> {
            if (removeIsActive[0]) {
                filter_checkbox1Checked = false;
                filter_checkbox2Checked = false;
                filter_checkbox3Checked = false;

                upperBarFilter.setImageDrawable(getDrawable(R.drawable.filtericon_inactive));

                filterLayoutIsVisible = false;
                container.removeView(filterLayout);

                pubInfoArray.clear();
                retrievePubList(true);
            }
        });

        filterCancel.setOnClickListener(view -> {
            filterLayoutIsVisible = false;
            container.removeView(filterLayout);
        });


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        container.addView(filterLayout, params);
    }

    private void getMyLocation() {
//        locationHelper = new LocationHelper();

        locationHelper.getMyLocationOnlyOneTime(this, new LocationHelperListener() {
            @Override
            public void onSearchingStarted() {

            }

            @Override
            public void onSearchingEnded() {

            }

            @Override
            public void onLocationFound(NGeoPoint nGeoPoint) {
                pubInfoArray.clear();
                JLog.v("Normal GPS calling");
                myLocation = nGeoPoint;
                retrievePubList(true);
            }

            @Override
            public void onLocationTimeout() {
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), "현재 위치를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                myLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), "현재 위치를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                myLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onHasNoLocationPermission() {
                pubInfoArray.clear();
                JLog.e("GPS permission has denied");
                myLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onGpsIsOff() {
                pubInfoArray.clear();
                JLog.e("GPS is off");
                myLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }
        });
    }


    //****Favorite Tab****

    private synchronized void retrieveFavoriteHistoryList(boolean clearArray){
        if(clearArray) {
            pubInfoFavoriteArray.clear();
        }
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();

            map.put("at", Integer.toString(pubInfoFavoriteArray.size()));
            map.put("id_member", Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving favorited drink histories", "likehistory", map);

            int i = 0;
            while (true) {
                try {
                    String num = Integer.toString(i++);
                    long id = Long.parseLong(map.get("id_place_" + num));
                    String name = map.get("name_place_" + num);
                    String address = map.get("address_place_" + num);
                    String imageAddress = map.get("imgadd_place_" + num);
                    double lat = Double.parseDouble(map.get("lat_" + num));
                    double lng = Double.parseDouble(map.get("lng_" + num));
                    int drinkProvideType = Integer.parseInt(map.get("alcoholpertable_" + num));

//                        JLog.v("retrieving number "+num);

                    pubInfoFavoriteArray.add(new PubInfo(id, name, address, "주점", imageAddress, true, lat, lng, drinkProvideType));
                } catch (Exception e) {
                    break;
                }
            }

            String dataleft = map.get("datalefts");
            new Handler(getMainLooper()).post(() -> {
                pubInfoFavoriteRecyclerView.finishRefreshing();
                pubInfoFavoriteAdapter.notifyDataSetChanged();
                if(dataleft.equals("TRUE")){
                    pubInfoFavoriteRecyclerView.finishLoadmore();
                }
            });
        }).start();
    }


    //****History Tab****

    private synchronized void retrieveHistoryList(boolean clearArray){
        if(clearArray){
            historyInfoArray.clear();
        }
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();

            map.put("at", Integer.toString(historyInfoArray.size()));
            map.put("id_member", Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving drink histories", "drinkhistory", map);

            int i = 0;
            while (true) {
                try {
                    String num = Integer.toString(i++);
                    long id = Long.parseLong(map.get("id_place_" + num));
                    String name = map.get("name_place_" + num);
                    String address = map.get("address_place_" + num);
                    String imageAddress = map.get("imgadd_place_" + num);
                    boolean favorite = false;
                    if (map.get("like_" + num).equals("TRUE")) {
                        favorite = true;
                    }
                    double lat = Double.parseDouble(map.get("lat_" + num));
                    double lng = Double.parseDouble(map.get("lng_" + num));
                    int drinkProvideType = Integer.parseInt(map.get("alcoholpertable_" + num));

                    String dateString = map.get("date_visit_" + num);
                    int year = Integer.parseInt(dateString.substring(0,4));
                    int month = Integer.parseInt(dateString.substring(4,6));
                    int day = Integer.parseInt(dateString.substring(6,8));

                    String drinkName = map.get("category_drink_" + num);
                    String drinkCategory = map.get("name_drink_" + num);

//                        JLog.v("retrieving number "+num);

                    PubInfo pubInfo = new PubInfo(id, name, address, "주점", imageAddress, favorite, lat, lng, drinkProvideType);
                    DrinkInfo drinkInfo = new DrinkInfo(drinkCategory,drinkName);
                    historyInfoArray.add(new PubHistoryInfo(pubInfo,drinkInfo,year,month,day));
                } catch (Exception e) {
                    break;
                }
            }

            String totalSavingCost = NumericHelper.toMoneyFormat(map.get("totalsavingcost"));
            String dataLefts = map.get("datalefts");

            new Handler(getMainLooper()).post(() -> {
                try {
                    historyPrice.setText(totalSavingCost+"원");
                }catch (Exception e){}
                historyAdapter.notifyDataSetChanged();
                historyRecyclerView.finishRefreshing();
                if (dataLefts.equals("TRUE")) {
                    historyRecyclerView.finishLoadmore();
                }
            });
        }).start();
    }


    //****My Page Tab****

    private void openProfilePhotoPopup() {
        Dialog dialog = new Dialog(this);

        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.mypage_profilephoto_popup, null);
        TextView see = (TextView) layout.findViewById(R.id.profilePhotoPopup_see);
        TextView change = (TextView) layout.findViewById(R.id.profilePhotoPopup_change);
        TextView delete = (TextView) layout.findViewById(R.id.profilePhotoPopup_delete);

        see.setOnClickListener(view -> {
            ImageViewer.Builder viewer = new ImageViewer.Builder(Home.this, new String[]{StaticData.currentUser.profileImageAddress});
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

    private void logoutToLoginPage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("정말 로그아웃 할래요?");
        dialogBuilder.setNegativeButton("아니요", null);
        dialogBuilder.setPositiveButton("네", (dialogInterface, i) -> {
            AccessToken.setCurrentAccessToken(null);
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            finishAffinity();
        });
        dialogBuilder.create().show();

    }


    //****Other Tab****




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageCropper.IMAGE_CROP_IMAGESELECT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Intent intent = new Intent(Home.this, ImageCropper.class);
                intent.setData(uri);
                startActivityForResult(intent, ImageCropper.IMAGE_CROP_REQUEST);
            }
        } else if (requestCode == ImageCropper.IMAGE_CROP_REQUEST) {
            if (resultCode == RESULT_OK) {
                Drawable drawable = ImageCropper.croppedImage;
            }
        } else if (requestCode == PubPage.REQUEST_OPENPUBPAGE) {
            if (resultCode == PubPage.RESULT_FAVORITECHANGED) {
                switch (tapBar.getCurrentlyFocusedTapNumber()) {
                    case 0:
                        homeAdapter.setFavoriteButton(data.getBooleanExtra("favorite", false));
                        break;
                    case 1:
                        pubInfoFavoriteAdapter.setFavoriteButton(data.getBooleanExtra("favorite", false));
                        break;
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try{
            pubInfoRecyclerView.finishRefreshing();
        }catch (Exception e){}

//        locationHelper.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }



    boolean finishIfBackButtonClickedOnceMore = false;
    @Override
    public void onBackPressed() {
        if (filterLayoutIsVisible) {
            container.removeView(filterLayout);
            filterLayoutIsVisible = false;
        } else {
            if(finishIfBackButtonClickedOnceMore){
                super.onBackPressed();
            }else{
                Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르면 종료합니다",Toast.LENGTH_SHORT).show();
                finishIfBackButtonClickedOnceMore = true;
                new Thread(()->{
                    try{
                        Thread.sleep(3000);
                        finishIfBackButtonClickedOnceMore = false;
                    }catch (Exception e){}
                }).start();
            }


//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("정말 한잔을 종료하실건가요?");
//            builder.setNegativeButton("아니요",null);
//            builder.setPositiveButton("네", (dialogInterface, i) -> {
//                super.onBackPressed();
//            });
//            builder.create().show();
        }
    }


}
