package kotel.hanzan.function;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class AssetImageHelper {
    public static RequestCreator loadDrinkImage(Context context, String drink){
        return Picasso.with(context).load("file:///android_asset/drinks/"+drink+".png");
    }

    public static RequestCreator loadTapBarImage(Context context, String name){
        return Picasso.with(context).load("file:///android_asset/tapbar/"+name+".png");
    }

    public static Drawable loadTapBarImageDrawable(Context context, String name){
        Drawable drawable = null;
        try{
            drawable = Drawable.createFromStream(context.getAssets().open("tapbar/"+name+".png"),null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return drawable;
    }
}
