package kotel.hanzan.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.R;
import kotel.hanzan.function.AssetsHelper;
import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.listener.TapBarItemClickListener;


/**
 * 1. use setItems() to initiate tapbar.
 * 2. use setTopColor() to change color of moving bar at the top of the tapbar.
 * 3. use setListener() to retrieve click event of item.
 */

public class TapBar extends RelativeLayout{

    private Context context;
    private LinearLayout layout;
    private View top;
    private LinearLayout container;

    private String[] title;
    private String[] image;

    private TextView[] titleTextView;
    private ImageView[] imageView;

    private TapBarItemClickListener listener;

    private int currentlyFocusedTapNumber = 0;

    private int mainColor;



    public TapBar(Context context) {
        super(context);
        init(context);
    }

    public TapBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context=context;
        layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.tapbar,null);
        top = layout.findViewById(R.id.tapbar_top);
        container = layout.findViewById(R.id.tapbar_container);

        setMainColor(ColorHelper.getColor(getResources(),R.color.mainColor_medium));

        addView(layout);
    }



    public void setListener(TapBarItemClickListener listener){
        this.listener = listener;
    }

    public void setMainColor(int color){
        mainColor = color;
        top.setBackgroundColor(mainColor);
    }

    public void setItems(String[] title, String[] image){
        if(title.length!=image.length){
            JLog.e("title and image arrays are not matching each other!");
            return;
        }
        container.removeAllViews();

        this.title = title;
        this.image = image;
        setTopBarWidth();

        titleTextView = new TextView[title.length];
        imageView = new ImageView[image.length];

        for(int i=0;i<title.length;i++){
            final int number = i;

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(StaticData.displayWidth/title.length, (int)getResources().getDimension(R.dimen.home_bottomLayoutHeight));
            int itemMargin = (int)getResources().getDimension(R.dimen.tapbar_itemMargin);
            LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.tapbar_item,null);
            ImageView itemImage = (ImageView) itemLayout.findViewById(R.id.tapbar_itemImage);
            TextView itemTitle = (TextView)itemLayout.findViewById(R.id.tapbar_itemTitle);

            imageView[i] = itemImage;
            titleTextView[i] = itemTitle;

            LinearLayout.LayoutParams itemImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            itemImageParams.weight = 1;
            imageView[i].setLayoutParams(itemImageParams);
            imageView[i].setPadding(itemMargin,itemMargin,itemMargin,itemMargin);

            imageView[i].setImageDrawable(AssetsHelper.loadTapBarImageDrawable(context,image[i]+"_off"));
            itemTitle.setText(title[i]);


            itemLayout.setPadding(itemMargin,itemMargin,itemMargin,itemMargin);
            itemLayout.setOnClickListener(view -> {
                if(currentlyFocusedTapNumber!=number) {
                    setFocusedItem(number);
                }
            });

            setItemToClicked(0);

            container.addView(itemLayout,itemParams);

        }
    }

    public int getCurrentlyFocusedTapNumber(){
        return currentlyFocusedTapNumber;
    }



    private void setTopBarWidth(){
        int itemCount = title.length;
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(StaticData.displayWidth/itemCount, ViewGroup.LayoutParams.MATCH_PARENT);
        top.setLayoutParams(params);
    }

    public void setFocusedItem(int number){
        if(listener!=null){
            listener.onClickStarted(title[number],number);
        }

        int itemCount = title.length;
        setItemToClicked(number);

        ObjectAnimator animator = ObjectAnimator.ofFloat(top,"X",top.getX(),number*StaticData.displayWidth/itemCount).setDuration(150);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                top.setX(number*StaticData.displayWidth/itemCount);
                if(listener!=null){
                    listener.onClick(title[number],number);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();

    }

    private void setItemToClicked(int number){

        imageView[currentlyFocusedTapNumber].setImageDrawable(AssetsHelper.loadTapBarImageDrawable(context,image[currentlyFocusedTapNumber]+"_off"));
        titleTextView[currentlyFocusedTapNumber].setTextColor(Color.BLACK);

        currentlyFocusedTapNumber = number;

        imageView[currentlyFocusedTapNumber].setImageDrawable(AssetsHelper.loadTapBarImageDrawable(context,image[currentlyFocusedTapNumber]+"_on"));
        titleTextView[currentlyFocusedTapNumber].setTextColor(mainColor);

    }

}
