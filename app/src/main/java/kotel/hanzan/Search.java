package kotel.hanzan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.JRecyclerView;
import kotel.hanzan.view.Loading;

public class Search extends JActivity {
    private ImageView back, searchIcon, deleteText, searchText;
    private EditText searchEditText;
    private TextView upperTitle;
    private LinearLayout initialPanel;
    private JRecyclerView recyclerView;
    private Loading loading;

    private LocationHelper locationHelper = new LocationHelper();


    private InputMethodManager inputMethodManager;

    private ArrayList<PubInfo> pubInfoArray = new ArrayList<>();

    private class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {
        private ViewHolder lastClickedViewHolder = null;
        private int lastClickedNumber;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, favorite;
            TextView text1, text2, text3;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.pubitem_image);
                favorite = (ImageView) itemView.findViewById(R.id.pubitem_favorite);
                text1 = (TextView) itemView.findViewById(R.id.pubitem_text1);
                text2 = (TextView) itemView.findViewById(R.id.pubitem_text2);
                text3 = (TextView) itemView.findViewById(R.id.pubitem_text3);
            }
        }

        public void setFavoriteButton(boolean clicked) {
            try {
                pubInfoArray.get(lastClickedNumber).setFavorite(clicked);
                if (clicked) {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_selected);
                } else {
                    lastClickedViewHolder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            } catch (Exception e) {
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PubInfo pubInfo = pubInfoArray.get(position);

            Picasso.with(getApplicationContext()).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(holder.image);
            if (pubInfo.getFavorite()) {
                holder.favorite.setImageResource(R.drawable.favorite_selected);
            } else {
                holder.favorite.setImageResource(R.drawable.favorite_unselected);
            }

            String distanceString = "";

            if(myLocationMarkerIsVisible) {
                Double distance = GeoHelper.getActualKilometer(pubInfo.latitude, pubInfo.longitude, StaticData.myLatestLocation.getLatitude(), StaticData.myLatestLocation.getLongitude());
                distanceString = GeoHelper.getDistanceString(distance);
            }

            holder.text1.setText(pubInfo.name + "  " + distanceString);
            holder.text2.setText(pubInfo.district);
            holder.text3.setText(pubInfo.address);


            holder.itemView.setOnClickListener(view -> {
                lastClickedViewHolder = holder;
                lastClickedNumber = position;
                Intent intent = new Intent(getApplicationContext(), PubPage.class);
                intent.putExtra("info", pubInfo);
                startActivityForResult(intent, PubPage.REQUEST_OPENPUBPAGE);
            });

            holder.favorite.setOnClickListener(view -> {
                pubInfoArray.get(position).setFavorite(!pubInfoArray.get(position).getFavorite());
                if(pubInfoArray.get(position).getFavorite()){
                    holder.favorite.setImageResource(R.drawable.favorite_selected);
                }else{
                    holder.favorite.setImageResource(R.drawable.favorite_unselected);
                }
            });

        }

        @Override
        public int getItemCount() {
            return pubInfoArray.size();
        }
    }
    private SearchRecyclerViewAdapter adapter;

    private String searchedWord = "";

    private boolean myLocationMarkerIsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        adapter = new SearchRecyclerViewAdapter();

        back = (ImageView) findViewById(R.id.search_back);
        upperTitle = (TextView)findViewById(R.id.search_upperTitle);
        searchIcon = (ImageView) findViewById(R.id.search_searchIcon);
        searchText = (ImageView) findViewById(R.id.search_search);
        deleteText = (ImageView) findViewById(R.id.search_searchDeleteText);
        searchEditText = (EditText) findViewById(R.id.search_searchEditText);
        initialPanel = (LinearLayout) findViewById(R.id.search_initialPanel);
        recyclerView = (JRecyclerView) findViewById(R.id.search_recycler);
        loading = (Loading)findViewById(R.id.search_loading);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setEnableRefresh(false);
        recyclerView.setOverScrollRefreshShow(false);


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String word = editable.toString();

                while(word.startsWith(" ")){
                    word = word.substring(1,word.length());
                }
                while(word.endsWith(" ")){
                    word = word.substring(0,word.length()-1);
                }

                if (word.length() != 0) {
                    searchText.setVisibility(View.VISIBLE);
                } else {
                    searchText.setVisibility(View.GONE);
                }
            }
        });

        searchIcon.setOnClickListener(view -> search(searchEditText.getText().toString()));

        back.setOnClickListener(view -> finish());

        searchText.setOnClickListener(view -> search(searchEditText.getText().toString()));

        deleteText.setOnClickListener(view -> {
            pubInfoArray = new ArrayList<>();
            adapter.notifyDataSetChanged();
            searchEditText.setText("");
            upperTitle.setText(getString(R.string.search));
            initialPanel.setVisibility(View.VISIBLE);
        });

        searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchIcon.callOnClick();
                }
            return false;
        });

        recyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {}

            @Override
            public void onLoadMore() {
                retrievePubList(false);
            }
        });
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

        if(StaticData.myLatestLocation==null) {
            locationHelper.getMyLocationOnlyOneTime(this, new LocationHelperListener() {
                @Override
                public void onSearchingStarted() {}

                @Override
                public void onSearchingEnded() {}

                @Override
                public void onLocationFound(NGeoPoint nGeoPoint) {
                    myLocationMarkerIsVisible = true;

                    JLog.v("Normal GPS call");
                    StaticData.myLatestLocation = nGeoPoint;
                    retrievePubList(true);
                }

                @Override
                public void onLocationTimeout() {
                    myLocationMarkerIsVisible = false;
//                    Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                    StaticData.myLatestLocation = StaticData.defaultLocation;
                    retrievePubList(true);
                }

                @Override
                public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                    myLocationMarkerIsVisible = false;
//                    Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                    StaticData.myLatestLocation = StaticData.defaultLocation;
                    retrievePubList(true);
                }

                @Override
                public void onHasNoLocationPermission() {
                    myLocationMarkerIsVisible = false;
//                    Toast.makeText(getApplicationContext(), getString(R.string.allowGPS), Toast.LENGTH_SHORT).show();
                    StaticData.myLatestLocation = StaticData.defaultLocation;
                    retrievePubList(true);
                }

                @Override
                public void onGpsIsOff() {
                    myLocationMarkerIsVisible = false;
//                    Toast.makeText(getApplicationContext(), getString(R.string.turnOnGPS), Toast.LENGTH_SHORT).show();
                    StaticData.myLatestLocation = StaticData.defaultLocation;
                    retrievePubList(true);
                }
            });
        }else{
            JLog.v("Using buffered location");
            retrievePubList(true);
        }
    }


    private void search(String word) {
        while(word.startsWith(" ")){
            word = word.substring(1,word.length());
        }
        while(word.endsWith(" ")){
            word = word.substring(0,word.length()-1);
        }

        if (word.length() == 0) {
            Toast.makeText(this, getString(R.string.enterSearchText), Toast.LENGTH_SHORT).show();
            return;
        }
        searchedWord = word;

        searchEditText.setText("");

        loading.setLoadingStarted();

        upperTitle.setText(getString(R.string.search)+" - '" + word+"'");

        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        initialPanel.setVisibility(View.INVISIBLE);
        pubInfoArray.clear();
        adapter.notifyDataSetChanged();

        requestGPSPermission();
    }

    private synchronized void retrievePubList(boolean clearArray){
        if(clearArray){
            pubInfoArray.clear();
        }

        new Thread(()->{
            String userLat = Double.toString(StaticData.myLatestLocation.getLatitude());
            String userLng = Double.toString(StaticData.myLatestLocation.getLongitude());
            String word = searchedWord;
            if(word.equals("")){
                return;
            }

            HashMap<String,String> map = new HashMap<>();
            map.put("searchtext",word);
            map.put("user_lat",userLat);
            map.put("user_lng",userLng);
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("at",Integer.toString(pubInfoArray.size()));

            map = ServerConnectionHelper.connect("searching word - " + word,"searchplace",map);

            int i=0;
            while(true){
                String num = Integer.toString(i++);
                if(map.get("id_place_"+num)==null){
                    break;
                }
                long id = Long.parseLong(map.get("id_place_"+num));
                String name = map.get("name_place_"+num);
                String address = map.get("address_place_"+num);
                String imageAddress = map.get("imgadd_place_"+num);
                String district = map.get("district_"+num);
                boolean favorite=false;
                if(map.get("like_"+num).equals("TRUE")){
                    favorite=true;
                }
                double lat = Double.parseDouble(map.get("lat_"+num));
                double lng = Double.parseDouble(map.get("lng_"+num));

                pubInfoArray.add(new PubInfo(id,name,address, district,imageAddress,favorite,lat,lng));
            }

            String dataleft = map.get("datalefts");

            new Handler(getMainLooper()).post(() -> {
                recyclerView.finishRefreshing();
                adapter.notifyDataSetChanged();
                loading.setLoadingCompleted();
                if (dataleft!=null && dataleft.equals("TRUE")){
                    recyclerView.finishLoadmore();
                }
            });
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PubPage.REQUEST_OPENPUBPAGE){
            if(resultCode == PubPage.RESULT_FAVORITECHANGED){
                adapter.setFavoriteButton(data.getBooleanExtra("favorite",false));
            }
        }
    }

}
