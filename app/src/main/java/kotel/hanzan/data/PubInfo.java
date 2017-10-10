package kotel.hanzan.data;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.R;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;

public class PubInfo implements Serializable{

    public long id;
    public String name,address,district,drinkTypes;
    protected boolean favorite;
    public double latitude,longitude;

    public String phone,dayoff,work_weekday,work_weekend,description;
    public ArrayList<String> imageAddress;
    public ArrayList<DrinkInfo> drinkList;
    public ArrayList<String> menuImageAddress;

    public boolean tutorialPub = false;

    public PubInfo(Context context, long id, String name, String address, String district, String drinkTypes, String imageAddress, boolean favorite, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.imageAddress = new ArrayList<>();
        this.imageAddress.add(imageAddress);
        this.favorite = favorite;
        this.latitude = latitude;
        this.longitude = longitude;
        this.drinkList = new ArrayList<>();
        this.menuImageAddress = new ArrayList<>();
        this.drinkTypes="";
        if(!drinkTypes.equals("")) {
            String[] drinkTypesTemp = drinkTypes.split(",");
            for (int i = 0; i < drinkTypesTemp.length; i++) {
                this.drinkTypes += DrinkInfo.getDrinkName(context, drinkTypesTemp[i]);
                if (i != drinkTypesTemp.length - 1) {
                    this.drinkTypes += ", ";
                }else{
                    this.drinkTypes += (" "+context.getString(R.string.isFree));
                }
            }
        }
    }

    public boolean setFavorite(boolean favoriteIsOn){
        final boolean[] sendingSucceed = {false};
        //send favorite info to server here
        Thread th = new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("id_place",Long.toString(id));
            map = ServerConnectionHelper.connect("clicked like button","likeplace",map);
            if(map.get("likeresult")==null||map.get("likeresult").equals("FALSE")){
                sendingSucceed[0] = false;
            }else if(map.get("likeresult").equals("TRUE")){
                JLog.v("favorite click info sended");
                favorite = favoriteIsOn;
                sendingSucceed[0] = true;
            }
        });
        th.start();
        try{
            th.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        return sendingSucceed[0];
    }

    public boolean getFavorite(){
        return favorite;
    }

}