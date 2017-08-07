package kotel.hanzan.function;

import android.content.Context;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class AssetImageHelper {
    public static RequestCreator loadDrinkImage(Context context, String drink){
        return Picasso.with(context).load("file:///android_asset/drinks/"+drink+".png");
    }
}
