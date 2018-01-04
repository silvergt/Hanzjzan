package kotel.hanzan.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.data.StaticData;
import kotel.hanzan.R;
import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.LocationFilterListener;

import static com.facebook.FacebookSdk.getApplicationContext;

public class LocationFilterView extends RelativeLayout{
    private Context context;
    private LocationFilterListener listener;

    private RelativeLayout layout;
    private LinearLayout upperScrollContainer;
    private RecyclerView itemRecycler;
    private TextView selectLocation,searchAroundMe;

    private TextView lastClickedUpperBarItemText;
    private View lastClickedUpperBarItemLowerBar;

    private ArrayList<Location> locationList= new ArrayList<>();
    private FilterRecyclerViewAdapter adapter = new FilterRecyclerViewAdapter();

    private int currentFocusedLocationListNumber = 0;
    private String currentClickedLocationName = "";


    private class Location{
        String locationName;
        ArrayList<String> specificLocations = new ArrayList<>();
        ArrayList<Boolean> specificLocationsIsActivated = new ArrayList<>();

        public Location(String locationName, String specificLocations, boolean specificLocationsIsActivated) {
            this.locationName = locationName;
            this.specificLocations.add(specificLocations);
            this.specificLocationsIsActivated.add(specificLocationsIsActivated);
        }
    }

    private class FilterRecyclerViewAdapter extends RecyclerView.Adapter<FilterRecyclerViewAdapter.ViewHolder> {
        int selectedNumber = -1;
        TextView lastClickedTextView;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView comingSoon,locationName;

            public ViewHolder(View itemView) {
                super(itemView);
                comingSoon = itemView.findViewById(R.id.locationFilter_comingSoon);
                locationName = itemView.findViewById(R.id.locationFilter_locationName);
            }
        }

        public void clickedNewLocationList(int num){
            currentFocusedLocationListNumber = num;
            selectedNumber = -1;

            adapter.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.locationfilter_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(locationList.size()==0)return;
            String locationName = locationList.get(currentFocusedLocationListNumber).specificLocations.get(position);
            boolean isActive = locationList.get(currentFocusedLocationListNumber).specificLocationsIsActivated.get(position);

            holder.locationName.setText(locationName);
            holder.comingSoon.setVisibility(isActive ? INVISIBLE : VISIBLE);
            if(isActive){
                holder.locationName.setTextColor(ColorHelper.getColor(context.getResources(),R.color.black));
                holder.locationName.setBackgroundResource(R.drawable.roundbox_black_hollow);
            }else{
                holder.locationName.setTextColor(ColorHelper.getColor(context.getResources(),R.color.darkGray));
                holder.locationName.setBackgroundResource(R.drawable.roundbox_gray_hollow);
            }

            holder.itemView.setOnClickListener(view -> {
                itemClicked(locationName);
                if(isActive){
                    currentClickedLocationName = locationName;
                    selectLocation.setBackgroundResource(R.drawable.roundbox_maincolor);

                    if(lastClickedTextView!=null){
                        lastClickedTextView.setTextColor(ColorHelper.getColor(context.getResources(),R.color.black));
                        lastClickedTextView.setBackgroundResource(R.drawable.roundbox_black_hollow);
                    }
                    holder.locationName.setTextColor(ColorHelper.getColor(context.getResources(),R.color.white));
                    holder.locationName.setBackgroundResource(R.drawable.roundbox_maincolor_nothollow);
                    lastClickedTextView = holder.locationName;
                    selectedNumber = position;
                }
                if(listener!=null){
                    listener.onItemClick(locationName,isActive);
                }
            });
        }

