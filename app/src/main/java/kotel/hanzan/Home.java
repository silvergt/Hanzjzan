package kotel.hanzan;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.DrinkInfo;
import kotel.hanzan.Data.EventInfo;
import kotel.hanzan.Data.PubHistoryInfo;
import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.AssetImageHelper;
import kotel.hanzan.function.CalendarHelper;
import kotel.hanzan.function.DrawableHelper;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.NumericHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.listener.TapBarItemClickListener;
import kotel.hanzan.view.DrinkCalendar;
import kotel.hanzan.view.JCheckBox;
import kotel.hanzan.view.JRecyclerView;
import kotel.hanzan.view.Loading;
import kotel.hanzan.view.ProfileCircleImageView;
import kotel.hanzan.view.TapBar;

public class Home extends AppCompatActivity {
    private TapBar tapBar;
    private RelativeLayout container;

    private ImageView upperBarLeftIcon, upperBarMap, upperBarSearch, upperBarFilter, upperBarDropdown;
    private TextView upperBarMainText, upperBarSubText;

    private Loading loading;

    private LocationHelper locationHelper = new LocationHelper();

    private InputMethodManager inputMethodManager;

    //************************Home Tab************************
    private RelativeLayout homeLayout;
    private JRecyclerView pubInfoRecyclerView;
    private ArrayList<PubInfo> pubInfoArray = new ArrayList<>();
    private HomeRecyclerViewAdapter homeAdapter = new HomeRecyclerViewAdapter();

    private class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
        LinearLayout.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder = null;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                image.setLayoutParams(imageParams);
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
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoArray.get(position);

            String distanceString = "";

            if(myLocationIsAvailable){
                double distance = GeoHelper.getActualKilometer(StaticData.myLatestLocation.getLatitude(), StaticData.myLatestLocation.getLongitude(), pubInfo.latitude, pubInfo.longitude);
                distanceString = GeoHelper.getDistanceString(distance);
            }

