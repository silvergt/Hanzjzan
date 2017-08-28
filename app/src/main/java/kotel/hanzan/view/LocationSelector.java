package kotel.hanzan.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


public class LocationSelector extends RelativeLayout {
    private Context context;

    public LocationSelector(Context context) {
        super(context);
        init(context);
    }

    public LocationSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context = context;
    }
}
