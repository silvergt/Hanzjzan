package kotel.hanzan.Data;

public class PubHistoryInfo extends PubInfo {
    public DrinkInfo drinkInfo;
    public int YYYY,MM,DD;

    public PubHistoryInfo(long id, String name, String address, String businessType,
                          String imageAddress, boolean favorite, double latitude, double longitude,int drinkProvideType,
                          DrinkInfo drinkInfo, int YYYY,int MM,int DD) {
        super(id, name, address, businessType, imageAddress, favorite, latitude, longitude,drinkProvideType);
        this.drinkInfo = drinkInfo;
        this.YYYY = YYYY;
        this.MM = MM;
        this.DD = DD;
    }

    public PubHistoryInfo(PubInfo pubInfo,DrinkInfo drinkInfo, int YYYY,int MM,int DD){
        super(pubInfo.id, pubInfo.name, pubInfo.address, pubInfo.businessType, pubInfo.imageAddress.get(0), pubInfo.favorite, pubInfo.latitude, pubInfo.longitude,pubInfo.drinkProvideType);
        this.drinkInfo = drinkInfo;
        this.YYYY = YYYY;
        this.MM = MM;
        this.DD = DD;
    }

}
