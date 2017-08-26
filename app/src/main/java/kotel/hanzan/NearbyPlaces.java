package kotel.hanzan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.view.Loading;

public class NearbyPlaces extends NMapActivity {
    final private String LOCATION_MYLOCATION = "LOCATION_MYLOCATION";

    private NMapView mapView;// 지도 화면 View

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapPOIdata poiData;
//    private NGeoPoint myLocation;

    private NMapOverlayItem currentlyFocusedMarker;

    private LocationHelper locationHelper = new LocationHelper();

    private Drawable selected, unselected, myLocationMarker;

    private int drawableWidth, drawableHeight;

    private Loading loading;
    private ImageView myLocationButton, back, pubImage;
    private LinearLayout pubInfoLayout;
    private TextView pubText1, pubText2, pubText3, pubText4;

    private ArrayList<PubInfo> pubInfoArray = new ArrayList<>();

    private boolean myLocationMarkerIsVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationviewer);

        init();
    }

    private void init() {

        drawableWidth = (int) getResources().getDimension(R.dimen.markerWidth);
        drawableHeight = (int) getResources().getDimension(R.dimen.markerHeight);


        loading = (Loading) findViewById(R.id.locationViewer_loading);
        myLocationButton = (ImageView) findViewById(R.id.locationViewer_myLocation);
        back = (ImageView) findViewById(R.id.locationViewer_back);
        pubInfoLayout = (LinearLayout) findViewById(R.id.locationViewer_pubInfoLayout);
        pubImage = (ImageView) findViewById(R.id.locationViewer_pubImage);
        pubText1 = (TextView) findViewById(R.id.locationViewer_pubText1);
        pubText2 = (TextView) findViewById(R.id.locationViewer_pubText2);
        pubText3 = (TextView) findViewById(R.id.locationViewer_pubText3);
        pubText4 = (TextView) findViewById(R.id.locationViewer_pubText4);

        mapView = (NMapView) findViewById(R.id.locationViewer_mapView);
        mapView.setClientId(getString(R.string.naver_client_id));
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();
        mapView.setScalingFactor(2.0f);

        provider = new NMapResourceProvider(this) {
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
        overlayManager = new NMapOverlayManager(this, mapView, provider);
        poiData = new NMapPOIdata(2, provider);

        overlayManager.setOnCalloutOverlayListener((nMapOverlay, nMapOverlayItem, rect) -> {
            onMarkerClicked(nMapOverlayItem);
            return null;
        });

        pubInfoLayout.setVisibility(View.INVISIBLE);

        mapView.setOnMapViewTouchEventListener(new NMapView.OnMapViewTouchEventListener() {
            @Override
            public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {

            }

            @Override
            public void onLongPressCanceled(NMapView nMapView) {

            }

            @Override
            public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {

            }

            @Override
            public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {
                JLog.v("LATLNG");
                NGeoPoint[] geos = GeoHelper.getMapStartEndPoint(nMapView.getMapController().getMapCenter().getLatitudeE6(),nMapView.getMapController().getMapCenter().getLongitudeE6(),
                        nMapView.getMapProjection().getLatitudeSpan(),nMapView.getMapProjection().getLongitudeSpan());

                JLog.v(geos[0].getLatitude());
                JLog.v(geos[0].getLongitude());
                JLog.v(geos[1].getLatitude());
                JLog.v(geos[1].getLongitude());
//                JLog.v(Double.toString(nMapView.getMapController().getMapCenter().getLatitude()));
//                JLog.v(Double.toString(nMapView.getMapController().getMapCenter().getLongitude()));
//                JLog.v(Double.toString(nMapView.getMapController().getMapCenter().getLatitudeE6()));
//                JLog.v(Double.toString(nMapView.getMapController().getMapCenter().getLongitudeE6()));
//                JLog.v(nMapView.getMapProjection().getLatitudeSpan());
//                JLog.v(nMapView.getMapProjection().getLongitudeSpan());

            }

            @Override
            public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {

            }

            @Override
            public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {
                if(currentlyFocusedMarker!=null) {
                    removeFocusFromMarker();
                }
            }
        });

        myLocationButton.setOnClickListener(view -> {
            pubInfoLayout.setVisibility(View.INVISIBLE);
            loading.setLoadingStarted();
            pubInfoArray.clear();
            getMyLocation();
        });

        back.setOnClickListener(view -> {
            finish();
        });

        selected = getResources().getDrawable(R.drawable.gps_selected, null);
        selected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        unselected = getResources().getDrawable(R.drawable.gps_unselected, null);
        unselected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        myLocationMarker = getResources().getDrawable(R.drawable.loading_back, null);
        myLocationMarker.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        loading.setLoadingStarted();
        requestGPSPermission();
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
        getMyLocation();
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
                StaticData.myLatestLocation = nGeoPoint;
                myLocationMarkerIsVisible = true;

                addMarkerTo(StaticData.myLatestLocation, true, LOCATION_MYLOCATION);
                getNearbyPubs(nGeoPoint);
            }

            @Override
            public void onLocationTimeout() {
                Toast.makeText(getApplicationContext(), "현재 위치를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo);
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                Toast.makeText(getApplicationContext(), "현재 위치를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo);
            }

            @Override
            public void onHasNoLocationPermission() {
                Toast.makeText(getApplicationContext(), "설정에서 위치정보 사용을 수락해 주세요", Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo);
            }

            @Override
            public void onGpsIsOff() {
                Toast.makeText(getApplicationContext(), "위치를 켜 주세요", Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo);
            }
        });

    }


    private void addMarkerTo(NGeoPoint geoPoint, boolean isMyLocation, String name) {

        poiData.beginPOIdata(1);
        if (isMyLocation) {
            poiData.addPOIitem(geoPoint, LOCATION_MYLOCATION, myLocationMarker, 0);
        } else {
            poiData.addPOIitem(geoPoint, name, unselected, 1);
        }
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    private void onMarkerClicked(NMapOverlayItem nMapOverlayItem) {
        JLog.v(nMapOverlayItem.getTitle());

//        mapView.getMapController().animateTo(nMapOverlayItem.getPoint());

        int position=0;
        if (nMapOverlayItem.getTitle().equals(LOCATION_MYLOCATION)) {
            return;
        }else{
            try {
                position = Integer.parseInt(nMapOverlayItem.getTitle());
            }catch (Exception e){
                JLog.e("parsing integer error");
                return;
            }
        }

        if (currentlyFocusedMarker != null) {
            currentlyFocusedMarker.setMarker(unselected);
        }

        currentlyFocusedMarker = nMapOverlayItem;
        currentlyFocusedMarker.setMarker(selected);

        pubInfoLayout.setVisibility(View.VISIBLE);

        String distanceString = "";

        if(StaticData.myLatestLocation!=null && myLocationMarkerIsVisible) {
            Double distance = GeoHelper.getActualKilometer(StaticData.myLatestLocation.latitude, StaticData.myLatestLocation.longitude,
                    nMapOverlayItem.getPoint().getLatitude(), nMapOverlayItem.getPoint().getLongitude());
            distanceString = GeoHelper.getDistanceString(distance);
        }

        Picasso.with(this).load(pubInfoArray.get(position).imageAddress.get(0)).into(pubImage);
        pubText1.setText(pubInfoArray.get(position).name+"  "+distanceString);
        pubText2.setText(pubInfoArray.get(position).businessType);
        pubText4.setText(pubInfoArray.get(position).address);

        int finalPosition = position;
        pubInfoLayout.setOnClickListener(view -> {
            Intent intent=new Intent(NearbyPlaces.this,PubPage.class);
            intent.putExtra("info",pubInfoArray.get(finalPosition));
            startActivity(intent);
        });
    }

    private void getNearbyPubs(NGeoPoint geoPoint) {
        JLog.v("getNearbyPubs");

        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("at", Integer.toString(pubInfoArray.size()));
            map.put("user_lat", Double.toString(geoPoint.getLatitude()));
            map.put("user_lng", Double.toString(geoPoint.getLongitude()));
            map.put("id_member", Long.toString(StaticData.currentUser.id));

            map = ServerConnectionHelper.connect("retrieving nearby pubs", "nearbyplace", map);

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
                boolean favorite = false;
                if (map.get("like_" + num).equals("TRUE")) {
                    favorite = true;
                }
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));
                int drinkProvideType = Integer.parseInt(map.get("alcoholpertable_" + num));

                pubInfoArray.add(new PubInfo(id, name, address, "주점", imageAddress, favorite, lat, lng, drinkProvideType));
            }
            new Handler(getMainLooper()).post(this::updateMarkers);
            loading.setLoadingCompleted();
        }).start();
    }

    private void updateMarkers(){
        poiData.removeAllPOIdata();

        if(StaticData.myLatestLocation!=null && myLocationMarkerIsVisible){
            addMarkerTo(StaticData.myLatestLocation,true,LOCATION_MYLOCATION);
        }

        for(int i=0;i<pubInfoArray.size();i++){
//            JLog.v("adding",i," - "+pubInfoArray.get(i).name);
            NGeoPoint geoPoint = new NGeoPoint(pubInfoArray.get(i).longitude,pubInfoArray.get(i).latitude);

            if(i == 0){
                mapView.getMapController().setMapCenter(geoPoint,13);
            }

            addMarkerTo(geoPoint,false,Integer.toString(i));

//            poiData.beginPOIdata(1);
//            poiData.addPOIitem(geoPoint, Integer.toString(i), unselected, 1);
//            poiData.endPOIdata();
        }

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    private void removeFocusFromMarker(){
        pubInfoLayout.setVisibility(View.INVISIBLE);
        currentlyFocusedMarker.setMarker(unselected);
        currentlyFocusedMarker=null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }

}
