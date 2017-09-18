package kotel.hanzan.function;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class AssetsHelper {

    /** IMAGE HELPER */

    public static RequestCreator loadDrinkImage(Context context, String drink){
        return Picasso.with(context).load("file:///android_asset/drinks/"+drink+".png");
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

    public static Drawable loadAssetImageDrawable(Context context, String folder, String name){
        Drawable drawable = null;
        try{
            drawable = Drawable.createFromStream(context.getAssets().open(folder+"/"+name+".png"),null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return drawable;
    }

    public static RequestCreator loadAssetImageCreator(Context context, String folder, String name){
        return Picasso.with(context).load("file:///android_asset/"+folder+"/"+name+".png");
    }



    /** TEXT HELPER */

    public static String loadText(Context context, String folder, String name){
        String returnValue = "";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(folder+"/"+name+".txt")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                returnValue = returnValue.concat(mLine+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return returnValue;
    }

}
