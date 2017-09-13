package kotel.hanzan.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.wang.avi.AVLoadingIndicatorView;

import kotel.hanzan.R;
import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.listener.JRecyclerViewListener;

import static kotel.hanzan.R.color.mainColor_medium;

public class JRecyclerView extends TwinklingRefreshLayout {
    private RecyclerView recyclerView;

    private JRecyclerViewListener listener;

    private boolean isLoadingMore=false,isRefreshing=false;

    private ImageView toTopButton;

    public JRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public JRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        recyclerView = new RecyclerView(context);
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);


        setHeaderView(new IHeaderView() {
            @Override
            public View getView() {
                AVLoadingIndicatorView image = new AVLoadingIndicatorView(context);
                image.setIndicator("BallClipRotateIndicator");
                image.setIndicatorColor(ColorHelper.getColor(getResources(),R.color.mainColor_medium));
                return image;
            }

            @Override
            public void onPullingDown(float fraction, float maxHeadHeight, float headHeight) {

            }

            @Override
            public void onPullReleasing(float fraction, float maxHeadHeight, float headHeight) {

            }

            @Override
            public void startAnim(float maxHeadHeight, float headHeight) {

            }

            @Override
            public void onFinish(OnAnimEndListener animEndListener) {
                animEndListener.onAnimEnd();
            }

            @Override
            public void reset() {

            }
        });

        setHeaderHeight(40);

        setEnableLoadmore(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                try {

                    int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();


                    if(pastVisibleItems >= 3){
                        toTopButton.setVisibility(VISIBLE);
                    }else{
                        toTopButton.setVisibility(INVISIBLE);
                    }


                    if ( (visibleItemCount + pastVisibleItems) >= totalItemCount)
                    {
                        if(!isLoadingMore && listener!=null){
                            listener.onLoadMore();
                            isLoadingMore=true;
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                (int)getResources().getDimension(R.dimen.JRecyclerView_buttonWidth),
                (int)getResources().getDimension(R.dimen.JRecyclerView_buttonWidth) );
        imageParams.addRule(ALIGN_PARENT_BOTTOM);
        imageParams.addRule(ALIGN_PARENT_RIGHT);
        imageParams.setMargins(0,0,5,(int)getResources().getDimension(R.dimen.leftRightMargin));
        toTopButton = new ImageView(context);
        toTopButton.setLayoutParams(imageParams);
        toTopButton.setImageResource(R.drawable.top);
        toTopButton.setVisibility(INVISIBLE);

        toTopButton.setOnClickListener(view -> {
            recyclerView.smoothScrollToPosition(0);
        });


        addView(recyclerView);
        addView(toTopButton);
    }

    public void removeToTopButton(){
        removeView(toTopButton);
    }






    @Override
    public void finishLoadmore() {
        super.finishLoadmore();
        isLoadingMore=false;
    }

    @Override
    public void onRefresh(TwinklingRefreshLayout refreshLayout) {
        super.onRefresh(refreshLayout);
        isRefreshing=true;
        if(listener!=null){
            listener.onRefresh(refreshLayout);
        }
    }

    @Override
    public void onFinishRefresh() {
        super.onFinishRefresh();
        isRefreshing=false;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        recyclerView.setLayoutManager(layout);
    }

    public void setOnJRecyclerViewListener(JRecyclerViewListener listener){
        this.listener = listener;
    }

    @Deprecated
    @Override
    public void setOnRefreshListener(RefreshListenerAdapter refreshListener) {
        super.setOnRefreshListener(refreshListener);
    }

    @Deprecated
    @Override
    public void setEnableLoadmore(boolean enableLoadmore1) {
        super.setEnableLoadmore(enableLoadmore1);
    }
}