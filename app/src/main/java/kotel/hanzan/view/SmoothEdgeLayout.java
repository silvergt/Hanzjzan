package kotel.hanzan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;


/** Make this as a parent view of a view you want to make it scroll smoothly at the start and end of the view. */

public class SmoothEdgeLayout extends TwinklingRefreshLayout{
    Context context;

    public SmoothEdgeLayout(Context context) {
        super(context);
        init(context);
    }

    public SmoothEdgeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context = context;

        setEnableRefresh(false);
        setEnableLoadmore(false);

        setBackgroundColor(Color.WHITE);
    }

}
