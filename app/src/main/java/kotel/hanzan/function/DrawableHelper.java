package kotel.hanzan.function;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class DrawableHelper {

    /** This method exists because of deprecation of getDrawable(int res)
     *  of android phones which has api lower than Lollipop(api 21). */
    public static Drawable getDrawable(Resources res,  int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return res.getDrawable(id,null);
        }else{
            return res.getDrawable(id);
        }
    }
}
