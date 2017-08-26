package kotel.hanzan.function;

import com.nhn.android.maps.maplib.NGeoPoint;

public class GeoHelper {

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public static double getActualKilometer(double lat1,double lng1,double lat2,double lng2) {

        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(lng1 - lng2);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        JLog.v(Double.toString(AVERAGE_RADIUS_OF_EARTH_KM * c));

        return AVERAGE_RADIUS_OF_EARTH_KM * c;
    }

    public static String getDistanceString(double distance){
        String distanceString;

        if(distance < 1) {
            distanceString = String.format("%.1f",distance*1000)+"m";
        }else{
            distanceString = String.format("%.2f",distance)+"km";
        }

        return distanceString;
    }

    public static NGeoPoint[] getMapStartEndPoint(int centerLatE6, int centerLngE6, int latSpan, int lngSpan){
// Screen 시작 좌표
        int slat = centerLatE6 + (latSpan/2);
        int slng = centerLngE6 - (lngSpan/2);
        NGeoPoint startGeoPoint = new NGeoPoint(slng,slat);

// Screen 끝 좌표
        int elat = centerLatE6 - (latSpan/2);
        int elng = centerLngE6 + (lngSpan/2);
        NGeoPoint endGeoPoint = new NGeoPoint(elng,elat);

        return new NGeoPoint[]{startGeoPoint,endGeoPoint};

    }
}
