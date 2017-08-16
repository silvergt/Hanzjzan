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

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

import kotel.hanzan.function.JLog;
import kotel.hanzan.view.Loading;

public class NearbyPlaces extends NMapActivity {
    final private String LOCATION_MYLOCATION="LOCATION_MYLOCATION";

    private NMapView mapView;// 지도 화면 View

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapLocationManager locationManager;
    private NMapPOIdata poiData;
    private NGeoPoint myLocation;

    private NMapOverlayItem currentlyFocusedMarker;

    private Drawable selected,unselected;

    private int drawableSize;

    private Loading loading;
    private ImageView myLocationButton,back,pubImage;
    private LinearLayout pubInfoLayout;
    private TextView pubText1,pubText2,pubText3,pubText4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationviewer);

        init();
    }

    private void init(){
        drawableSize=(int)getResources().getDimension(R.dimen.locationViewer_markerSize);

        locationManager = new NMapLocationManager(this);
        locationManager.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {
            @Override
            public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
                onMyLocationUpdated(nGeoPoint);
                getNearbyPubs(nGeoPoint);
                locationManager.disableMyLocation();
                loading.setLoadingCompleted();
                return true;
            }

            @Override
            public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {
                loading.setLoadingCompleted();
            }

            @Override
            public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
                loading.setLoadingCompleted();
            }
        });

        loading = (Loading)findViewById(R.id.locationViewer_loading);
        myLocationButton = (ImageView)findViewById(R.id.locationViewer_myLocation);
        back = (ImageView)findViewById(R.id.locationViewer_back);
        pubInfoLayout = (LinearLayout)findViewById(R.id.locationViewer_pubInfoLayout);
        pubImage=(ImageView)findViewById(R.id.locationViewer_pubImage);
        pubText1=(TextView)findViewById(R.id.locationViewer_pubText1);
        pubText2=(TextView)findViewById(R.id.locationViewer_pubText2);
        pubText3=(TextView)findViewById(R.id.locationViewer_pubText3);
        pubText4=(TextView)findViewById(R.id.locationViewer_pubText4);

        mapView=(NMapView) findViewById(R.id.locationViewer_mapView);
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


        myLocationButton.setOnClickListener(view ->{
            getMyLocation();
        });

        back.setOnClickListener(view -> {
            finish();
        });

        selected = getResources().getDrawable(R.drawable.loading_front,null);
        selected.setBounds(-drawableSize/2,-drawableSize,drawableSize/2,0);

        unselected = getResources().getDrawable(R.drawable.loading_back,null);
        unselected.setBounds(-drawableSize/2,-drawableSize,drawableSize/2,0);

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
        try {
            if(!locationManager.isMyLocationEnabled()) {
                locationManager.enableMyLocation(false);
            }
        }catch (Exception e){
            JLog.v("Loading map without my location");
            NGeoPoint geo=new NGeoPoint();
            geo.latitude=37.518775;
            geo.longitude=127.050081;

            mapView.getMapController().setMapCenter(geo,13);
            loading.setLoadingCompleted();
        }

    }


    private void addMarkerTo(double lat, double lng){
        NGeoPoint geo=new NGeoPoint();
        geo.latitude=lat;
        geo.longitude=lng;

        poiData.beginPOIdata(1);
        poiData.addPOIitem(geo, "Pizza 777-111", unselected,1);
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);

    }

    private void addMarkerTo(NGeoPoint geoPoint,boolean isMyLocation){

        poiData.beginPOIdata(1);
        if(isMyLocation) {
            poiData.addPOIitem(geoPoint, LOCATION_MYLOCATION, unselected, 2);
        }else{
            poiData.addPOIitem(geoPoint, "tttt", unselected, 1);
        }
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    private void onMarkerClicked(NMapOverlayItem nMapOverlayItem){
        JLog.v(nMapOverlayItem.getTitle());
        JLog.v(Double.toString(nMapOverlayItem.getPoint().getLatitude()));
        JLog.v(Double.toString(nMapOverlayItem.getPoint().getLongitude()));

//        mapView.getMapController().animateTo(nMapOverlayItem.getPoint());

        if(nMapOverlayItem.getTitle().equals(LOCATION_MYLOCATION)){
            return;
        }

        if(currentlyFocusedMarker!=null){
            currentlyFocusedMarker.setMarker(unselected);
        }


        currentlyFocusedMarker = nMapOverlayItem;

        currentlyFocusedMarker.setMarker(selected);

        pubInfoLayout.setVisibility(View.VISIBLE);

//        Intent intent=new Intent(LocationViewer.this,Home.class);
//        startActivity(intent);
    }

    private void onMyLocationUpdated(NGeoPoint geoPoint){
        JLog.v("myLoc",Double.toString(geoPoint.getLatitude()));
        JLog.v("myLoc",Double.toString(geoPoint.getLongitude()));
        myLocation = locationManager.getMyLocation();


        poiData.removeAllPOIdata();

        mapView.getMapController().animateTo(myLocation);
        addMarkerTo(myLocation,true);
    }

    private void getNearbyPubs(NGeoPoint geopoint){

        addMarkerTo(37.518775,127.050081);
        addMarkerTo(37.518875,127.050081);
        addMarkerTo(37.518975,127.050081);
        addMarkerTo(37.519075,127.050081);
        addMarkerTo(37.519175,127.050081);
        addMarkerTo(37.538775,127.050081);
    }

}
