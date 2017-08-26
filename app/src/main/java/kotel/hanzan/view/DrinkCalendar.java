package kotel.hanzan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import kotel.hanzan.R;
import kotel.hanzan.function.JLog;
import kotel.hanzan.listener.DrinkCalendarListener;

public class DrinkCalendar extends RelativeLayout {
    private Context context;

    private RelativeLayout layout;
    private TextView monthText, lowerText;
    private LinearLayout calendarLayout;
    private LinearLayout[] calendarRow;
//    private Calendar calendar;
    private ImageView leftButton, rightButton;

    private RelativeLayout[] cells;

    private DrinkCalendarListener listener;

    private GregorianCalendar calendar;

    private int headerBufferSize;
    

    public DrinkCalendar(Context context) {
        super(context);
        init(context);
    }

    public DrinkCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.drinkcalendar, null);
        monthText = (TextView) layout.findViewById(R.id.drinkCalendar_month);
        calendarLayout = (LinearLayout) layout.findViewById(R.id.drinkCalendar_calendarLayout);
        lowerText = (TextView) layout.findViewById(R.id.drinkCalendar_lowerText);
        leftButton = (ImageView) layout.findViewById(R.id.drinkCalendar_left);
        rightButton = (ImageView) layout.findViewById(R.id.drinkCalendar_right);


        leftButton.setOnClickListener(view -> {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1, 1);
            updateCalendar();
        });

        rightButton.setOnClickListener(view -> {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
            updateCalendar();
        });

/*

        new Thread(() -> {
            calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

            calendarRow = new LinearLayout[6];
            cells = new RelativeLayout[42];

            JLog.v("3");
            for (int i = 0; i < calendarLayout.getChildCount(); i++) {
                calendarRow[i] = (LinearLayout) calendarLayout.getChildAt(i);
                for (int j = 0; j < 7; j++) {
                    cells[i * 7 + j] = (RelativeLayout) calendarRow[i].getChildAt(j);
                }
            }
            JLog.v("4");
            new Handler(Looper.getMainLooper()).post(this::setCalendar);

        }).start();
*/

        calendar = new GregorianCalendar(Locale.getDefault());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

        calendarRow = new LinearLayout[6];
        cells = new RelativeLayout[42];

        for (int i = 0; i < calendarLayout.getChildCount(); i++) {
            calendarRow[i] = (LinearLayout) calendarLayout.getChildAt(i);
            for (int j = 0; j < 7; j++) {
                cells[i * 7 + j] = (RelativeLayout) calendarRow[i].getChildAt(j);
            }
        }
        setCalendar();


        addView(layout);

    }

    public void setCalendar() {
        updateCalendar();
    }

    public void setCalendar(int year, int monthInNormal) {
        calendar.set(year, monthInNormal - 1, 1);
        updateCalendar();
    }

    public void setListener(DrinkCalendarListener listener) {
        this.listener = listener;
    }

    public void setDateChecked(ArrayList<Integer> date) {
        for (int i = 0; i < date.size(); i++) {
            ((ImageView)cells[date.get(i) - 1 + headerBufferSize].getChildAt(0)).setImageResource(R.drawable.calendar_check);
        }
    }

    public int getViewingYear() {
        return calendar.get(Calendar.YEAR);
    }

    public int getViewingMonthInNormal() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    public void setLowerText(String str) {
        lowerText.setText(str);
    }


    private void setTodayChecked() {
        Calendar todayCalendar = Calendar.getInstance();
        if (todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
            ((ImageView)cells[todayCalendar.get(Calendar.DATE) - 1 + headerBufferSize].getChildAt(0)).setImageResource(R.drawable.calendar_check_today);
            ((TextView)cells[todayCalendar.get(Calendar.DATE) - 1 + headerBufferSize].getChildAt(1)).setTextColor(Color.WHITE);
            JLog.v("calcal",todayCalendar.get(Calendar.DATE) - 1);
        }
    }

    private void updateCalendar() {
        headerBufferSize = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        for(int i=0; i<cells.length;i++){
            ((TextView)cells[i].getChildAt(1)).setTextColor(Color.BLACK);
            ((ImageView)cells[i].getChildAt(0)).setImageResource(0);
        }

        monthText.setText(Integer.toString(calendar.get(Calendar.YEAR)) + "년 " + Integer.toString(calendar.get(Calendar.MONTH) + 1) + "월");

        switch (calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
            case 4:
                calendarRow[4].setVisibility(GONE);
                calendarRow[5].setVisibility(GONE);
                break;
            case 5:
                calendarRow[4].setVisibility(VISIBLE);
                calendarRow[5].setVisibility(GONE);
                break;
            case 6:
                calendarRow[4].setVisibility(VISIBLE);
                calendarRow[5].setVisibility(VISIBLE);
                break;
        }

        int i = 0;
        while (i < calendar.getActualMaximum(Calendar.WEEK_OF_MONTH) * 7) {

            if (i < calendar.get(Calendar.DAY_OF_WEEK) - 1 || i > calendar.get(Calendar.DAY_OF_WEEK) + calendar.getActualMaximum(Calendar.DATE) - 2) {
                ((TextView)cells[i++].getChildAt(1)).setText("");
            }else{
                for (int j = 0; j < calendar.getActualMaximum(Calendar.DATE); j++) {
                    ((TextView)cells[i++].getChildAt(1)).setText(Integer.toString(j+1));
                }
            }

        }

        if (listener != null) {
            listener.movedToAnotherMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        }
        setTodayChecked();
    }
}