            Picasso.with(Home.this).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(holder.image);

            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_selected);
            } else {
                holder.favorite.setImageResource(R.drawable.favorite_unselected);
            }
            holder.text1.setText(pubInfo.name + "  " + distanceString);
            holder.text2.setText(pubInfo.district);
            holder.text3.setText(pubInfo.address);


            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
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
    private boolean myLocationIsAvailable = false;
        //******FILTER******
        private boolean filterLayoutIsVisible = false;
        private RelativeLayout filterLayout;
        private JCheckBox checkbox1,checkbox2,checkbox3,checkbox4,checkbox5,checkbox6,checkbox7;
        private boolean filter_checkbox1Checked = false, filter_checkbox2Checked = false, filter_checkbox3Checked = false,
                filter_checkbox4Checked = false, filter_checkbox5Checked = false, filter_checkbox6Checked = false,
                filter_checkbox7Checked = false;


    //************************My Favorite Tab************************
    private JRecyclerView pubInfoFavoriteRecyclerView;
    private ArrayList<PubInfo> pubInfoFavoriteArray = new ArrayList<>();
    FavoriteRecyclerViewAdapter pubInfoFavoriteAdapter = new FavoriteRecyclerViewAdapter();
    private class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder> {
        LinearLayout.LayoutParams imageParams;
        private ViewHolder lastClickedViewHolder = null;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                image.setLayoutParams(imageParams);
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
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoFavoriteArray.get(position);

            Picasso.with(Home.this).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(holder.image);
            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_selected);
            } else {
                holder.favorite.setImageResource(R.drawable.favorite_unselected);
            }
            holder.text1.setText(pubInfo.name);
            holder.text2.setText(pubInfo.district);
            holder.text3.setText(pubInfo.address);

            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
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
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubHistoryInfo pubInfo = historyInfoArray.get(position);

            Picasso.with(getApplicationContext()).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(holder.image);
            String dateText = Integer.toString(pubInfo.YYYY) + "/" + Integer.toString(pubInfo.MM) + "/" + Integer.toString(pubInfo.DD);
            holder.dateAndDrink.setText(dateText + " - '" + pubInfo.drinkInfo.drinkName + "'");
            holder.name.setText(pubInfo.name);
            holder.address.setText(pubInfo.address);
            JLog.v(historyInfoArray.get(position).drinkInfo.drinkType);
            AssetImageHelper.loadDrinkImage(getApplicationContext(),historyInfoArray.get(position).drinkInfo.drinkType).into(holder.drinkImage);

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
    private ArrayList<EventInfo> eventInfoArray = new ArrayList<>();
    private EventRecyclerViewAdapter eventAdapter = new EventRecyclerViewAdapter();
    private JRecyclerView eventRecycler;
    private class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {
        RelativeLayout.LayoutParams eventImageParams;

        public EventRecyclerViewAdapter(){
            eventImageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayWidth/2);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView)itemView.findViewById(R.id.event_titleImage);
                image.setLayoutParams(eventImageParams);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.eventitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Picasso.with(getApplicationContext()).load(eventInfoArray.get(position).titleImageAddress).placeholder(R.drawable.loading_store).into(holder.image);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(),Event.class);
                intent.putExtra("info",eventInfoArray.get(position));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return eventInfoArray.size();
        }
    }


    //************************MyPage Tab************************
    private ProfileCircleImageView mypageProfileImage;
    private TextView mypageProfileText1, mypageProfileText2;
    private ImageView mypageCurrentMembership,profileNameChange;
    private LinearLayout mypageMembership, mypageInquire, mypageSetting, mypageLogout;
    private DrinkCalendar mypageCalendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(StaticData.currentUser==null){
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        upperBarLeftIcon = (ImageView) findViewById(R.id.home_upperBarLeftIcon);
        upperBarMap = (ImageView) findViewById(R.id.home_upperBarMapIcon);
        upperBarSearch = (ImageView) findViewById(R.id.home_upperBarSearchIcon);
        upperBarFilter = (ImageView) findViewById(R.id.home_upperBarFilterIcon);
        upperBarMainText = (TextView) findViewById(R.id.home_upperBarMainText);
        upperBarDropdown = (ImageView) findViewById(R.id.home_upperBarDropdown);
//        upperBarSubText = (TextView) findViewById(R.id.home_upperBarSubText);
        container = (RelativeLayout) findViewById(R.id.home_contentContainer);
        tapBar = (TapBar) findViewById(R.id.home_tapbar);
        loading = (Loading) findViewById(R.id.home_loading);

        upperBarFilter.setImageDrawable( isAnyFilterChecked() ? DrawableHelper.getDrawable(getResources(),R.drawable.filtericon_active) : DrawableHelper.getDrawable(getResources(),R.drawable.filtericon_inactive));

        upperBarFilter.setVisibility(View.INVISIBLE);

        upperBarFilter.setOnClickListener(view -> {
            if (filterLayoutIsVisible) {
                container.removeView(filterLayout);
                filterLayoutIsVisible = false;
            } else {
                openFilter();
            }
        });

        upperBarMainText.setOnClickListener(view -> {
            upperBarDropdown.callOnClick();
        });

        upperBarDropdown.setOnClickListener(view -> {
            openLocationDropdown();
        });

        upperBarSearch.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Search.class);
            startActivity(intent);
        });

        upperBarMap.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, NearbyPlaces.class);
            startActivity(intent);
        });

        tapBar.setItems(new String[]{getString(R.string.home), getString(R.string.favorite), getString(R.string.history)
                        , getString(R.string.event), getString(R.string.mypage)},
                new String[]{ "home", "favorite", "history", "event", "profile" });

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
                    setUpperBarVisibility(false);
                    upperBarMainText.setText(getString(R.string.mypage));
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

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.home_membershippopup, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        TextView text = (TextView)layout.findViewById(R.id.homeMembershipPopup_text);
        TextView no = (TextView) layout.findViewById(R.id.homeMembershipPopup_no);
        TextView yes = (TextView) layout.findViewById(R.id.homeMembershipPopup_yes);

        if (StaticData.currentUser.expireYYYY==0){
            text.setText(getString(R.string.membershipPopupText));

        }else if(CalendarHelper.getDaysBetweenDates(CalendarHelper.getCurrentDate(),
                new int[]{StaticData.currentUser.expireYYYY,StaticData.currentUser.expireMM,StaticData.currentUser.expireDD})
                <= 7 ){
            text.setText(getString(R.string.membershipPopupTextAlmostEnd));
        }

        no.setOnClickListener(view -> dialog.cancel());
        yes.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Membership.class);
            startActivity(intent);
            dialog.cancel();
        });

        dialog.setContentView(layout);
        dialog.show();
    }

    private void setUpperBarVisibility(boolean setToVisible){
        int visibility = setToVisible ? View.VISIBLE : View.INVISIBLE;

        upperBarLeftIcon.setVisibility(visibility);
        upperBarMap.setVisibility(visibility);
        upperBarSearch.setVisibility(visibility);
        upperBarDropdown.setVisibility(visibility);
//        upperBarSubText.setVisibility(visibility);
        upperBarFilter.setVisibility(View.INVISIBLE);


        int resID = setToVisible ? R.drawable.bottomline : 0;

        upperBarMainText.setBackgroundResource(resID);

    }


    private void openHomeTab() {
        container.removeAllViews();

        setUpperBarVisibility(true);

        upperBarMainText.setText(getString(R.string.aroundMe));

        if(StaticData.currentUser.expireYYYY != 0 && StaticData.currentUser.isHanzanAvailableToday) {
            upperBarLeftIcon.setImageResource(R.drawable.icon);
        }else if(StaticData.currentUser.expireYYYY != 0 && !StaticData.currentUser.isHanzanAvailableToday){
            upperBarLeftIcon.setImageResource(R.drawable.icon_used);
        }else {
            upperBarLeftIcon.setImageResource(R.drawable.icon_deactivated);
        }

        homeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_home, null);
        pubInfoRecyclerView = (JRecyclerView) homeLayout.findViewById(R.id.home_homeRecycler);
        homeLayout.setBackgroundColor(Color.WHITE);

        pubInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pubInfoRecyclerView.setAdapter(homeAdapter);
        pubInfoRecyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
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
                retrievePubList(false);
            }
        });

        pubInfoRecyclerView.startRefresh();

        container.addView(homeLayout);
    }

    private void openMyFavoriteTab() {
        container.removeAllViews();

        setUpperBarVisibility(false);

        upperBarMainText.setText(getString(R.string.favorite));

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

        setUpperBarVisibility(false);

        upperBarMainText.setText(getString(R.string.history));


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

        setUpperBarVisibility(false);

        upperBarMainText.setText(getString(R.string.event));

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_event, null);
        eventRecycler = (JRecyclerView) layout.findViewById(R.id.home_eventRecycler);

        eventRecycler.setLayoutManager(new LinearLayoutManager(this));
        eventRecycler.setAdapter(eventAdapter);
        eventRecycler.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                retrieveEventList(true);
            }

            @Override
            public void onLoadMore() {

            }
        });

        eventRecycler.startRefresh();

        container.addView(layout);
    }

    private void openMyPageTab() {
        container.removeAllViews();

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.home_mypage, null);
        mypageProfileImage = (ProfileCircleImageView) layout.findViewById(R.id.mypage_profileImage);
        mypageProfileText1 = (TextView) layout.findViewById(R.id.mypage_profileText1);
        mypageProfileText2 = (TextView) layout.findViewById(R.id.mypage_profileText2);
        mypageCurrentMembership = (ImageView) layout.findViewById(R.id.mypage_currentMembership);
        mypageCalendar = (DrinkCalendar) layout.findViewById(R.id.mypage_calendar);
        mypageMembership = (LinearLayout) layout.findViewById(R.id.mypage_joinMembership);
        mypageInquire = (LinearLayout) layout.findViewById(R.id.mypage_inquire);
        mypageSetting = (LinearLayout) layout.findViewById(R.id.mypage_setting);
        mypageLogout = (LinearLayout) layout.findViewById(R.id.mypage_logout);
        profileNameChange = (ImageView) layout.findViewById(R.id.mypage_profileNameChange);


        setCalendarChecked(mypageCalendar.getViewingYear(),mypageCalendar.getViewingMonthInNormal());

        mypageCalendar.setListener(this::setCalendarChecked);


        mypageProfileImage.setImage(this,StaticData.currentUser.profileImageAddress);

        mypageProfileText1.setText(StaticData.currentUser.name);


        String expireDate;
        if(StaticData.currentUser.expireYYYY==0){
            expireDate = getString(R.string.notMemberYet);
        }else{
            expireDate = "~ "+Integer.toString(StaticData.currentUser.expireYYYY)+"."+Integer.toString(StaticData.currentUser.expireMM)
                    +"."+Integer.toString(StaticData.currentUser.expireDD);
        }
        mypageProfileText2.setText(getString(R.string.myMembership) + expireDate);

        mypageProfileText1.setOnClickListener(view -> profileNameChange.callOnClick());

        mypageProfileImage.setOnClickListener(view -> {
            openProfilePhotoPopup();
        });

        profileNameChange.setOnClickListener(view -> {
            openProfileNameChangePopup();
        });

        mypageCurrentMembership.setOnClickListener(view->{
            mypageMembership.callOnClick();
        });

        mypageMembership.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Membership.class);
            startActivity(intent);
        });

        mypageInquire.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String[] email=new String[]{StaticData.adminEmail};
            String subject=getString(R.string.emailTitle)+" - "+StaticData.currentUser.name;
            String body=getString(R.string.emailBody);

            intent.putExtra(Intent.EXTRA_EMAIL,email);
            intent.putExtra(Intent.EXTRA_SUBJECT,subject);
            intent.putExtra(Intent.EXTRA_TEXT,body);
            intent.setType("message/rfc822");
