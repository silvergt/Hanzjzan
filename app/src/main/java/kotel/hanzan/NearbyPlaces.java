package kotel.hanzan;

import android.Manifest;
import android.content.Context;
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
import kotel.hanzan.function.DrawableHelper;
import kotel.hanzan.function.GeoHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocaleHelper;
import kotel.hanzan.function.LocationHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.LocationHelperListener;
import kotel.hanzan.view.JNMapActivity;
import kotel.hanzan.view.Loading;

public class NearbyPlaces extends JNMapActivity {
    final private String LOCATION_MYLOCATION = "LOCATION_MYLOCATION";

    private NMapView mapView;// 지도 화면 View

    private NMapResourceProvider provider;
    private NMapOverlayManager overlayManager;
    private NMapPOIdata poiData;
//    private NGeoPoint myLocation;

    private NMapOverlayItem currentlyFocusedMarker;
    private PubInfo currectlyFocusedPubInfo;

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
        setContentView(R.layout.activity_nearbyplaces);

        init();
    }

    private void init() {
        drawableWidth = (int) getResources().getDimension(R.dimen.markerWidth);
        drawableHeight = (int) getResources().getDimension(R.dimen.markerHeight);

        loading = (Loading) findViewById(R.id.nearbyPlaces_loading);
        myLocationButton = (ImageView) findViewById(R.id.nearbyPlaces_myLocation);
        back = (ImageView) findViewById(R.id.nearbyPlaces_back);
        pubInfoLayout = (LinearLayout) findViewById(R.id.nearbyPlaces_pubInfoLayout);
        pubImage = (ImageView) findViewById(R.id.nearbyPlaces_pubImage);
        pubText1 = (TextView) findViewById(R.id.nearbyPlaces_pubText1);
        pubText2 = (TextView) findViewById(R.id.nearbyPlaces_pubText2);
        pubText3 = (TextView) findViewById(R.id.nearbyPlaces_pubText3);
        pubText4 = (TextView) findViewById(R.id.nearbyPlaces_pubText4);

        mapView = (NMapView) findViewById(R.id.nearbyPlaces_mapView);
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
            boolean scrolled = false;
            int span = 0;

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
                if (scrolled) {
                    pubInfoArray.clear();
                    getNearbyPubs(mapView.getMapController().getMapCenter(), false);
                    scrolled = false;
                } else if (mapView.getMapProjection().getLatitudeSpan() != span) {
                    pubInfoArray.clear();
                    getNearbyPubs(mapView.getMapController().getMapCenter(), false);
                    span = mapView.getMapProjection().getLatitudeSpan();
                }
            }

            @Override
            public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {
                scrolled = true;
            }

            @Override
            public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {
                if (currentlyFocusedMarker != null) {
                    removeFocusFromMarker();
                    scrolled = false;
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

        selected = DrawableHelper.getDrawable(getResources(), R.drawable.gps_selected);
        selected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        unselected = DrawableHelper.getDrawable(getResources(), R.drawable.gps_unselected);
        unselected.setBounds(-drawableWidth / 2, -drawableHeight, drawableWidth / 2, 0);

        myLocationMarker = DrawableHelper.getDrawable(getResources(), R.drawable.gps_mylocation);
        myLocationMarker.setBounds(-drawableWidth / 2, -drawableWidth / 2, drawableWidth / 2, drawableWidth / 2);

        loading.setLoadingStarted();

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
                getNearbyPubs(nGeoPoint, true);
            }

            @Override
            public void onLocationTimeout() {
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo, true);
            }

            @Override
            public void onLocationUnavailableArea(NGeoPoint nGeoPoint) {
                Toast.makeText(getApplicationContext(), getString(R.string.failedGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo, true);
            }

            @Override
            public void onHasNoLocationPermission() {
                Toast.makeText(getApplicationContext(), getString(R.string.allowGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo, true);
            }

            @Override
            public void onGpsIsOff() {
                Toast.makeText(getApplicationContext(), getString(R.string.turnOnGPS), Toast.LENGTH_SHORT).show();
                NGeoPoint geo = StaticData.defaultLocation;
                myLocationMarkerIsVisible = false;

                getNearbyPubs(geo, true);
            }
        });
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

        try {
            int position;
            if (nMapOverlayItem.getTitle().equals(LOCATION_MYLOCATION)) {
                return;
            } else {
                try {
                    position = Integer.parseInt(nMapOverlayItem.getTitle());
                } catch (Exception e) {
                    JLog.e("parsing integer error");
                    return;
                }
            }

            if (currentlyFocusedMarker != null) {
                currentlyFocusedMarker.setMarker(unselected);
            }

            currectlyFocusedPubInfo = pubInfoArray.get(position);
            currentlyFocusedMarker = nMapOverlayItem;
            currentlyFocusedMarker.setMarker(selected);

            pubInfoLayout.setVisibility(View.VISIBLE);

            String distanceString = "";

            if (StaticData.myLatestLocation != null && myLocationMarkerIsVisible) {
                Double distance = GeoHelper.getActualKilometer(StaticData.myLatestLocation.latitude, StaticData.myLatestLocation.longitude,
                        nMapOverlayItem.getPoint().getLatitude(), nMapOverlayItem.getPoint().getLongitude());
                distanceString = GeoHelper.getDistanceString(distance);
            }

            Picasso.with(this).load(currectlyFocusedPubInfo.imageAddress.get(0)).placeholder(R.drawable.loading_store).into(pubImage);
            pubText1.setText(currectlyFocusedPubInfo.name + "  " + distanceString);
            pubText2.setText(currectlyFocusedPubInfo.district);
            pubText4.setText(currectlyFocusedPubInfo.address);

            pubInfoLayout.setOnClickListener(view -> {
                Intent intent = new Intent(NearbyPlaces.this, PubPage.class);
                intent.putExtra("info", currectlyFocusedPubInfo);
                startActivity(intent);
            });
        }catch (Exception e){
            e.printStackTrace();
            pubInfoArray.clear();
            getNearbyPubs(mapView.getMapController().getMapCenter(), false);
        }
    }

    private synchronized void getNearbyPubs(NGeoPoint geoPoint, boolean returnToMyLocation) {
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
                String name = map.get("name_place_" + num);
                String address = map.get("address_place_" + num);
                String imageAddress = map.get("imgadd_place_" + num);
                String district = map.get("district_" + num);
                boolean favorite = false;
                if (map.get("like_" + num).equals("TRUE")) {
                    favorite = true;
                }
                double lat = Double.parseDouble(map.get("lat_" + num));
                double lng = Double.parseDouble(map.get("lng_" + num));

                pubInfoArray.add(new PubInfo(id, name, address, district, imageAddress, favorite, lat, lng));
            }
            new Handler(getMainLooper()).post(() -> {
                updateMarkers(returnToMyLocation);
            });
            loading.setLoadingCompleted();
        }).start();
    }

    private void updateMarkers(boolean returnToMyLocation) {
        poiData.removeAllPOIdata();

        for (int i = 0; i < pubInfoArray.size(); i++) {
            NGeoPoint geoPoint = new NGeoPoint(pubInfoArray.get(i).longitude, pubInfoArray.get(i).latitude);

            addMarkerTo(geoPoint, false, false, Integer.toString(i));
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

    private void removeFocusFromMarker() {
        pubInfoLayout.setVisibility(View.INVISIBLE);
        currentlyFocusedMarker.setMarker(unselected);
        currentlyFocusedMarker = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

}
