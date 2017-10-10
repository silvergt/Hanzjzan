package kotel.hanzan.data;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;
import java.util.HashMap;

import kotel.hanzan.R;

public class DrinkInfo implements Serializable{


    public String drinkType;
    public String drinkName;
    public String drinkImageAddress;

    public DrinkInfo(String drinkType, String drinkName,String drinkImageAddress) {
        this.drinkType = drinkType;
        this.drinkName = drinkName;
        this.drinkImageAddress = drinkImageAddress;
    }



    /** Returns drink name(ENG,KOR). code is drink name text retrieved from server. */
    public static String getDrinkName(Context context,String code){
        String returnValue;
        final HashMap<String,Integer> drinkIdMap = new HashMap<String,Integer>(){
            {
                put("makgeolli",100);
                put("soju",200);
                put("beer",300);
                put("sake",400);
                put("liquor",500);
                put("misc",600);
                put("snack",700);
                put("cocktail",800);
            }
        };

        Resources res = context.getResources();

        int drinkCode;
        try {
            drinkCode = drinkIdMap.get(code);
        }catch (Exception e){
            return "UNKNOWN";
        }

        switch (drinkCode){
            case 100:returnValue = res.getString(R.string.makgeolli); break;
            case 200:returnValue = res.getString(R.string.soju); break;
            case 300:returnValue = res.getString(R.string.beer); break;
            case 400:returnValue = res.getString(R.string.sake); break;
            case 500:returnValue = res.getString(R.string.liquor); break;
            case 600:returnValue = res.getString(R.string.misc); break;
            case 700:returnValue = res.getString(R.string.snack); break;
            case 800:returnValue = res.getString(R.string.cocktail); break;
            default:returnValue = "UNKNOWN";break;

        }

        return returnValue;
    }
}
