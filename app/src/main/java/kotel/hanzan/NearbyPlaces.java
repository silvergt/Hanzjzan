package kotel.hanzan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.PubInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.DrawableHelper;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.view.JNMapActivity;
import kotel.hanzan.view.Loading;

public class NearbyPlaces extends JNMapActivity {
    final private String LOCATION_MYLOCATION = "LOCATION_MYLOCATION";

    private NMapView mapView;

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapPOIdata poiData;

    private NMapOverlayItem currentlyFocusedMarker;
    private PubInfo currentlyFocusedPubInfo;

    private LocationHelper locationHelper = new LocationHelper();

    private Drawable selected, unselected, myLocationMarker;

    private int drawableWidth, drawableHeight;

    private Loading loading;
    private ImageView myLocationButton, back, pubImage;
    private LinearLayout pubInfoLayout;
    private TextView pubText1, pubText2, pubText3, pubText4, informationText;

    private ArrayList<PubInfo> pubInfoArray = new ArrayList<>();
    private ArrayList<DistrictInfo> districtInfoArray = new ArrayList<>();

    private boolean myLocationMarkerIsVisible = false;

    private class DistrictInfo{
        Drawable image;
        int quantity;
        double lat,lng;

        public DistrictInfo(String imageAddress,int quantity, double lat, double lng) {
            try {
                Uri uri = Uri.parse(imageAddress);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                image = Drawable.createFromStream(inputStream, uri.toString());
            } catch (FileNotFoundException e) {
                image = DrawableHelper.getDrawable(getResources(),R.drawable.drinkselector_default);
            }
            this.quantity = quantity;
            this.lat = lat;
            this.lng = lng;
            image.setBounds(-drawableWidth * 2/ 3, -drawableHeight * 2/ 3, drawableWidth * 2/ 3, drawableHeight * 2/ 3);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearbyplaces);

        init();
    }

    private void init() {
        drawableWidth = (int) getResources().getDimension(R.dimen.markerWidth);
        drawableHeight = (int) getResources().getDimension(R.dimen.markerHeight);

        loading = findViewById(R.id.nearbyPlaces_loading);
        myLocationButton = findViewById(R.id.nearbyPlaces_myLocation);
        back = findViewById(R.id.nearbyPlaces_back);
        pubInfoLayout = findViewById(R.id.nearbyPlaces_pubInfoLayout);
        pubImage = findViewById(R.id.nearbyPlaces_pubImage);
        pubText1 = findViewById(R.id.nearbyPlaces_pubText1);
        pubText2 = findViewById(R.id.nearbyPlaces_pubText2);
        pubText3 = findViewById(R.id.nearbyPlaces_pubText3);
        pubText4 = findViewById(R.id.nearbyPlaces_pubText4);
        informationText = findViewById(R.id.nearbyPlaces_information);

        mapView = findViewById(R.id.nearbyPlaces_mapView);
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

        myLocationButton.setOnClickListener(view -> {
            pubInfoLayout.setVisibility(View.INVISIBLE);
            loading.setLoadingStarted();
            pubInfoArray.clear();
            getMyLocation();
        });

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
                if(mapView.getMapController().getZoomLevel()>=11) {
                    removeFocusFromMarker();
                }
            }

            @Override
            public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {

            }

