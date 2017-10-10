package kotel.hanzan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;
import com.squareup.picasso.Picasso;

import kotel.hanzan.data.PubInfo;
import kotel.hanzan.data.StaticData;
import kotel.hanzan.function.DrawableHelper;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.view.JNMapActivity;
import kotel.hanzan.view.Loading;

public class LocationViewer extends JNMapActivity {
    final private String LOCATION_MYLOCATION="LOCATION_MYLOCATION";

    private NMapView mapView;// 지도 화면 View

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapPOIdata poiData;
    private NGeoPoint pubLocation;
    private PubInfo pubInfo;

    private LocationHelper locationHelper = new LocationHelper();

    private Drawable pubMarker,myLocationMarker;

    private int drawableWidth,drawableHeight;

    private Loading loading;
    private ImageView myLocationButton,back,pubImage;
    private LinearLayout pubInfoLayout;
    private TextView upperText,pubText1,pubText2,pubText3,pubText4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationviewer);

        init();
    }

    private void init(){
        pubInfo = (PubInfo)getIntent().getSerializableExtra("info");

        drawableWidth=(int)getResources().getDimension(R.dimen.markerWidth);
        drawableHeight=(int)getResources().getDimension(R.dimen.markerHeight);


        loading = findViewById(R.id.locationViewer_loading);
        myLocationButton = findViewById(R.id.locationViewer_myLocation);
        back = findViewById(R.id.locationViewer_back);
        pubInfoLayout = findViewById(R.id.locationViewer_pubInfoLayout);
        pubImage= findViewById(R.id.locationViewer_pubImage);
        upperText= findViewById(R.id.locationViewer_upperBarMainText);
        pubText1= findViewById(R.id.locationViewer_pubText1);
        pubText2= findViewById(R.id.locationViewer_pubText2);
        pubText3= findViewById(R.id.locationViewer_pubText3);
        pubText4= findViewById(R.id.locationViewer_pubText4);

        mapView= findViewById(R.id.locationViewer_mapView);
        mapView.setClientId(getString(R.string.naver_client_id));
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();
        mapView.setScalingFactor(2.0f);

        provider=new NMapResourceProvider(this) {
            @Override
            protected int findResourceIdForMarker(int i, boolean b) {
                return 0;
            }

            @Override
            protected Drawable getDrawableForMarker(int i, boolean b, NMapOverlayItem nMapOverlayItem) {
                return null;
            }

            @Override
            public Drawable getCalloutBackground(NMapOverlayItem nMapOverlayItem) {
                return null;
            }

            @Override
            public String getCalloutRightButtonText(NMapOverlayItem nMapOverlayItem) {
                return null;
            }

            @Override
            public Drawable[] getCalloutRightButton(NMapOverlayItem nMapOverlayItem) {
                return new Drawable[0];
            }

            @Override
            public Drawable[] getCalloutRightAccessory(NMapOverlayItem nMapOverlayItem) {
                return new Drawable[0];
            }

            @Override
            public int[] getCalloutTextColors(NMapOverlayItem nMapOverlayItem) {
                return new int[0];
            }

            @Override
            public Drawable[] getLocationDot() {
                return new Drawable[0];
            }

            @Override
            public Drawable getDirectionArrow() {
                return null;
            }

            @Override
            public int getParentLayoutIdForOverlappedListView() {
                return 0;
            }

            @Override
            public int getOverlappedListViewId() {
                return 0;
            }

            @Override
            public int getLayoutIdForOverlappedListView() {
                return 0;
            }

            @Override
            public void setOverlappedListViewLayout(ListView listView, int i, int i1, int i2) {

            }

            @Override
            public int getListItemLayoutIdForOverlappedListView() {
                return 0;
            }

            @Override
            public int getListItemTextViewId() {
                return 0;
            }

            @Override
            public int getListItemTailTextViewId() {
                return 0;
            }

            @Override
            public int getListItemImageViewId() {
                return 0;
            }

            @Override
            public int getListItemDividerId() {
                return 0;
            }

            @Override
            public void setOverlappedItemResource(NMapPOIitem nMapPOIitem, ImageView imageView) {

            }
        };
        overlayManager = new NMapOverlayManager(this,mapView,provider);
        poiData = new NMapPOIdata(2, provider);

        overlayManager.setOnCalloutOverlayListener((nMapOverlay, nMapOverlayItem, rect) -> {
            onMarkerClicked(nMapOverlayItem);
            return null;
        });

        pubInfoLayout.setVisibility(View.INVISIBLE);

        upperText.setText(pubInfo.name);
        Picasso.with(this).load(pubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(pubImage);

        pubText1.setText(pubInfo.name);
        pubText2.setText(pubInfo.drinkTypes);
        pubText3.setText(pubInfo.district);
        pubText4.setText(pubInfo.address);

        myLocationButton.setOnClickListener(view ->{
            getMyLocation();
        });

        back.setOnClickListener(view -> {
            finish();
        });

        pubMarker = DrawableHelper.getDrawable(getResources(),R.drawable.gps_selected);
        pubMarker.setBounds(-drawableWidth/2,-drawableHeight,drawableWidth/2,0);

        myLocationMarker = DrawableHelper.getDrawable(getResources(),R.drawable.gps_mylocation);
        myLocationMarker.setBounds(-drawableWidth/2,-drawableWidth/2,drawableWidth/2,drawableWidth/2);

        pubInfoLayout.setVisibility(View.VISIBLE);

        pubLocation = new NGeoPoint(pubInfo.longitude,pubInfo.latitude);

        addMarkerTo(pubLocation,false);
        mapView.getMapController().setMapCenter(pubLocation,13);

        requestGPSPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getMyLocation();
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

    private void getMyLocation(){
        loading.setLoadingStarted();

        locationHelper.getMyLocationOnlyOneTime(this, new LocationHelperListener() {
            @Override
            public void onSearchingStarted() {

            }

            @Override
            public void onSearchingEnded() {

            }

            @Override
            public void onLocationFound(NGeoPoint nGeoPoint) {
                StaticData.myLatestLocation = nGeoPoint;

                poiData.removeAllPOIdata();

                addMarkerTo(StaticData.myLatestLocation,true);
                addMarkerTo(pubLocation,false);
                mapView.getMapController().setMapCenter(pubLocation,13);

                double distance = GeoHelper.getActualKilometer(StaticData.myLatestLocation.getLatitude(),StaticData.myLatestLocation.getLongitude(),pubInfo.latitude,pubInfo.longitude);
                String distanceString = GeoHelper.getDistanceString(distance);

                pubText1.setText(pubInfo.name+"  "+distanceString);
                loading.setLoadingCompleted();
            }

            @Override
            public void onLocationTimeout() {
                Toast.makeText(getApplicationContext(),getString(R.string.failedGPS),Toast.LENGTH_SHORT).show();
                mapView.getMapController().setMapCenter(pubLocation,13);
                loading.setLoadingCompleted();
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                Toast.makeText(getApplicationContext(),getString(R.string.failedGPS),Toast.LENGTH_SHORT).show();
                mapView.getMapController().setMapCenter(pubLocation,13);
                loading.setLoadingCompleted();
            }

            @Override
            public void onHasNoLocationPermission() {
                Toast.makeText(getApplicationContext(), getString(R.string.allowGPS), Toast.LENGTH_SHORT).show();
                mapView.getMapController().setMapCenter(pubLocation,13);
                loading.setLoadingCompleted();
            }

            @Override
            public void onGpsIsOff() {
                Toast.makeText(getApplicationContext(), getString(R.string.turnOnGPS), Toast.LENGTH_SHORT).show();
                mapView.getMapController().setMapCenter(pubLocation,13);
                loading.setLoadingCompleted();
            }
        });
    }

    private void onMarkerClicked(NMapOverlayItem nMapOverlayItem) {}


    private void addMarkerTo(NGeoPoint geoPoint,boolean isMyLocation){

        poiData.beginPOIdata(1);
        if(isMyLocation) {
            poiData.addPOIitem(geoPoint, LOCATION_MYLOCATION, myLocationMarker, -1);
        }else{
            poiData.addPOIitem(geoPoint, pubInfo.name, pubMarker, 1);
        }
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }
}