//            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,getString(R.string.chooseEmailApp)));

        });

        mypageLogout.setOnClickListener(view -> logoutToLoginPage());

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
            map.put("user_lat", Double.toString(StaticData.myLatestLocation.getLatitude()));
            map.put("user_lat", Double.toString(StaticData.myLatestLocation.getLongitude()));
            map.put("filter_1",filter_checkbox1Checked ? "TRUE" : "FALSE");
            map.put("filter_2",filter_checkbox2Checked ? "TRUE" : "FALSE");
            map.put("filter_3",filter_checkbox3Checked ? "TRUE" : "FALSE");
            map.put("filter_4",filter_checkbox4Checked ? "TRUE" : "FALSE");
            map.put("filter_5",filter_checkbox5Checked ? "TRUE" : "FALSE");
            map.put("filter_6",filter_checkbox6Checked ? "TRUE" : "FALSE");
            map.put("filter_7",filter_checkbox7Checked ? "TRUE" : "FALSE");
            map = ServerConnectionHelper.connect("retrieve nearby places", "nearbyplace", map);

            int i = 0;
            while (true) {
                String num = Integer.toString(i++);
                if(map.get("id_place_" + num)==null){
                    break;
                }
                long id = Long.parseLong(map.get("id_place_" + num));
                String name = map.get("name_place_" + num);
                String address = map.get("address_place_" + num);
                String imageAddress = map.get("imgadd_place_" + num);
                String district = map.get("district_"+num);
                boolean favorite = false;
                if (map.get("like_" + num).equals("TRUE")) {
                    favorite = true;
                }
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));

                pubInfoArray.add(new PubInfo(id, name, address, district, imageAddress, favorite, lat, lng));
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

    private boolean isAnyFilterChecked(){
        return filter_checkbox1Checked || filter_checkbox2Checked || filter_checkbox3Checked
                || filter_checkbox4Checked|| filter_checkbox5Checked|| filter_checkbox6Checked|| filter_checkbox7Checked;
    }

    private boolean isAnyFilterCheckedTemporarily(){
        try {
            boolean checked = checkbox1.isChecked() || checkbox2.isChecked() || checkbox3.isChecked() || checkbox4.isChecked()
                    || checkbox5.isChecked() || checkbox6.isChecked() || checkbox7.isChecked();
            return checked;
        }catch (Exception e){
            return false;
        }
    }

    private void openFilter() {
        if (filterLayoutIsVisible) {
            return ;
        }

        filterLayoutIsVisible = true;

        filterLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.home_filter, null);
        checkbox1 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox1);
        checkbox2 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox2);
        checkbox3 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox3);
        checkbox4 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox4);
        checkbox5 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox5);
        checkbox6 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox6);
        checkbox7 = (JCheckBox) filterLayout.findViewById(R.id.filter_checkbox7);
        TextView textbox1 = (TextView) filterLayout.findViewById(R.id.filter_textbox1);
        TextView textbox2 = (TextView) filterLayout.findViewById(R.id.filter_textbox2);
        TextView textbox3 = (TextView) filterLayout.findViewById(R.id.filter_textbox3);
        TextView textbox4 = (TextView) filterLayout.findViewById(R.id.filter_textbox4);
        TextView textbox5 = (TextView) filterLayout.findViewById(R.id.filter_textbox5);
        TextView textbox6 = (TextView) filterLayout.findViewById(R.id.filter_textbox6);
        TextView textbox7 = (TextView) filterLayout.findViewById(R.id.filter_textbox7);
        TextView filterApply = (TextView) filterLayout.findViewById(R.id.filter_apply);
        TextView filterRemove = (TextView) filterLayout.findViewById(R.id.filter_remove);
        View filterCancel = filterLayout.findViewById(R.id.filter_cancel);

        checkbox1.setChecked(filter_checkbox1Checked);
        checkbox2.setChecked(filter_checkbox2Checked);
        checkbox3.setChecked(filter_checkbox3Checked);
        checkbox4.setChecked(filter_checkbox4Checked);
        checkbox5.setChecked(filter_checkbox5Checked);
        checkbox6.setChecked(filter_checkbox6Checked);
        checkbox7.setChecked(filter_checkbox7Checked);

        filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        filterRemove.setBackground(isAnyFilterChecked() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));

        textbox1.setOnClickListener(view -> checkbox1.callOnClick());
        textbox2.setOnClickListener(view -> checkbox2.callOnClick());
        textbox3.setOnClickListener(view -> checkbox3.callOnClick());
        textbox4.setOnClickListener(view -> checkbox4.callOnClick());
        textbox5.setOnClickListener(view -> checkbox5.callOnClick());
        textbox6.setOnClickListener(view -> checkbox6.callOnClick());
        textbox7.setOnClickListener(view -> checkbox7.callOnClick());


        checkbox1.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox2.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox3.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox4.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox5.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily() ? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox6.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily()? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        checkbox7.setOnCheckListener(() -> {
            filterApply.setBackground(isAnyFilterCheckedTemporarily()? DrawableHelper.getDrawable(getResources(),R.drawable.button_active) : DrawableHelper.getDrawable(getResources(),R.drawable.button_inactive));
        });

        filterApply.setOnClickListener(view -> {
            if (isAnyFilterCheckedTemporarily()) {
                filter_checkbox1Checked = checkbox1.isChecked();
                filter_checkbox2Checked = checkbox2.isChecked();
                filter_checkbox3Checked = checkbox3.isChecked();
                filter_checkbox4Checked = checkbox4.isChecked();
                filter_checkbox5Checked = checkbox5.isChecked();
                filter_checkbox6Checked = checkbox6.isChecked();
                filter_checkbox7Checked = checkbox7.isChecked();

                upperBarFilter.setImageDrawable(isAnyFilterChecked() ? DrawableHelper.getDrawable(getResources(),R.drawable.filtericon_active) : DrawableHelper.getDrawable(getResources(),R.drawable.filtericon_inactive));

                filterLayoutIsVisible = false;
                container.removeView(filterLayout);


                pubInfoRecyclerView.startRefresh();
            }
        });

        filterRemove.setOnClickListener(view -> {
            if (isAnyFilterChecked()) {
                filter_checkbox1Checked = false;
                filter_checkbox2Checked = false;
                filter_checkbox3Checked = false;
                filter_checkbox4Checked = false;
                filter_checkbox5Checked = false;
                filter_checkbox6Checked = false;
                filter_checkbox7Checked = false;

                upperBarFilter.setImageDrawable(DrawableHelper.getDrawable(getResources(),R.drawable.filtericon_inactive));

                filterLayoutIsVisible = false;
                container.removeView(filterLayout);

                pubInfoRecyclerView.startRefresh();
            }
        });

        filterCancel.setOnClickListener(view -> {
            filterLayoutIsVisible = false;
            container.removeView(filterLayout);
        });


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        container.addView(filterLayout, params);
    }

    private void openLocationDropdown(){
        if(upperBarDropdown.getVisibility() == View.VISIBLE){
            JLog.v("Dropdown");
        }
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
                myLocationIsAvailable = true;
                pubInfoArray.clear();
                JLog.v("Normal GPS calling");
                StaticData.myLatestLocation = nGeoPoint;
                retrievePubList(true);
            }

            @Override
            public void onLocationTimeout() {
                myLocationIsAvailable = false;
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                StaticData.myLatestLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                myLocationIsAvailable = false;
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                StaticData.myLatestLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onHasNoLocationPermission() {
                myLocationIsAvailable = false;
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), getString(R.string.allowGPS), Toast.LENGTH_SHORT).show();
                StaticData.myLatestLocation = StaticData.defaultLocation;
                retrievePubList(true);
            }

            @Override
            public void onGpsIsOff() {
                myLocationIsAvailable = false;
                pubInfoArray.clear();
                Toast.makeText(getApplicationContext(), getString(R.string.turnOnGPS), Toast.LENGTH_SHORT).show();
                StaticData.myLatestLocation = StaticData.defaultLocation;
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
                String num = Integer.toString(i++);
                if(map.get("id_place_" + num)==null){
                    break;
                }
                long id = Long.parseLong(map.get("id_place_" + num));
                String name = map.get("name_place_" + num);
                String address = map.get("address_place_" + num);
                String imageAddress = map.get("imgadd_place_" + num);
                String district = map.get("district_"+num);
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));

                pubInfoFavoriteArray.add(new PubInfo(id, name, address, district, imageAddress, true, lat, lng));
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
                String num = Integer.toString(i++);
                if(map.get("id_place_" + num)==null){
                    break;
                }
                long id = Long.parseLong(map.get("id_place_" + num));
                String name = map.get("name_place_" + num);
                String address = map.get("address_place_" + num);
                String imageAddress = map.get("imgadd_place_" + num);
                String district = map.get("district_"+num);
                boolean favorite = false;
                if (map.get("like_" + num).equals("TRUE")) {
                    favorite = true;
                }
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));

                String dateString = map.get("date_visit_" + num);
                int year = Integer.parseInt(dateString.substring(0,4));
                int month = Integer.parseInt(dateString.substring(4,6));
                int day = Integer.parseInt(dateString.substring(6,8));

                String drinkCategory = map.get("category_drink_" + num);
                String drinkName = map.get("name_drink_" + num);

