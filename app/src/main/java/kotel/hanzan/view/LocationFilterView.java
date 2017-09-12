package kotel.hanzan.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kotel.hanzan.R;
import kotel.hanzan.listener.LocationFilterListener;

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
        ArrayList<String> specificLocations;
        ArrayList<Boolean> specificLocationsIsActivated;

        public Location(String locationName, ArrayList<String> specificLocations, ArrayList<Boolean> specificLocationsIsActivated) {
            this.locationName = locationName;
            this.specificLocations = specificLocations;
            this.specificLocationsIsActivated = specificLocationsIsActivated;
        }
    }

    private class FilterRecyclerViewAdapter extends RecyclerView.Adapter<FilterRecyclerViewAdapter.ViewHolder> {
        int selectedNumber = -1;
        TextView lastClickedTextView;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView comingSoon,locationName;

            public ViewHolder(View itemView) {
                super(itemView);
                comingSoon = (TextView)itemView.findViewById(R.id.locationFilter_comingSoon);
                locationName = (TextView)itemView.findViewById(R.id.locationFilter_locationName);
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
                holder.locationName.setTextColor(context.getResources().getColor(R.color.black));
                holder.locationName.setBackgroundResource(R.drawable.roundbox_black_hollow);
            }else{
                holder.locationName.setTextColor(context.getResources().getColor(R.color.darkGray));
                holder.locationName.setBackgroundResource(R.drawable.roundbox_gray_hollow);
            }

            holder.itemView.setOnClickListener(view -> {
                if(isActive){
                    currentClickedLocationName = locationName;
                    selectLocation.setBackgroundResource(R.drawable.roundbox_maincolor);

                    if(lastClickedTextView!=null){
                        lastClickedTextView.setTextColor(context.getResources().getColor(R.color.black));
                        lastClickedTextView.setBackgroundResource(R.drawable.roundbox_black_hollow);
                    }
                    holder.locationName.setTextColor(context.getResources().getColor(R.color.white));
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

        upperScrollContainer = (LinearLayout)layout.findViewById(R.id.locationFilter_upperBarContainer);
        itemRecycler = (RecyclerView)layout.findViewById(R.id.locationFilter_locationContainer);
        selectLocation = (TextView)layout.findViewById(R.id.locationFilter_selectLocation);
        searchAroundMe = (TextView)layout.findViewById(R.id.locationFilter_aroundMe);


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


    private void test(){
        ArrayList<String> loc1 = new ArrayList<>();
        loc1.add("신촌");
        loc1.add("건대입구");
        loc1.add("회기");
        loc1.add("혜화");
        loc1.add("이태원");
        loc1.add("안암");
        loc1.add("강남");
        ArrayList<Boolean> loc1isActivated = new ArrayList<>();
        loc1isActivated.add(true);
        loc1isActivated.add(false);
        loc1isActivated.add(false);
        loc1isActivated.add(false);
        loc1isActivated.add(false);
        loc1isActivated.add(false);
        loc1isActivated.add(false);
        locationList.add(new Location("서울",loc1,loc1isActivated));

        ArrayList<String> loc2 = new ArrayList<>();
        loc2.add("일산");
        loc2.add("분당");
        loc2.add("판교");
        loc2.add("안산");
        ArrayList<Boolean> loc2isActivated = new ArrayList<>();
        loc2isActivated.add(false);
        loc2isActivated.add(false);
        loc2isActivated.add(false);
        loc2isActivated.add(false);
        locationList.add(new Location("경기",loc2,loc2isActivated));

    }

    public void retrieveLocationDataFromServer(){
        locationList.clear();


        test();



        for(int i=0;i<locationList.size();i++){
            final int number = i;

            LinearLayout locationUpperItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.locationfilter_upperitem,null);
            LinearLayout.LayoutParams locationUpperItemParams = new LinearLayout.LayoutParams(
                    (int)getResources().getDimension(R.dimen.locationFilter_upperBarItemWidth), ViewGroup.LayoutParams.MATCH_PARENT);

            TextView locationItemText = (TextView)locationUpperItem.findViewById(R.id.locationFilter_upperItem_text);
            View locationItemLowerBar = locationUpperItem.findViewById(R.id.locationFilter_upperItem_lowerbar);

            locationItemText.setText(locationList.get(number).locationName);
            if(number == 0){
                lastClickedUpperBarItemText = locationItemText;
                lastClickedUpperBarItemText.setTextColor(getResources().getColor(R.color.mainColor_light));
                lastClickedUpperBarItemLowerBar = locationItemLowerBar;
                lastClickedUpperBarItemLowerBar.setBackgroundColor(getResources().getColor(R.color.mainColor_light));
            }

            locationItemText.setOnClickListener(view -> {
                clickedNewLocationList(number);

                if(lastClickedUpperBarItemText!=null) {
                    lastClickedUpperBarItemText.setTextColor(getResources().getColor(R.color.black));
                }
                lastClickedUpperBarItemText = locationItemText;
                lastClickedUpperBarItemText.setTextColor(getResources().getColor(R.color.mainColor_light));
                if(lastClickedUpperBarItemLowerBar!=null){
                    lastClickedUpperBarItemLowerBar.setBackgroundColor(0);
                }
                lastClickedUpperBarItemLowerBar = locationItemLowerBar;
                lastClickedUpperBarItemLowerBar.setBackgroundColor(getResources().getColor(R.color.mainColor_light));
            });

            upperScrollContainer.addView(locationUpperItem,locationUpperItemParams);
        }


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



}
