package kotel.hanzan.Data;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;
import java.util.HashMap;

import kotel.hanzan.R;

public class DrinkInfo implements Serializable{
    final public static HashMap<String,Integer> drinkIdMap = new HashMap<String,Integer>(){
        {
            put("mak",100);
            put("soju",200);
            put("beer",300);
            put("wine",400);
            put("sake",500);
            put("cocktail",600);
        }
    };

    public String drinkType;
    public String drinkName;

    public DrinkInfo(String drinkType, String drinkName) {
        this.drinkType = drinkType;
        this.drinkName = drinkName;
    }

    public int getDrinkPrice(){
        return 4000;
    }


    public static String getDrinkName(Context context,String code){
        String returnValue = "";

        Resources res = context.getResources();

        switch (drinkIdMap.get(code)){
            case 100:returnValue = res.getString(R.string.mak); break;
            case 200:returnValue = res.getString(R.string.soju); break;
            case 300:returnValue = res.getString(R.string.beer); break;
            case 400:returnValue = res.getString(R.string.wine); break;
            case 500:returnValue = res.getString(R.string.sake); break;
            case 600:returnValue = res.getString(R.string.cocktail); break;

        }

        return returnValue;
    }
}
