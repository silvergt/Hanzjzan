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

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.R;
import kotel.hanzan.listener.LocationHelperListener;

/**
 * 1. User LocationHelperListener to get Location from every possible situations
 * 2. You have to check if permission had been granted. and if there's no permission granted, I recommend you to request permission before use this class
 */

public class LocationHelper {
    NMapLocationManager locationManager;


    /** Get user's current location one time. Retrieved location can be modified/used by implementing listener. */
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



    /** Returns location's dong( ex : 청담동, 창천동) according to lat lng */
    public static String getLocationDongBy(Context context, double lat, double lng) {
        final String[] returnValue = {""};
        Thread thread = new Thread(()->{
            Geocoder geo = new Geocoder(context);
            try {
                List<Address> list =geo.getFromLocation(lat, lng, 1);
                Address address = list.get(0);
                JLog.v("Dong", address.getThoroughfare());
                JLog.v("Full loc", address.getAddressLine(0));
                String concatenationString = address.getThoroughfare() == null || address.getThoroughfare().equals("null") ?
                        getLocationDongBy(context, StaticData.defaultLocation.getLatitude(),StaticData.defaultLocation.getLongitude()) :
                        address.getThoroughfare();
                returnValue[0] = returnValue[0].concat(concatenationString);
            }catch (Exception e){e.printStackTrace();}
        });
        thread.start();
        try {
            thread.join();
        }catch (Exception e){e.printStackTrace();}

        return returnValue[0];
    }

    /** Returns location's full name( ex : 대한민국 서울특별시 청담동 ~~~~) according to lat lng */
    public static String getFullLocationBy(Context context, double lat, double lng) {
        final String[] returnValue = {""};
        Thread thread = new Thread(()->{
            Geocoder geo = new Geocoder(context);
            try {
                List<Address> list =geo.getFromLocation(lat, lng, 1);
                Address address = list.get(0);
                JLog.v("Full loc", address.getAddressLine(0));
                String concatenationString = address.getLocality() + " " + address.getSubLocality() + " " +
                        address.getThoroughfare() + " " + address.getSubThoroughfare();
                returnValue[0] = returnValue[0].concat(concatenationString);
            }catch (Exception e){
                e.printStackTrace();
                returnValue[0] = returnValue[0].concat(context.getString(R.string.unknownLocation));
            }
        });
        thread.start();
        try {
            thread.join();
        }catch (Exception e){e.printStackTrace();}

        return returnValue[0];
    }

    /** Returns location's lat lng in NGeoPoint class form according to address */
    public static NGeoPoint getLocationLatLngBy(Context context, String address){
        final NGeoPoint[] returnValue = new NGeoPoint[1];
        JLog.v("address~",address);
        Thread thread = new Thread(()->{
            Geocoder geo = new Geocoder(context);
            try {
                List<Address> list =geo.getFromLocationName(address,1);
                Address addressTemp = list.get(0);
                returnValue[0] = new NGeoPoint(addressTemp.getLongitude(),addressTemp.getLatitude());
            }catch (Exception e){e.printStackTrace();}
        });
        thread.start();
        try {
            thread.join();
        }catch (Exception e){e.printStackTrace();}

        return returnValue[0];
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