//                        JLog.v("retrieving number "+num);

                PubInfo pubInfo = new PubInfo(id, name, address, district, imageAddress, favorite, lat, lng);
                DrinkInfo drinkInfo = new DrinkInfo(drinkCategory,drinkName);
                historyInfoArray.add(new PubHistoryInfo(pubInfo,drinkInfo,year,month,day));
            }

            String totalSavingCost = NumericHelper.toMoneyFormat(map.get("totalsavingcost"));
            String dataLefts = map.get("datalefts");

            new Handler(getMainLooper()).post(() -> {
                try {
                    historyPrice.setText(totalSavingCost+getString(R.string.won));
                }catch (Exception e){}
                historyAdapter.notifyDataSetChanged();
                historyRecyclerView.finishRefreshing();
                if (dataLefts.equals("TRUE")) {
                    historyRecyclerView.finishLoadmore();
                }
            });
        }).start();
    }


    //****Event Tab****

    private synchronized void retrieveEventList(boolean clearArray){
        if(clearArray){
            eventInfoArray.clear();
        }
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();

            map.put("id_member", Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving event lists", "eventlist", map);

            int i = 0;
            while (true) {
                String num = Integer.toString(i++);
                if(map.get("id_event_" + num)==null){
                    break;
                }
                long id = Long.parseLong(map.get("id_event_" + num));
                String titleImage = map.get("titleimgadd_event_"+num);
                String mainImage = map.get("mainimgadd_event_"+num);
                String title = map.get("title_event_"+num);

                eventInfoArray.add(new EventInfo(id,titleImage,mainImage,title));
            }

            new Handler(getMainLooper()).post(() -> {
                eventAdapter.notifyDataSetChanged();
                eventRecycler.finishRefreshing();
            });
        }).start();
    }


    //****My Page Tab****

    private void openProfilePhotoPopup() {
        Dialog dialog = new Dialog(this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.mypage_profilephoto_popup, null);
        TextView see = (TextView) layout.findViewById(R.id.profilePhotoPopup_see);
        TextView change = (TextView) layout.findViewById(R.id.profilePhotoPopup_change);
        TextView delete = (TextView) layout.findViewById(R.id.profilePhotoPopup_delete);

        see.setOnClickListener(view -> {
            mypageProfileImage.openProfilePage(this);
            dialog.cancel();
        });

        change.setOnClickListener(view -> {
            loading.setLoadingStarted();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, ImageCropper.IMAGE_CROP_IMAGESELECT);
            dialog.cancel();
        });

        delete.setOnClickListener(view -> {
            loading.setLoadingStarted();
            updateNewProfileImage(new byte[0],true);
            dialog.cancel();
        });

        dialog.setContentView(layout);

        dialog.show();
    }

    private void openProfileNameChangePopup(){
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.mypage_profilenamechange_popup,null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ProfileCircleImageView profileImage = (ProfileCircleImageView)layout.findViewById(R.id.profileNameChange_image);
        EditText profileEditText = (EditText)layout.findViewById(R.id.profileNameChange_editText);
        ImageView delete = (ImageView)layout.findViewById(R.id.profileNameChange_xmark);
        TextView cancel = (TextView)layout.findViewById(R.id.profileNameChange_cancel);
        TextView confirm = (TextView)layout.findViewById(R.id.profileNameChange_confirm);

        profileImage.setImage(this,StaticData.currentUser.profileImageAddress);
        profileEditText.setText(StaticData.currentUser.name);



        profileEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                confirm.callOnClick();
            }
            return false;
        });

        cancel.setOnClickListener(view -> dialog.cancel());

        delete.setOnClickListener(view -> profileEditText.setText(""));

        confirm.setOnClickListener(view -> {
            try {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }catch (Exception e){}
            loading.setLoadingStarted();
            String word = profileEditText.getText().toString();

            while(word.startsWith(" ")){
                word = word.substring(1,word.length());
            }
            while(word.endsWith(" ")){
                word = word.substring(0,word.length()-1);
            }
            if (word.length() == 0) {
                new Handler(getMainLooper()).post(()->loading.setLoadingCompleted());
                return;
            }

            String finalWord = word;
            new Thread(()->{
                HashMap<String, String> map = new HashMap<>();
                map.put("id_member",Long.toString(StaticData.currentUser.id));
                map.put("name_member", finalWord);
                map = ServerConnectionHelper.connect("Updating new profile name","nameupdate",map);

                String result = map.get("nameupdate_result");
                String newName = map.get("name_member");

                if(result!=null && result.equals("TRUE")){
                    StaticData.currentUser.name = newName;
                    new Handler(getMainLooper()).post(()-> {
                        mypageProfileText1.setText(newName);
                        dialog.cancel();
                    });
                }
                new Handler(getMainLooper()).post(()->loading.setLoadingCompleted());

            }).start();
        });

        dialog.setContentView(layout,params);
        dialog.show();
    }

    private void logoutToLoginPage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.logoutMessage));
        dialogBuilder.setNegativeButton(getString(R.string.no), null);
        dialogBuilder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
            StaticData.currentUser = null;
            AccessToken.setCurrentAccessToken(null);
            UserManagement.requestLogout(null);
            Session.getCurrentSession().close();
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            finishAffinity();
        });
        dialogBuilder.create().show();

    }

    private void setCalendarChecked(int year, int monthInNormal){
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();

            String date = Integer.toString(year);
            if(monthInNormal<10) date+="0";
            date += Integer.toString(monthInNormal);

            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("date",date);

            map = ServerConnectionHelper.connect("retrieving "+date+" drink history","drinkcalendar",map);

            int i=0;
            ArrayList<Integer> dateArray = new ArrayList<>();
            while(true){
                String num = Integer.toString(i++);
                String retrievedDate = map.get("date_visit_" + num);
                if(retrievedDate==null){
                    break;
                }else {
                    dateArray.add(CalendarHelper.parseDate(retrievedDate)[2]);
                }
            }
            new Handler(getMainLooper()).post(()->{
                mypageCalendar.setDateChecked(dateArray);
            });
        }).start();
    }

    private void updateNewProfileImage(byte[] bitmapArray, boolean deleteProfile){
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            String delete = "FALSE";
            if(deleteProfile)delete = "TRUE";
            map.put("deleteornot",delete);

            map = ServerConnectionHelper.connect("updating profile image","imageupdate",map,"profileimage",bitmapArray);

            String result = map.get("imageupdate_result");
            String newImageAddress = map.get("imgadd_member");

            new Handler(getMainLooper()).post(()->{
                loading.setLoadingCompleted();
                if(result != null && result.equals("TRUE")){
                    StaticData.currentUser.profileImageAddress = newImageAddress;
                    mypageProfileImage.setImage(this,newImageAddress);
                }
            });
        }).start();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageCropper.IMAGE_CROP_IMAGESELECT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Intent intent = new Intent(Home.this, ImageCropper.class);
                intent.setData(uri);
                startActivityForResult(intent, ImageCropper.IMAGE_CROP_REQUEST);
            }else{
                loading.setLoadingCompleted();
            }
        } else if (requestCode == ImageCropper.IMAGE_CROP_REQUEST) {
            if (resultCode == RESULT_OK) {
                byte[] bitmapArray = ImageCropper.croppedImageByteArray;
                updateNewProfileImage(bitmapArray,false);
            }else{
                loading.setLoadingCompleted();
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
        if(StaticData.currentUser.expireYYYY != 0 && StaticData.currentUser.isHanzanAvailableToday) {
            upperBarLeftIcon.setImageResource(R.drawable.icon);
        }else if(StaticData.currentUser.expireYYYY != 0 && !StaticData.currentUser.isHanzanAvailableToday) {
            upperBarLeftIcon.setImageResource(R.drawable.icon_used);
        }else {
            upperBarLeftIcon.setImageResource(R.drawable.icon_deactivated);
        }
        try{
            pubInfoRecyclerView.finishRefreshing();
        }catch (Exception e){}

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
                Toast.makeText(this,getString(R.string.oneMoreBackButton),Toast.LENGTH_SHORT).show();
                finishIfBackButtonClickedOnceMore = true;
                new Thread(()->{
                    try{
                        Thread.sleep(2500);
                        finishIfBackButtonClickedOnceMore = false;
                    }catch (Exception e){}
                }).start();
            }

        }
    }


}
