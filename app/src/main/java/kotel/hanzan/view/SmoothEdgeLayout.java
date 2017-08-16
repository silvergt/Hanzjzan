package kotel.hanzan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;


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
