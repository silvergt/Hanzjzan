package kotel.hanzan.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import kotel.hanzan.listener.HorizontalSlideViewChildListener;

/**
 *  1. This view is for HorizontalSlideView
 *  2. Nothing's different from RelativeLayout, but has method .setSlideViewChildClickListener
 *  3. If you want to call click event manually, use .callClickEvent()
 */


public class HorizontalSlideViewChild extends RelativeLayout {
    HorizontalSlideViewChildListener listener;

    public HorizontalSlideViewChild(Context context) {
        super(context);
    }

    public HorizontalSlideViewChild(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Deprecated
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
    }

    public void setSlideViewChildClickListener(HorizontalSlideViewChildListener listener){
        this.listener=listener;
    }

    public void callClickEvent(){
        if(listener!=null) {
            listener.onClick();
        }
    }

}
