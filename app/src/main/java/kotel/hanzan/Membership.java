package kotel.hanzan;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.CalendarHelper;
import kotel.hanzan.function.NumericHelper;
import kotel.hanzan.function.ServerConnectionHelper;

public class Membership extends AppCompatActivity {
    private ImageView back;
    private TextView expireDate;
    private RecyclerView recyclerView;
    private MembershipAdapter adapter = new MembershipAdapter();
    private ArrayList<MembershipTicketInfo> ticketArray = new ArrayList<>();

    private int startYYYY, startMM, startDD;

    private class MembershipTicketInfo{
        String name;

        int originalPrice;
        int discountPrice;
        boolean isNowDiscounted;

        int durationMonths;

        public MembershipTicketInfo(String name, int durationMonths, int originalPrice, int discountPrice) {
            this.name = name;
            this.originalPrice = originalPrice;
            this.discountPrice = discountPrice;
            this.durationMonths = durationMonths;
            if(discountPrice != 0){
                isNowDiscounted = true;
            }else{
                isNowDiscounted = false;
            }
        }
    }

    class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView saleIcon,title,currentPrice,strikeThroughedPrice,duration;
            public ViewHolder(View itemView) {
                super(itemView);
                saleIcon = (TextView)itemView.findViewById(R.id.membership_ticket_saleIcon);
                title = (TextView)itemView.findViewById(R.id.membership_ticket_name);
                currentPrice = (TextView)itemView.findViewById(R.id.membership_ticket_currentPrice);
                strikeThroughedPrice = (TextView)itemView.findViewById(R.id.membership_ticket_strikeThroughedPrice);
                duration = (TextView)itemView.findViewById(R.id.membership_ticket_date);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.membership_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MembershipTicketInfo ticketInfo = ticketArray.get(position);
            if(ticketInfo.isNowDiscounted){
                holder.saleIcon.setVisibility(View.VISIBLE);
                holder.saleIcon.setText(Integer.toString(100 - (int)(100*(float)ticketInfo.discountPrice/ticketInfo.originalPrice))+"%\nSALE");
                holder.strikeThroughedPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.originalPrice))+getString(R.string.won) );
                holder.currentPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.discountPrice))+getString(R.string.won) );
            }else{
                holder.saleIcon.setVisibility(View.INVISIBLE);
                holder.strikeThroughedPrice.setText("");
                holder.currentPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.originalPrice))+getString(R.string.won) );
            }

            holder.title.setText(ticketInfo.name);

            int[] MembershipExpire = CalendarHelper.getDateAfterMonths(ticketInfo.durationMonths,new int[]{startYYYY,startMM,startDD});

            holder.duration.setText("~"+Integer.toString(MembershipExpire[0])+"."+Integer.toString(MembershipExpire[1])+"."+
                Integer.toString(MembershipExpire[2]));
        }

        @Override
        public int getItemCount() {
            return ticketArray.size();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        back = (ImageView)findViewById(R.id.membership_back);
        expireDate = (TextView)findViewById(R.id.membership_expireDate);
        recyclerView = (RecyclerView) findViewById(R.id.membership_recycler);

        if(StaticData.currentUser.expireYYYY==0){
            //if user is not a member of Hanzan
            int[] startDate = CalendarHelper.getCurrentDate();
            startYYYY = startDate[0];
            startMM = startDate[1];
            startDD = startDate[2];
            expireDate.setText(getString(R.string.notMemberYet));
        }else{
            //if user is a member of Hanzan
            startYYYY = StaticData.currentUser.expireYYYY;
            startMM = StaticData.currentUser.expireMM;
            startDD = StaticData.currentUser.expireDD;
            expireDate.setText(Integer.toString(startYYYY)+"."+Integer.toString(startMM)+"."+Integer.toString(startDD));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        retrieveMembershipTicketInfo();

        back.setOnClickListener(view -> finish());
    }

    private void retrieveMembershipTicketInfo(){
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving membership ticket info","ticketinfo",map);

            int i=0;
            while (true) {
                String num = Integer.toString(i++);
                if (map.get("durationmonths_" + num)==null){
                    break;
                }
                int durationMonth = Integer.parseInt(map.get("durationmonths_" + num));
                String ticketName = map.get("name_ticket_" + num);
                int originalPrice = Integer.parseInt(map.get("originalprice_" + num));
                int discountPrice = Integer.parseInt(map.get("discountprice_" + num));

                ticketArray.add(new MembershipTicketInfo(ticketName, durationMonth,originalPrice,discountPrice));
            }
            new Handler(getMainLooper()).post(()->{
                adapter.notifyDataSetChanged();

            });
        }).start();
    }
}
