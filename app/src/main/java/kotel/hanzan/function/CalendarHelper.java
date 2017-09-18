package kotel.hanzan.function;

import java.util.Calendar;

public class CalendarHelper {
    public static int[] getCurrentDate(){
        int[] returnValue = new int[3];

        Calendar calendar = Calendar.getInstance();
        returnValue[0] = calendar.get(Calendar.YEAR);
        returnValue[1] = calendar.get(Calendar.MONTH)+1;
        returnValue[2] = calendar.get(Calendar.DATE);

        return returnValue;
    }


    /** This method parse String date information(YYYYMMDD format) into int[]{YYYY,MM,DD} */
    public static int[] parseDate(String rawDateString){
        int[] returnValue = new int[3];
        try {
            int year = Integer.parseInt(rawDateString.substring(0, 4));
            int month = Integer.parseInt(rawDateString.substring(4, 6));
            int day = Integer.parseInt(rawDateString.substring(6, 8));
            returnValue[0] = year;
            returnValue[1] = month;
            returnValue[2] = day;
        }catch (Exception e){
            returnValue[0] = 0;
            returnValue[1] = 0;
            returnValue[2] = 0;
        }

        return returnValue;
    }



    /** This method returns the int[] date format which is after 'months' months from 'startDate' */
    public static int[] getDateAfterMonths(int months, int[] startDate){
        int[] returnValue = getDateAfterDays(getDaysOfMonths(months,startDate),startDate);

        return returnValue;
    }


    /** This method returns the int[] date format which is after 'days' days from 'startDate' */
    public static int[] getDateAfterDays(int days, int[] startDate){
        int[] returnValue = new int[3];

        returnValue[0] = startDate[0];
        returnValue[1] = startDate[1];
        returnValue[2] = startDate[2];

        while( days > 0 ){
            returnValue[2]++;
            days--;
            if(returnValue[2] > getActualMaximumDate(returnValue[0],returnValue[1])){
                returnValue[2] = 1;
                returnValue[1]++;
                if(returnValue[1] == 13){
                    returnValue[1] = 1;
                    returnValue[0]++;
                }
            }
        }
        return returnValue;
    }





    //해당 기간이 총 몇일인지
    public static int getDaysOfMonths(int months, int[] startDate){
        int totalDate = 0;

        int[] date = new int[]{startDate[0],startDate[1],startDate[2]};

        for(int i=0 ; months > 0 ; i++){
            months--;
            totalDate += getActualMaximumDate(date[0],date[1]);
            date[1]++;
            if(date[1] == 13){
                date[0]++;
                date[1] = 1;
            }
        }

        totalDate -= 1;

        return totalDate;
    }

    //해당 월이 총 몇일인지
    public static int getActualMaximumDate(int year, int monthInNormal){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthInNormal-1,1);

        return calendar.getActualMaximum(Calendar.DATE);
    }



    public static int getDaysBetweenDates(int[] startDate, int[] endDate){
        int returnValue;

        Calendar startCal = Calendar.getInstance();
        startCal.set(startDate[0],startDate[1] - 1,startDate[2]);

        Calendar endCal = Calendar.getInstance();
        endCal.set(endDate[0],endDate[1] - 1,endDate[2]);

        long diff=endCal.getTimeInMillis()-startCal.getTimeInMillis();
        returnValue=(int)(diff / (24 * 60 * 60 * 1000));

        returnValue = Math.abs(returnValue);

        return returnValue;
    }


}
