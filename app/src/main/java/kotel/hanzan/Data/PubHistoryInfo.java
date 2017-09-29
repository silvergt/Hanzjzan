package kotel.hanzan.Data;

import java.io.Serializable;

public class PubHistoryInfo extends PubInfo implements Serializable {
    public DrinkInfo drinkInfo;
    public int YYYY,MM,DD;

    public PubHistoryInfo(long id, String name, String address, String district,
                          String imageAddress, boolean favorite, double latitude, double longitude,
                          DrinkInfo drinkInfo, int YYYY,int MM,int DD) {
        super(null,id, name, address, district,"", imageAddress, favorite, latitude, longitude);
        this.drinkInfo = drinkInfo;
        this.YYYY = YYYY;
        this.MM = MM;
        this.DD = DD;
    }

    public PubHistoryInfo(PubInfo pubInfo,DrinkInfo drinkInfo, int YYYY,int MM,int DD){
        super(null,pubInfo.id, pubInfo.name, pubInfo.address, pubInfo.district, pubInfo.drinkTypes, pubInfo.imageAddress.get(0), pubInfo.favorite, pubInfo.latitude, pubInfo.longitude);
        this.drinkInfo = drinkInfo;
        this.YYYY = YYYY;
        this.MM = MM;
        this.DD = DD;
    }

}
