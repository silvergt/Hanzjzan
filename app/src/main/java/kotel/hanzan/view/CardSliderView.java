package kotel.hanzan.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.ArrayList;

import kotel.hanzan.R;
import kotel.hanzan.data.StaticData;

/**
 * 1. Create class
 * 2. setImageData()
 * 3. setSlideListener()
 */

public class CardSliderView extends RelativeLayout{
    private Context context;
    DiscreteScrollView discreteScrollView;

    private int contentWidth=0, contentSpace=0;

    private ArrayList<String> imageList = new ArrayList<>();

    private class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ViewHolder> {
        final private int VIEWTYPE_HEADER=1;
        final private int VIEWTYPE_NORMAL=2;
        final private int VIEWTYPE_FOOTER=3;

        class ViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                /** findViewById */
                image = itemView.findViewById(R.id.cardSliderView_image);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0){
                return VIEWTYPE_HEADER;
            }else if(position == imageList.size() + 1){
                return VIEWTYPE_FOOTER;
            }else{
                return VIEWTYPE_NORMAL;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            /** inflate view */
            View view = LayoutInflater.from(context).inflate(R.layout.cardsliderview_image, null);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(contentWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            if(viewType == VIEWTYPE_NORMAL) {
                params.setMargins(contentSpace, 0, contentSpace, 0);
            }else if(viewType == VIEWTYPE_HEADER){
                params.setMargins(contentSpace*2, 0, contentSpace, 0);
            }else if(viewType == VIEWTYPE_FOOTER){
                params.setMargins(contentSpace, 0, contentSpace*2, 0);
            }
            view.setLayoutParams(params);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(holder.image!=null) {
                Picasso.with(context).load(imageList.get(position)).placeholder(R.drawable.loading_store).into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }
    }

    public CardSliderView(Context context) {
        super(context);
        init(context);
    }

    public CardSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context){
        this.context = context;

        contentWidth = (int)((float)StaticData.displayWidth*53/60);
        contentSpace = (int)((float)StaticData.displayWidth*1/80);

        discreteScrollView = new DiscreteScrollView(context);

        discreteScrollView.setAdapter(new ImageViewAdapter());

        discreteScrollView.setOverScrollEnabled(false);
        discreteScrollView.setItemTransitionTimeMillis(200);

        RelativeLayout.LayoutParams scrollViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        discreteScrollView.setLayoutParams(scrollViewParams);

        addView(discreteScrollView);
    }

    public void setImageList(ArrayList<String> arrayList){
        imageList = arrayList;
    }

}
