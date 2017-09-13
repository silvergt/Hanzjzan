package kotel.hanzan.function;


import android.content.res.Resources;
import android.os.Build;

public class ColorHelper {

    @SuppressWarnings("deprecation")
    public static int getColor(Resources res, int id){
        if (Build.VERSION.SDK_INT >= 23) {
            return res.getColor(id,null);
        }else{
            return res.getColor(id);
        }
    }
}
