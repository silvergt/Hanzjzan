package kotel.hanzan.function;

public class GeoHelper {

    public static double getActualKilometer(double lat1,double lng1,double lat2,double lng2){
        double R = 6371;
        double dLat = deg2rad(lat2-lat1);
        double dLon = deg2rad(lng2-lng1);
        double a =Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;

        return Math.round(d);
    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
