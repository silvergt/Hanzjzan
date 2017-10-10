package kotel.hanzan.listener;


import kotel.hanzan.data.DrinkInfo;

public interface DrinkSelectorListener {
    void itemSelected(DrinkInfo drinkInfo);
    void typeSelected(String typeName);
}