        @Override
        public int getItemCount() {
            if(locationList.size()==0)return 0;
            return locationList.get(currentFocusedLocationListNumber).specificLocations.size();
        }
    }



    public LocationFilterView(Context context) {
        super(context);
        init(context);
    }

    public LocationFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context = context;

        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.home_locationfilter,null);

        upperScrollContainer = layout.findViewById(R.id.locationFilter_upperBarContainer);
        itemRecycler = layout.findViewById(R.id.locationFilter_locationContainer);
        selectLocation = layout.findViewById(R.id.locationFilter_selectLocation);
        searchAroundMe = layout.findViewById(R.id.locationFilter_aroundMe);


        retrieveLocationDataFromServer();

        selectLocation.setOnClickListener(view -> {
            if(!currentClickedLocationName.equals("")){
                if (listener != null) {
                    listener.onSelectLocationClick(currentClickedLocationName);
                }
            }
        });

        searchAroundMe.setOnClickListener(view -> {
            if(listener!=null){
                listener.onSearchAroundMeClick();
            }
        });

        itemRecycler.setLayoutManager(new GridLayoutManager(context,3,GridLayoutManager.VERTICAL,false));
        itemRecycler.setAdapter(adapter);

        addView(layout);

        clickedNewLocationList(0);
    }


    public void retrieveLocationDataFromServer(){
        locationList.clear();


        new Thread(()-> {

            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving filter location data","filterlist",map);
            int t=0;
            while(true){
                String num = Integer.toString(t++);
                if(map.get("name_district_"+num)==null){
                    break;
                }
                String location = map.get("name_district_"+num);
                String city = map.get("city_district_"+num);
                boolean status = false;
                if(map.get("status_district_"+num).equals("TRUE")){
                    status = true;
                }

                for(int i=0;i<locationList.size();i++){
                    if(locationList.get(i).locationName.equals(city)){
                        locationList.get(i).specificLocations.add(location);
                        locationList.get(i).specificLocationsIsActivated.add(status);
                        break;
                    }else{
                        if(i == locationList.size()-1){
                            locationList.add(new Location(city,location,status));
                            break;
                        }
                    }
                }

                if(locationList.size()==0){
                    locationList.add(new Location(city,location,status));
                }

            }


            new Handler(Looper.getMainLooper()).post(()->{
                for(int i=0;i<locationList.size();i++){
                    final int number = i;

                    LinearLayout locationUpperItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.locationfilter_upperitem,null);
                    LinearLayout.LayoutParams locationUpperItemParams = new LinearLayout.LayoutParams(
                            (int)getResources().getDimension(R.dimen.locationFilter_upperBarItemWidth), ViewGroup.LayoutParams.MATCH_PARENT);

                    TextView locationItemText = locationUpperItem.findViewById(R.id.locationFilter_upperItem_text);
                    View locationItemLowerBar = locationUpperItem.findViewById(R.id.locationFilter_upperItem_lowerbar);

                    locationItemText.setText(locationList.get(number).locationName);
                    if(number == 0){
                        lastClickedUpperBarItemText = locationItemText;
                        lastClickedUpperBarItemText.setTextColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
                        lastClickedUpperBarItemLowerBar = locationItemLowerBar;
                        lastClickedUpperBarItemLowerBar.setBackgroundColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
                    }

                    locationItemText.setOnClickListener(view -> {
                        clickedNewLocationList(number);

                        if(lastClickedUpperBarItemText!=null) {
                            lastClickedUpperBarItemText.setTextColor(ColorHelper.getColor(getResources(),R.color.black));
                        }
                        lastClickedUpperBarItemText = locationItemText;
                        lastClickedUpperBarItemText.setTextColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
                        if(lastClickedUpperBarItemLowerBar!=null){
                            lastClickedUpperBarItemLowerBar.setBackgroundColor(0);
                        }
                        lastClickedUpperBarItemLowerBar = locationItemLowerBar;
                        lastClickedUpperBarItemLowerBar.setBackgroundColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
                    });

                    upperScrollContainer.addView(locationUpperItem,locationUpperItemParams);

                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }



    public String getSelectedLocation(){
        return currentClickedLocationName;
    }

    public void clickedNewLocationList(int num){
        adapter.clickedNewLocationList(num);

        currentClickedLocationName = "";
        selectLocation.setBackgroundResource(R.drawable.roundbox_gray);


        if(listener!=null){
            listener.onUpperBarItemClick(locationList.get(num).locationName);
        }

    }

    public void setListener(LocationFilterListener listener){
        this.listener = listener;
    }

    int ilsanClicked = 0;
    private void itemClicked(String name) {
        if(currentClickedLocationName.equals(name)){
            selectLocation.callOnClick();
        }
        if (name.equals("일산")) {
            if (ilsanClicked == 0) {
                ilsanClicked++;
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ilsanClicked = 0;
                }).start();
            } else {
                ilsanClicked++;
                if (ilsanClicked == 7) {
                    Toast.makeText(getApplicationContext(), "JJCOP is.......", Toast.LENGTH_SHORT).show();
                    ilsanClicked = 0;
                }
            }
        }
    }

}
