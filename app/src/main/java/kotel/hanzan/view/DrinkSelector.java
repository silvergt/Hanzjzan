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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kotel.hanzan.Data.DrinkInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.R;
import kotel.hanzan.function.AssetsHelper;
import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.function.JLog;
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
    ArrayList<String> drinkType;
    ArrayList[] drinkList;
    ArrayList[] drinkImageList;
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
            CircleImageView image;
            public itemViewHolder(View itemView) {
                super(itemView);
                name= itemView.findViewById(R.id.drinkSelector_drinkList_text);
                image= itemView.findViewById(R.id.drinkSelector_drinkList_image);
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
            holder.name.setText((String)drinkList[currentSelectedType].get(position));
            if(drinkImageList[currentSelectedType].get(position)==null ||
                    drinkImageList[currentSelectedType].get(position).equals("NULL")){
                Picasso.with(context).load(R.drawable.drinkselector_default).into(holder.image);
            }else {
                Picasso.with(context).load((String) drinkImageList[currentSelectedType].get(position)).placeholder(R.drawable.drinkselector_default).into(holder.image);
            }
            holder.itemView.setOnClickListener(view -> {
                listItemSelected(currentSelectedType,position);
            });
        }

        @Override
        public int getItemCount() {
            int length =0;
            try{
                length = drinkList[currentSelectedType].size();
            }catch (Exception e){}
            return length;
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

    private void init(Context context){
        this.context = context;
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.drinkselector,null);
        drinkListView = layout.findViewById(R.id.drinkSelector_itemList);
        drinkTypeViewScroll = layout.findViewById(R.id.drinkSelector_typeScroll);
        drinkTypeView = layout.findViewById(R.id.drinkSelector_type);
        drinkListViewLayout = layout.findViewById(R.id.drinkSelector_itemListLayout);
        drinkTypeViewLayout = layout.findViewById(R.id.drinkSelector_typeLayout);
        leftOfScroll = layout.findViewById(R.id.drinkSelector_leftOfScroll);
        rightOfScroll = layout.findViewById(R.id.drinkSelector_rightOfScroll);

        addView(layout);

        drinkType = new ArrayList<>();
        drinkList = new ArrayList[1];
        drinkList[0] = new ArrayList();
        drinkImageList = new ArrayList[1];
        drinkImageList[0] = new ArrayList();

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

    public void setDrinkList(ArrayList<DrinkInfo> array){
        JLog.v("Setting DL");

        if(array.size()==0){
            return;
        }

        drinkType = new ArrayList<>();
        drinkType.add(array.get(0).drinkType);
        for(int i=0;i<array.size();i++){
            for(int j=0;j<drinkType.size();j++){
                if(drinkType.get(j).equals(array.get(i).drinkType)){
                    break;
                }else if(j==drinkType.size()-1){
                    drinkType.add(array.get(i).drinkType);
                }
            }
        }

        drinkList = new ArrayList[drinkType.size()];
        drinkImageList = new ArrayList[drinkType.size()];
        for(int i=0;i<drinkList.length;i++){
            drinkList[i] = new ArrayList<>();
            drinkImageList[i] = new ArrayList<>();
        }
        for(int i=0;i<array.size();i++){
            for(int j=0; j<drinkType.size();j++){
                if(drinkType.get(j).equals(array.get(i).drinkType)){
                    drinkList[j].add(array.get(i).drinkName);
                    drinkImageList[j].add(array.get(i).drinkImageAddress);
                }
            }
        }


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

        if(drinkType.size()==0){
            return;
        }

        for(int i=0;i<drinkType.size();i++){
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

        drinkListViewLayout.setBackgroundColor(ColorHelper.getColor(getResources(),R.color.drinkSelector_Blur));
        leftOfScroll.setBackgroundColor(ColorHelper.getColor(getResources(),R.color.drinkSelector_Blur));
        rightOfScroll.setBackgroundColor(ColorHelper.getColor(getResources(),R.color.drinkSelector_Blur));
        for(int i=0;i<drinkType.size();i++){
            if(i!=typeNum){
                shadows[i].setBackgroundColor(ColorHelper.getColor(getResources(),R.color.drinkSelector_Blur));
            }else{
                drinkTypeLayout[i].setBackgroundColor(ColorHelper.getColor(getResources(),R.color.drinkSelector_Blur));
            }
        }

        ValueAnimator anim=ValueAnimator.ofInt(0, StaticData.displayHeight-drinkTypeViewScroll.getHeight()).setDuration(500);
        anim.addUpdateListener(valueAnimator -> {
            int i=(int)valueAnimator.getAnimatedValue();
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(itemHeight,i);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            drinkListView.setLayoutParams(params);
            drinkListView.setX(drinkTypeView.getChildAt(typeNum).getX()-drinkTypeViewScroll.getScrollX()+drinkTypeViewScroll.getX());
        });
        anim.start();

        if(listener!=null){
            listener.typeSelected(drinkType.get(typeNum));
        }
    }




    private void setItemHeight(int height){
        this.itemHeight=height;
        LinearLayout.LayoutParams typeParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);
        drinkTypeViewLayout.setLayoutParams(typeParams);
        RelativeLayout.LayoutParams listParams=new RelativeLayout.LayoutParams(height,0);
        drinkListView.setLayoutParams(listParams);


        setDrinkTypeView();
    }

    private void setDrinkTypeView() {
        shadows = new View[drinkType.size()];
        drinkTypeLayout = new RelativeLayout[drinkType.size()];

        for (int i = 0; i < drinkType.size(); i++) {
            final int num = i;
            ScrollView.LayoutParams params = new ScrollView.LayoutParams(itemHeight, itemHeight);
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.drinkselector_drinktype, null);
            item.setLayoutParams(params);

            ImageView drinkTypeImage = item.findViewById(R.id.drinkSelector_drinkType_image);
            TextView drinkTypeName = item.findViewById(R.id.drinkSelector_drinkType_text);

            drinkTypeImage.setImageDrawable(AssetsHelper.loadDrinkImage(context,drinkType.get(num)));
//            AssetsHelper.loadDrinkImage(context,drinkType.get(num)).into(drinkTypeImage);
            drinkTypeName.setText(DrinkInfo.getDrinkName(context,drinkType.get(num)));

            drinkTypeLayout[i] = item.findViewById(R.id.drinkSelector_drinkType_layout);
            shadows[i] = item.findViewById(R.id.drinkSelector_drinkType_shadow);

            item.setOnClickListener(view -> {
                if (drinkListViewIsVisible && num == currentSelectedType) {
                    closeDrinkListView();
                } else {
                    if (drinkListViewIsVisible) {
                        closeDrinkListView();
                        typeSelected(num);
                    } else {
                        typeSelected(num);
                    }
                }

            });
            drinkTypeView.addView(item);
        }
    }

    private void listItemSelected(int typeNum, int itemNum){
        if(listener!=null) {
            DrinkInfo drinkInfo = new DrinkInfo(drinkType.get(typeNum),(String)drinkList[typeNum].get(itemNum),(String)drinkImageList[typeNum].get(itemNum));
            listener.itemSelected(drinkInfo);
        }
        JLog.v((String)drinkList[typeNum].get(itemNum));
        closeDrinkListView();
    }
}
