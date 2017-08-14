package kotel.hanzan.Data;

public class PubInfo{
    long id;
    String name,address,businessType,imageAddress;
    boolean favorite;
    double latitude,longitude;

    public PubInfo(long id, String name, String address, String businessType, String imageAddress, boolean favorite, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.businessType = businessType;
        this.imageAddress = imageAddress;
        this.favorite = favorite;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PubInfo() {
    }
}