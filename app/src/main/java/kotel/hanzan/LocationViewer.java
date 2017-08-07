package kotel.hanzan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
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
import kotel.hanzan.function.StaticData;

public class LocationViewer extends NMapActivity {


    private NMapView mapView;// 지도 화면 View

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapLocationManager locationManager;
    private NMapPOIdata poiData;

    TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationviewer);

        init();
    }

    private void init(){
        test=(TextView) findViewById(R.id.textTest);

        mapView=(NMapView) findViewById(R.id.locationViewer_mapView);
        mapView.setClientId(StaticData.NAVER_CLIENT_ID);
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

        requestGPSPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initMapViewWithMyLocation();
    }

    private void requestGPSPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }else{
                initMapViewWithMyLocation();
            }
        }else{
            initMapViewWithMyLocation();
        }
    }

    private void initMapViewWithMyLocation(){
        try {
            locationManager = new NMapLocationManager(this);
            locationManager.enableMyLocation(false);
        }catch (Exception e){
            JLog.v("Loading map without my location");
            initMapViewWithoutMyLocation();
        }

        try{
            JLog.v(Double.toString(locationManager.getMyLocation().getLatitude()));
            JLog.v(Double.toString(locationManager.getMyLocation().getLongitude()));
            mapView.getMapController().setMapCenter(locationManager.getMyLocation());

            addMarkerTo(37.518775,127.050081);
            addMarkerTo(37.538775,127.050081);
            addMarkerTo(locationManager.getMyLocation());

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initMapViewWithoutMyLocation(){
        NGeoPoint geo=new NGeoPoint();
        geo.latitude=37.518775;
        geo.longitude=127.050081;

        mapView.getMapController().setMapCenter(geo,12);
    }




    private void addMarkerTo(double lat, double lng){
        NGeoPoint geo=new NGeoPoint();
        geo.latitude=lat;
        geo.longitude=lng;

        poiData.beginPOIdata(1);
        poiData.addPOIitem(geo, "Pizza 777-111", getDrawable(R.mipmap.ic_launcher),1);
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);


    }

    private void addMarkerTo(NGeoPoint geoPoint){
        poiData.beginPOIdata(1);
        poiData.addPOIitem(geoPoint, "tttt", getDrawable(R.mipmap.ic_launcher),1);
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    private void onMarkerClicked(NMapOverlayItem nMapOverlayItem){
        JLog.v(nMapOverlayItem.getTitle());
        JLog.v(Double.toString(nMapOverlayItem.getPoint().getLatitude()));
        JLog.v(Double.toString(nMapOverlayItem.getPoint().getLongitude()));

        test.setText(nMapOverlayItem.getTitle());

        Intent intent=new Intent(LocationViewer.this,Home.class);
        startActivity(intent);
    }

}
