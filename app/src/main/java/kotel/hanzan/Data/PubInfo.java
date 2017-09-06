package kotel.hanzan.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;

public class PubInfo implements Serializable{

    public long id;
    public String name,address,district;
    protected boolean favorite;
    public double latitude,longitude;

    public String phone,dayoff,work_weekday,work_weekend,description;
    public ArrayList<String> imageAddress;
    public ArrayList<DrinkInfo> drinkList;

    public PubInfo(long id, String name, String address, String district, String imageAddress, boolean favorite, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.imageAddress = new ArrayList<>();
        this.imageAddress.add(imageAddress);
        this.favorite = favorite;
        this.latitude = latitude;
        this.longitude = longitude;

        drinkList = new ArrayList<>();
    }

    public void setFavorite(boolean favoriteIsOn){
        favorite = favoriteIsOn;
        //send favorite info to server here
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("id_place",Long.toString(id));
            map = ServerConnectionHelper.connect("clicked like button","likeplace",map);
            if(map.get("likeresult").equals("TRUE")){
                JLog.v("favorite click info sended");
            }
        }).start();
    }

    public boolean getFavorite(){
        return favorite;
    }

}