            @Override
            public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {

            }
        });

        mapView.setOnMapStateChangeListener(new NMapView.OnMapStateChangeListener() {
            @Override
            public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

            }

            @Override
            public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
                retrieveMapDataFromServer(mapView.getMapController().getMapCenter(), false);
            }

            @Override
            public void onMapCenterChangeFine(NMapView nMapView) {
                JLog.v("QQQ");
                retrieveMapDataFromServer(mapView.getMapController().getMapCenter(), false);
            }

            @Override
            public void onZoomLevelChange(NMapView nMapView, int i) {
                retrieveMapDataFromServer(mapView.getMapController().getMapCenter(), false);
            }

            @Override
            public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

            }
        });

        back.setOnClickListener(view -> {
            finish();
        });

        selected = DrawableHelper.getDrawable(getResources(), R.drawable.gps_selected);
        selected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        unselected = DrawableHelper.getDrawable(getResources(), R.drawable.gps_unselected);
        unselected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        myLocationMarker = DrawableHelper.getDrawable(getResources(), R.drawable.gps_mylocation);
        myLocationMarker.setBounds(-drawableWidth / 2, -drawableWidth / 2, drawableWidth / 2, drawableWidth / 2);

        new Handler(getMainLooper()).post(this::requestGPSPermission);
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
                myLocationMarkerIsVisible = true;

                addMarkerTo(StaticData.myLatestLocation, true, false, LOCATION_MYLOCATION);
                retrieveMapDataFromServer(nGeoPoint, true);
                loading.setLoadingCompleted();
            }

            @Override
            public void onLocationTimeout() {
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                retrieveMapDataFromServer(geo, true);
                loading.setLoadingCompleted();
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                retrieveMapDataFromServer(geo, true);
                loading.setLoadingCompleted();
            }

            @Override
            public void onHasNoLocationPermission() {
                Toast.makeText(getApplicationContext(), getString(R.string.allowGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                retrieveMapDataFromServer(geo, true);
                loading.setLoadingCompleted();
            }

            @Override
            public void onGpsIsOff() {
                Toast.makeText(getApplicationContext(), getString(R.string.turnOnGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                retrieveMapDataFromServer(geo, true);
                loading.setLoadingCompleted();
            }
        });
    }



    private void retrieveMapDataFromServer(NGeoPoint geo, boolean returnToMyLocation){
        if(mapView.getMapController().getZoomLevel()>=0) {
            informationText.setVisibility(View.GONE);
            pubInfoArray.clear();
            retrieveNearbyPubs(geo, returnToMyLocation);
        }else{
            pubInfoArray.clear();
            updateMarkers(false);

            informationText.setVisibility(View.VISIBLE);
            if(districtInfoArray.size()==0){
                retrieveDistrictInfo(returnToMyLocation);
            }else{
                updateDistrictMarkers(returnToMyLocation);
            }
        }
    }


    //ZOOM LEVEL ABOVE OR EQUALS 11

    private synchronized void retrieveNearbyPubs(NGeoPoint geoPoint, boolean returnToMyLocation) {
        NGeoPoint[] edgeGeo = GeoHelper.getMapStartEndPoint(geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6(),
                mapView.getMapProjection().getLatitudeSpan(), mapView.getMapProjection().getLongitudeSpan());

        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();
            JLog.v(Double.toString(edgeGeo[0].getLatitude()));
            JLog.v(Double.toString(edgeGeo[0].getLongitude()));
            JLog.v(Double.toString(edgeGeo[1].getLatitude()));
            JLog.v(Double.toString(edgeGeo[1].getLongitude()));
            map.put("lat1", Double.toString(edgeGeo[0].getLatitude()));
            map.put("lng1", Double.toString(edgeGeo[0].getLongitude()));
            map.put("lat2", Double.toString(edgeGeo[1].getLatitude()));
            map.put("lng2", Double.toString(edgeGeo[1].getLongitude()));
            map.put("distance", Double.toString(GeoHelper.getActualKilometer(edgeGeo[0].getLatitude(), edgeGeo[0].getLongitude(), edgeGeo[1].getLatitude(), edgeGeo[1].getLongitude())));

            map = ServerConnectionHelper.connect("retrieving nearby pubs", "mapplace", map);

            int i = 0;
            while (true) {
                String num = Integer.toString(i++);
                if (map.get("id_place_" + num) == null) {
                    break;
                }
                long id = Long.parseLong(map.get("id_place_" + num));
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));

                pubInfoArray.add(new PubInfo(getApplicationContext(),id, "", "", "", "","", false, lat, lng));
            }
            new Handler(getMainLooper()).post(() -> {
                updateMarkers(returnToMyLocation);
            });
            loading.setLoadingCompleted();
        }).start();
    }

    private synchronized void retrievePubByPubId(int pubId){
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("id_place",Integer.toString(pubId));

            map = ServerConnectionHelper.connect("retrieving selected pub's info", "mapplaceinfo", map);

            if (map.get("name_place") == null) {
                return;
            }
            String name = map.get("name_place");
            String address = map.get("address_place");
            String imageAddress = map.get("imgadd_place");
            String district = map.get("district");
            String drinkType = map.get("drink_place");
            boolean favorite = false;
            if (map.get("like").equals("TRUE")) {
                favorite = true;
            }
            double lat = Double.parseDouble(map.get("lat"));
            double lng = Double.parseDouble(map.get("lng"));

            currentlyFocusedPubInfo = new PubInfo(NearbyPlaces.this,pubId, name, address, district, drinkType, imageAddress, favorite, lat, lng);
            new Handler(getMainLooper()).post(() -> {

                String distanceString = "";

                if (StaticData.myLatestLocation != null && myLocationMarkerIsVisible) {
                    Double distance = GeoHelper.getActualKilometer(StaticData.myLatestLocation.latitude, StaticData.myLatestLocation.longitude,
                            currentlyFocusedPubInfo.latitude, currentlyFocusedPubInfo.longitude);
                    distanceString = GeoHelper.getDistanceString(distance);
                }

                Picasso.with(this).load(currentlyFocusedPubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(pubImage);
                pubText1.setText(currentlyFocusedPubInfo.name + "  " + distanceString);
                pubText2.setText(currentlyFocusedPubInfo.drinkTypes);
                pubText3.setText(currentlyFocusedPubInfo.district);
                pubText4.setText(currentlyFocusedPubInfo.address);

                pubInfoLayout.setOnClickListener(view -> {
                    Intent intent = new Intent(NearbyPlaces.this, PubPage.class);
                    intent.putExtra("info", currentlyFocusedPubInfo);
                    startActivity(intent);
                });
            });
            loading.setLoadingCompleted();
        }).start();
    }

    private void updateMarkers(boolean returnToMyLocation) {
        poiData.removeAllPOIdata();

        for (int i = 0; i < pubInfoArray.size(); i++) {
            NGeoPoint geoPoint = new NGeoPoint(pubInfoArray.get(i).longitude, pubInfoArray.get(i).latitude);
            addMarkerTo(geoPoint, false, false, Long.toString(pubInfoArray.get(i).id));
        }

        if (returnToMyLocation) {
            if(StaticData.myLatestLocation == null) {
                StaticData.myLatestLocation = StaticData.defaultLocation;
            }
            mapView.getMapController().setMapCenter(StaticData.myLatestLocation, 13);
        }
        if (StaticData.myLatestLocation != null && myLocationMarkerIsVisible) {
            addMarkerTo(StaticData.myLatestLocation, true, false, LOCATION_MYLOCATION);
        }
        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);

        pubInfoLayout.setVisibility(View.INVISIBLE);
    }

    private void addMarkerTo(NGeoPoint geoPoint, boolean isMyLocation, boolean setSelected, String name) {

        poiData.beginPOIdata(1);
        if (isMyLocation) {
            poiData.addPOIitem(geoPoint, LOCATION_MYLOCATION, myLocationMarker, 0);
        } else if (setSelected) {
            poiData.addPOIitem(geoPoint, name, selected, 2);
        } else {
            poiData.addPOIitem(geoPoint, name, unselected, 1);
        }
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }

    private void onMarkerClicked(NMapOverlayItem nMapOverlayItem) {
        JLog.v("marker title",nMapOverlayItem.getTitle());

        int position;
        try {
            if (nMapOverlayItem.getTitle().equals(LOCATION_MYLOCATION)) {
                return;
            } else {
                try {
                    JLog.v("INTEGER~ ",nMapOverlayItem.getTitle());
                    position = Integer.parseInt(nMapOverlayItem.getTitle());
                } catch (Exception e) {
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

            Picasso.with(this).load(R.drawable.loading_store).into(pubImage);
            pubText1.setText("");
            pubText2.setText("");
            pubText3.setText("");
            pubText4.setText("");

            retrievePubByPubId(position);

        }catch (Exception e){
            e.printStackTrace();
            pubInfoArray.clear();
            retrieveMapDataFromServer(mapView.getMapController().getMapCenter(), false);
        }
    }


    //ZOOM LEVEL BELOW 11

    private void retrieveDistrictInfo(boolean returnToMyLocation){

    }

    private void updateDistrictMarkers(boolean returnToMyLocation) {

    }

    private void addDistrictMarkerTo(NGeoPoint geoPoint, String districtName, int pubQuantity){
        poiData.beginPOIdata(1);
        poiData.addPOIitem(geoPoint, LOCATION_MYLOCATION, myLocationMarker, 0);
        poiData.endPOIdata();

        overlayManager.clearOverlays();
        overlayManager.createPOIdataOverlay(poiData, null);
    }




    private void removeFocusFromMarker() {
        if(currentlyFocusedMarker!=null) {
            pubInfoLayout.setVisibility(View.INVISIBLE);
            currentlyFocusedMarker.setMarker(unselected);
            currentlyFocusedMarker = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }
}
