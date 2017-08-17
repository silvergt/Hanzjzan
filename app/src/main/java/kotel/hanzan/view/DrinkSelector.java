package kotel.hanzan.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import kotel.hanzan.R;
import kotel.hanzan.function.JLog;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.listener.DrinkSelectorListener;

/**
 * 1.Use setDrinkList()
 * 2.Use setListener()
 *
 * 3.TypeSelected() method opens upper list menu
 * 4.closeDrinkListView() closes upper list menu if able
 */


public class DrinkSelector extends RelativeLayout{
    private Context context;
    String[] drinkType;
    String[][] drinkList;
    private DrinkSelectorListener listener;
    private int itemHeight = 50;

    private RelativeLayout layout,drinkListViewLayout;
    private LinearLayout drinkTypeViewLayout;
    private RecyclerView drinkListView;
    private HorizontalScrollView drinkTypeViewScroll;
    private LinearLayout drinkTypeView;
    private View[] shadows;
    private RelativeLayout[] drinkTypeLayout;
    private View leftOfScroll,rightOfScroll;

    private boolean drinkListViewIsVisible=false;
    private int currentSelectedType=0;

    class drinkListAdapter extends RecyclerView.Adapter<drinkListAdapter.itemViewHolder>{
        class itemViewHolder extends RecyclerView.ViewHolder{
            TextView name;
            ImageView image;
            public itemViewHolder(View itemView) {
                super(itemView);
                name=(TextView) itemView.findViewById(R.id.drinkSelector_drinkList_text);
                image=(ImageView)itemView.findViewById(R.id.drinkSelector_drinkList_image);
            }
        }
        @Override
        public drinkListAdapter.itemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drinkselector_drinklist,parent,false);
            RecyclerView.LayoutParams params=new RecyclerView.LayoutParams(itemHeight,itemHeight);
            view.setLayoutParams(params);
            return new itemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(drinkListAdapter.itemViewHolder holder, int position) {
            holder.name.setText(drinkList[currentSelectedType][position]);
            holder.itemView.setOnClickListener(view -> {
                listItemSelected(currentSelectedType,position);
            });
        }

