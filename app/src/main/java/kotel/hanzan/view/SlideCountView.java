package kotel.hanzan.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import kotel.hanzan.R;

/**
 *  1. First, you MUST set .initialize(~) before using
 *  2. use .setCountTo(int index) move focused point
 */

public class SlideCountView extends LinearLayout {
    private Context context;
    private ImageView[] entity;

    private int currentCount = 0;

    private SlideCountView.LayoutParams entityParams;
    private SlideCountView.LayoutParams focusedEntityParams;

    private int originalDiameter, focusedDiameter, margin;

    public SlideCountView(Context context) {
        super(context);
        init(context);
    }

    public SlideCountView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }


    public void initialize(int totalCount, int diameter, int margin) {
        entity = new ImageView[totalCount];

        this.margin=margin;
        originalDiameter=diameter;
        focusedDiameter=(int)(diameter*1.6);

        setGravity(Gravity.CENTER);

        entityParams = new SlideCountView.LayoutParams(originalDiameter, originalDiameter);
        entityParams.setMargins(margin / 2, 0, margin / 2, 0);
        entityParams.gravity = Gravity.CENTER_VERTICAL;

        focusedEntityParams = new SlideCountView.LayoutParams(focusedDiameter, focusedDiameter);

        for (int i = 0; i < totalCount; i++) {
            entity[i] = new ImageView(context);
            entity[i].setLayoutParams(entityParams);
            entity[i].setImageResource(R.drawable.slidecount_unselected);

            addView(entity[i]);
        }

        entity[0].setImageResource(R.drawable.slidecount_selected);
        entity[0].setLayoutParams(focusedEntityParams);

    }

    public void setCountTo(int index) {
        if (index < 0 || index > getTotalCount()) {
            return;
        }
        final int originalIndex=currentCount, newIndex=index;
        final LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams params2=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin / 2, 0, margin / 2, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(margin / 2, 0, margin / 2, 0);
        params.gravity = Gravity.CENTER_VERTICAL;

        ValueAnimator anim=ValueAnimator.ofInt(originalDiameter,focusedDiameter);
        anim.setDuration(60);
        anim.addUpdateListener(animation -> {
            int i=(int)animation.getAnimatedValue();
            params.width=focusedDiameter-(i-originalDiameter);
            params.height=focusedDiameter-(i-originalDiameter);
            params2.width=i;
            params2.height=i;

            entity[originalIndex].setLayoutParams(params);
            entity[newIndex].setLayoutParams(params2);
        });
        anim.start();

        entity[currentCount].setImageResource(R.drawable.slidecount_unselected);
//        entity[currentCount].setLayoutParams(entityParams);

        entity[index].setImageResource(R.drawable.slidecount_selected);
//        entity[index].setLayoutParams(focusedEntityParams);
        currentCount = index;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getTotalCount() {
        return entity.length;
    }
}
