package kotel.hanzan.Data;

import java.io.Serializable;

public class DrinkInfo implements Serializable{
    public String drinkType;
    public String drinkName;

    public DrinkInfo(String drinkType, String drinkName) {
        this.drinkType = drinkType;
        this.drinkName = drinkName;
    }

    public int getDrinkPrice(){
        return 4000;
    }
}
