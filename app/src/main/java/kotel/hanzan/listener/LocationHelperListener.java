package kotel.hanzan.listener;

import com.nhn.android.maps.maplib.NGeoPoint;

public interface LocationHelperListener {
    void onSearchingStarted();
    void onSearchingEnded();

    void onLocationFound(NGeoPoint nGeoPoint);
    void onLocationTimeout();
    void onLocationUnavailableArea(NGeoPoint nGeoPoint);
    void onHasNoLocationPermission();
    void onGpsIsOff();
}
