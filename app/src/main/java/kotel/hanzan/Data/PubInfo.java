package kotel.hanzan.Data;

import java.io.Serializable;

public class PubInfo implements Serializable{
    public long id;
    public String name,address,businessType,phone;
    private boolean favorite;
    public double latitude,longitude;
    public String[] imageAddress;

    public PubInfo(long id, String name, String address, String businessType, String[] imageAddress, boolean favorite, double latitude, double longitude) {
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

    public void setFavorite(boolean favoriteIsOn){
        favorite = favoriteIsOn;
        //send favorite info to server here
    }

    public boolean getFavorite(){
        return favorite;
    }
}