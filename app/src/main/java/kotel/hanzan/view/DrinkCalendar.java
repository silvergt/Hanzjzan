package kotel.hanzan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import kotel.hanzan.R;
import kotel.hanzan.listener.DrinkCalendarListener;

public class DrinkCalendar extends RelativeLayout{
    private Context context;

    private RelativeLayout layout;
    private TextView monthText, lowerText;
    private LinearLayout calendarLayout;
    private LinearLayout[] calendarRow;
    private Calendar calendar;
    private ImageView leftButton,rightButton;

    private CalendarCell[] cells;

    private DrinkCalendarListener listener;

    private class CalendarCell extends RelativeLayout{
        private ImageView image;
        private TextView dateText;
        public CalendarCell(Context context) {
            super(context);
            init();
        }

        public void init(){
            image=new ImageView(context);
            dateText=new TextView(context);

            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            layoutParams.setMargins(10,10,10,10);
            layoutParams.weight=1;
            setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            image.setLayoutParams(params);
            dateText.setLayoutParams(params);

            addView(dateText);
            addView(image);
        }

        public void setDate(int date){
            dateText.setText(Integer.toString(date));
            dateText.setTextColor(Color.BLACK);
            dateText.setGravity(Gravity.CENTER);
        }

        public void setFrontImage(int res){
            Picasso.with(context).load(res).into(image);
        }

        public void setBackImage(int res){
            dateText.setBackgroundResource(res);
        }
    }

    public DrinkCalendar(Context context) {
        super(context);
        init(context);
    }

    public DrinkCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        this.context=context;
        calendar=Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),1);

        layout=(RelativeLayout) LayoutInflater.from(context).inflate(R.layout.drinkcalendar,null);
        monthText=(TextView) layout.findViewById(R.id.drinkCalendar_month);
        calendarLayout=(LinearLayout) layout.findViewById(R.id.drinkCalendar_calendarLayout);
        lowerText=(TextView)layout.findViewById(R.id.drinkCalendar_lowerText);
        leftButton=(ImageView)layout.findViewById(R.id.drinkCalendar_left);
        rightButton=(ImageView)layout.findViewById(R.id.drinkCalendar_right);


        calendarRow=new LinearLayout[7];
        for(int i=0;i<calendarRow.length;i++){
            calendarRow[i]=new LinearLayout(context);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
            calendarRow[i].setOrientation(LinearLayout.VERTICAL);
            params.weight=1;
            calendarLayout.addView(calendarRow[i],params);
        }

        leftButton.setOnClickListener(view -> {
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)-1,1);
            updateCalendar();
        });

        rightButton.setOnClickListener(view -> {
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,1);
            updateCalendar();
        });

        addView(layout);

        setCalendar();
    }

    public void setCalendar(){
        updateCalendar();
    }

    public void setCalendar(int year, int monthInNormal){
        calendar.set(year,monthInNormal-1,1);
        updateCalendar();
    }

    public void setListener(DrinkCalendarListener listener){
        this.listener=listener;
    }

    public void setDateChecked(int[] date){
        for(int i=0;i<date.length;i++){
            cells[date[i]-1].setFrontImage(R.mipmap.ic_launcher);
        }
    }

    public int getViewingYear(){
        return calendar.get(Calendar.YEAR);
    }

    public int getViewingMonthInNormal(){
        return calendar.get(Calendar.MONTH)+1;
    }

    public void setLowerText(String str){
        lowerText.setText(str);
    }




    private void setTodayChecked(){
        Calendar todayCalendar=Calendar.getInstance();
        if(todayCalendar.get(Calendar.YEAR)==calendar.get(Calendar.YEAR) && todayCalendar.get(Calendar.MONTH)==calendar.get(Calendar.MONTH)){
            cells[todayCalendar.get(Calendar.DATE)-1].setBackgroundResource(R.mipmap.ic_launcher);
        }
    }

    private void updateCalendar(){
        monthText.setText(Integer.toString(calendar.get(Calendar.YEAR))+"년 "+Integer.toString(calendar.get(Calendar.MONTH)+1)+"월");

        for(int i=0;i<calendarRow.length;i++){
            calendarRow[i].removeAllViews();
        }

        cells=new CalendarCell[calendar.getActualMaximum(Calendar.DATE)];

        int columnToAdd=0;
        int i=0;
        while(i<calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)*7){
            if(columnToAdd==7)columnToAdd=0;

            if(i<calendar.get(Calendar.DAY_OF_WEEK)-1||i>calendar.get(Calendar.DAY_OF_WEEK)+cells.length-2){
                //달력 시작 전 부분
                CalendarCell buffer = new CalendarCell(context);
                calendarRow[columnToAdd++].addView(buffer);
                i++;
            }else{
                for(int j=0;j<cells.length;j++){
                    if(columnToAdd==7)columnToAdd=0;
                    cells[j] = new CalendarCell(context);
                    cells[j].setDate(j + 1);
                    calendarRow[columnToAdd++].addView(cells[j]);
                    i++;
                }
            }

        }

        if(listener!=null){
            listener.movedToAnotherMonth(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1);
        }

        setTodayChecked();

    }
}
