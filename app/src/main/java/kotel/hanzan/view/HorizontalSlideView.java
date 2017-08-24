package kotel.hanzan.view;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.listener.SlideListener;

/**
 * 1. First, initialize the view with .setChildWidth(int width) **MATCH THIS WITH PARENT'S WIDTH**
 * 2. Second, use addViewToList(HorizontalSlideViewChild view) to add entities into SlideView
 * 3. HorizontalSlideViewChild can be clicked by implementing HorizontalSlideViewChildClickListener
 * 4. Use .slideTo(int index) to manually slide to intended index
 */

public class HorizontalSlideView extends HorizontalScrollView implements View.OnTouchListener {
    private Context context;
    private LinearLayout layout;

    private int currentIndex = 0;

    private int oldX = 0, newX = 0;
    private float clickOldX, clickOldY, clickNewX, clickNewY;

    private SlideListener slideListener;

    private int width;

    private boolean alreadyClickedSomething=false;

    public HorizontalSlideView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        width = StaticData.displayWidth;

        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);

        HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);
        addView(layout);

        setOnTouchListener(this);
    }

    public void setChildWidth(int width) {
        this.width = width;
    }

    public void addViewToList(HorizontalSlideViewChild view) {
        HorizontalSlideView.LayoutParams params = new HorizontalSlideView.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        layout.addView(view);
    }

    public void addViewToList(HorizontalSlideViewChild view, int index) {
        HorizontalSlideView.LayoutParams params = new HorizontalSlideView.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        layout.addView(view, index);
    }

    public void removeViewFromList(int index) {
        layout.removeViewAt(index);
        if (currentIndex >= layout.getChildCount()) {
            slideTo(layout.getChildCount() - 1);
        }
    }

    public void removeViewFromList(HorizontalSlideViewChild view) {
        layout.removeView(view);
        if (currentIndex >= layout.getChildCount()) {
            slideTo(layout.getChildCount() - 1);
        }
    }


    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getListChildCount() {
        return layout.getChildCount();
    }


    public void slideTo(int index) {
        if(index<0||index>=layout.getChildCount()){
            return;
        }
        functionBeforeSliding();
        currentIndex = index;
        smoothScrollTo(width * index, 0);
        functionAfterSliding();
    }

    private void functionBeforeSliding(){
        if (slideListener!=null){
            slideListener.beforeSlide();
        }
    }

    private void functionAfterSliding(){
        if (slideListener!=null){
            slideListener.afterSlide();
        }
    }

    private void functionWhileSliding(){
        if (slideListener!=null){
            slideListener.whileSlide();
        }
    }

    public void setOnSlideListener(SlideListener slideListener){
        this.slideListener=slideListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            oldX = getScrollX();
            clickOldX = event.getX();
            clickOldY = event.getY();
            return true;
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            functionWhileSliding();
        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            newX = getScrollX();
            if (Math.abs(oldX - newX) > 50) {
                //MOVE TO NEW SLIDE
                if (newX < oldX && currentIndex > 0) {
                    //MOVING LEFT
                    slideTo(currentIndex - 1);
                } else if (newX > oldX && currentIndex < layout.getChildCount()) {
                    //MOVING RIGHT
                    slideTo(currentIndex + 1);
                }
                return true;
            } else {
                clickNewX = event.getX();
                clickNewY = event.getY();
                //CLICK
                if (Math.abs(clickOldX - clickNewX) <= 50 && Math.abs(clickOldY - clickNewY) <= 50) {
                    HorizontalSlideViewChild view = (HorizontalSlideViewChild) layout.getChildAt(currentIndex);
                    ValueAnimator anim=ValueAnimator.ofInt(0,20,0);
                    anim.setDuration(100);
                    anim.addUpdateListener(animation -> {
                        view.setPadding((int)animation.getAnimatedValue(),(int)animation.getAnimatedValue(),(int)animation.getAnimatedValue(),(int)animation.getAnimatedValue());
                    });
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if(!alreadyClickedSomething) {
                                alreadyClickedSomething=true;
                                view.callClickEvent();
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    alreadyClickedSomething = false;
                                }).start();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    anim.start();
                }

                //RETURN TO ORIGINAL SLIDE
                slideTo(currentIndex);
                return true;
            }
        }

        return false;
    }

}