        @Override
        public int getItemCount() {
            return drinkList[currentSelectedType].length;
        }
    }


    public DrinkSelector(Context context) {
        super(context);
        init(context);
    }

    public DrinkSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrinkSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DrinkSelector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.drinkselector,null);
        drinkListView = (RecyclerView) layout.findViewById(R.id.drinkSelector_itemList);
        drinkTypeViewScroll = (HorizontalScrollView) layout.findViewById(R.id.drinkSelector_typeScroll);
        drinkTypeView = (LinearLayout) layout.findViewById(R.id.drinkSelector_type);
        drinkListViewLayout = (RelativeLayout) layout.findViewById(R.id.drinkSelector_itemListLayout);
        drinkTypeViewLayout = (LinearLayout) layout.findViewById(R.id.drinkSelector_typeLayout);
        leftOfScroll = layout.findViewById(R.id.drinkSelector_leftOfScroll);
        rightOfScroll = layout.findViewById(R.id.drinkSelector_rightOfScroll);

        addView(layout);


        drinkListViewLayout.setOnClickListener(view -> {
            closeDrinkListView();
        });
        drinkListViewLayout.setVisibility(INVISIBLE);

        drinkTypeViewLayout.setOnClickListener(view -> closeDrinkListView());

        drinkTypeViewScroll.setOnTouchListener((view, motionEvent) -> {
            if(drinkListViewIsVisible){
                closeDrinkListView();
            }
            return false;
        });
    }

    public boolean isDrinkListViewIsVisible(){
        return drinkListViewIsVisible;
    }

    public void setDrinkList(String[] drinkType, String[][] drinkList){
        this.drinkType=drinkType;
        this.drinkList=drinkList;

        drinkListView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,true));
        drinkListView.setAdapter(new drinkListAdapter());

        setItemHeight((int)getResources().getDimension(R.dimen.drinkSelector_height));
    }

    public void setListener(DrinkSelectorListener listener){
        this.listener = listener;
    }

    public void closeDrinkListView(){
        drinkListViewIsVisible=false;

        drinkListViewLayout.setBackgroundColor(0);
        leftOfScroll.setBackgroundColor(0);
        rightOfScroll.setBackgroundColor(0);
        for(int i=0;i<drinkType.length;i++){
            shadows[i].setBackgroundColor(0);
            drinkTypeLayout[i].setBackgroundColor(0);
        }

        ValueAnimator anim=ValueAnimator.ofInt(drinkListView.getHeight(), 0).setDuration(150);
        anim.addUpdateListener(valueAnimator -> {
            int i=(int)valueAnimator.getAnimatedValue();
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(itemHeight,i);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            drinkListView.setLayoutParams(params);
            drinkListView.setX(drinkTypeView.getChildAt(currentSelectedType).getX()-drinkTypeViewScroll.getScrollX()+drinkTypeViewScroll.getX());
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(!drinkListViewIsVisible) {
                    drinkListViewLayout.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
    }

    public void typeSelected(int typeNum){
        drinkListViewIsVisible=true;
        drinkListViewLayout.setVisibility(VISIBLE);

        currentSelectedType=typeNum;
        drinkListView.getAdapter().notifyDataSetChanged();

        drinkListViewLayout.setBackgroundColor(getResources().getColor(R.color.drinkSelector_Blur));
        leftOfScroll.setBackgroundColor(getResources().getColor(R.color.drinkSelector_Blur));
        rightOfScroll.setBackgroundColor(getResources().getColor(R.color.drinkSelector_Blur));
        for(int i=0;i<drinkType.length;i++){
            if(i!=typeNum){
                shadows[i].setBackgroundColor(getResources().getColor(R.color.drinkSelector_Blur));
            }else{
                drinkTypeLayout[i].setBackgroundColor(getResources().getColor(R.color.drinkSelector_Blur));
            }
        }

        ValueAnimator anim=ValueAnimator.ofInt(0, StaticData.displayHeight-drinkTypeViewScroll.getHeight());
        anim.addUpdateListener(valueAnimator -> {
            int i=(int)valueAnimator.getAnimatedValue();
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(itemHeight,i);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            drinkListView.setLayoutParams(params);
            drinkListView.setX(drinkTypeView.getChildAt(typeNum).getX()-drinkTypeViewScroll.getScrollX()+drinkTypeViewScroll.getX());
        });
        anim.start();
    }




    private void setItemHeight(int height){
        this.itemHeight=height;
        LinearLayout.LayoutParams typeParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);
        drinkTypeViewLayout.setLayoutParams(typeParams);
        RelativeLayout.LayoutParams listParams=new RelativeLayout.LayoutParams(height,0);
        drinkListView.setLayoutParams(listParams);
        setDrinkTypeView();
    }

    private void setDrinkTypeView(){
        shadows = new View[drinkType.length];
        drinkTypeLayout = new RelativeLayout[drinkType.length];

        for(int i=0;i<drinkType.length;i++){
            final int num=i;
            ScrollView.LayoutParams params=new ScrollView.LayoutParams(itemHeight,itemHeight);
            RelativeLayout item=(RelativeLayout)LayoutInflater.from(context).inflate(R.layout.drinkselector_drinktype,null);
            item.setLayoutParams(params);


            TextView textview=(TextView) item.findViewById(R.id.drinkSelector_drinkType_text);
            textview.setText(drinkType[num]);

            drinkTypeLayout[i] = item.findViewById(R.id.drinkSelector_drinkType_layout);
            shadows[i] = item.findViewById(R.id.drinkSelector_drinkType_shadow);

            item.setOnClickListener(view -> {
                if(drinkListViewIsVisible && num==currentSelectedType) {
                    closeDrinkListView();
                }else{
                    if(drinkListViewIsVisible) {
                        closeDrinkListView();
                        typeSelected(num);
                    }else{
                        typeSelected(num);
                    }
                }

            });
            drinkTypeView.addView(item);
        }
    }

    private void listItemSelected(int typeNum, int itemNum){
        if(listener!=null) {
            listener.itemSelected(drinkList[typeNum][itemNum]);
        }
        JLog.v(drinkList[typeNum][itemNum]);
        closeDrinkListView();
    }
}
