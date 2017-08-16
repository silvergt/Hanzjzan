package kotel.hanzan.listener;


import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;

public interface JRecyclerViewListener {
    void onRefresh(TwinklingRefreshLayout refreshLayout);

    void onLoadMore();
}
