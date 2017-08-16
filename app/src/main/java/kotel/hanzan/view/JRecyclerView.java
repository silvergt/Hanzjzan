package kotel.hanzan.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.wang.avi.AVLoadingIndicatorView;

import kotel.hanzan.R;
import kotel.hanzan.listener.JRecyclerViewListener;

public class JRecyclerView extends TwinklingRefreshLayout {
    private RecyclerView recyclerView;

    private JRecyclerViewListener listener;

    private boolean isLoadingMore=false,isRefreshing=false;

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
                image.setIndicatorColor(context.getResources().getColor(R.color.mainColor_medium));
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

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                try {
                    int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

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

        addView(recyclerView);
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
}