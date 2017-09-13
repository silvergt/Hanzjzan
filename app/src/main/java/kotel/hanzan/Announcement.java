package kotel.hanzan;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.JRecyclerViewListener;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.JRecyclerView;

public class Announcement extends JActivity {
    private ImageView back;
    private JRecyclerView recyclerView;

    private AnnouncementRecyclerViewAdapter adapter = new AnnouncementRecyclerViewAdapter();
    private ArrayList<AnnouncementInfo> infoArray = new ArrayList<>();


    private class AnnouncementInfo{
        String title,entity;

        public AnnouncementInfo(String title, String entity) {
            this.title = title;
            this.entity = entity;
        }
    }

    private class AnnouncementRecyclerViewAdapter extends RecyclerView.Adapter<AnnouncementRecyclerViewAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView)itemView.findViewById(R.id.announcement_itemText);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pubitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AnnouncementInfo info = infoArray.get(position);

            holder.titleTextView.setText(info.title);
        }

        @Override
        public int getItemCount() {
            return infoArray.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        back = (ImageView)findViewById(R.id.announcement_back);
        recyclerView = (JRecyclerView)findViewById(R.id.announcement_recycler);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnJRecyclerViewListener(new JRecyclerViewListener() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                retrieveAnnouncements(true);
            }

            @Override
            public void onLoadMore() {
                retrieveAnnouncements(false);
            }
        });



        back.setOnClickListener(view -> finish());
    }

    private synchronized void retrieveAnnouncements(boolean clearArray) {
        if(clearArray) {
            infoArray.clear();
        }
        if(StaticData.myLatestLocation == null )StaticData.myLatestLocation = StaticData.defaultLocation;
        new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();

            map.put("at", Integer.toString(infoArray.size()));
            map = ServerConnectionHelper.connect("retrieve announcements", "announcement", map);

            int i = 0;
            while (true) {
                String num = Integer.toString(i++);
                if(map.get("title_" + num)==null){
                    break;
                }
                String title = map.get("title_" + num);
                String entity = map.get("entity_" + num);

                infoArray.add(new AnnouncementInfo(title,entity));
            }

            String dataleft = map.get("datalefts");

            new Handler(getMainLooper()).post(() -> {
                recyclerView.finishRefreshing();
                adapter.notifyDataSetChanged();
                if(dataleft!=null&&dataleft.equals("TRUE")){
                    recyclerView.finishLoadmore();
                }
            });
        }).start();

    }

}
