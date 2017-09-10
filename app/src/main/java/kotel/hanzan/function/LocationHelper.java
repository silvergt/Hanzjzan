package kotel.hanzan.function;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;

import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;

import java.util.List;

import kotel.hanzan.listener.LocationHelperListener;

/**
 * 1. User LocationHelperListener to get Location from every possible situations
 * 2. You have to check if permission had been granted. and if there's no permission granted, I recommend you to request permission before use this class
 */

public class LocationHelper {
    NMapLocationManager locationManager;


    public void getMyLocationOnlyOneTime(Context context, LocationHelperListener listener) {
        listener.onSearchingStarted();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                listener.onHasNoLocationPermission();
                listener.onSearchingEnded();
                return;
            }
        }

        locationManager = new NMapLocationManager(context);
        locationManager.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {
            @Override
            public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
                locationManager.disableMyLocation();
                listener.onLocationFound(nGeoPoint);
                listener.onSearchingEnded();
                return true;
            }

            @Override
            public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {
                locationManager.disableMyLocation();
                listener.onLocationTimeout();
                listener.onSearchingEnded();
            }

            @Override
            public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
                locationManager.disableMyLocation();
                listener.onLocationUnavailableArea(nGeoPoint);
                listener.onSearchingEnded();
            }
        });


        LocationManager locMan = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if (!locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            listener.onGpsIsOff();
            listener.onSearchingEnded();
            return;
        }

        try {
            locationManager.enableMyLocation(false);
        } catch (Exception e) {
            listener.onHasNoLocationPermission();
            listener.onSearchingEnded();
            e.printStackTrace();
        }
    }

    public void getLocationNameBy(Context context, double lat, double lng) {
        Thread thread = new Thread(()->{
            Geocoder geo = new Geocoder(context);
            try {
                List<Address> list =geo.getFromLocation(lat, lng, 1);
                Address address = list.get(0);
                JLog.v("1",address.getCountryName());
                JLog.v("2",address.getAdminArea());
                JLog.v("3",address.getFeatureName());
                JLog.v("4",address.getLocality());
                JLog.v("5",address.getPostalCode());
                JLog.v("6",address.getSubAdminArea());
                JLog.v("7",address.getSubLocality());
                JLog.v("8",address.getPremises());
            }catch (Exception e){e.printStackTrace();}
        });
        thread.start();
        try {
            thread.join();
        }catch (Exception e){e.printStackTrace();}
    }



    public void onStop(){
        try{
            locationManager.disableMyLocation();
        }catch (Exception e){}
    }

    public void onRestart(){
        try{
            locationManager.enableMyLocation(false);
        }catch (Exception e){}
    }

}
