package kotel.hanzan.function;

import android.util.Log;

public class JLog {
    final private static boolean logIsEnabled=true;

    public static void v(int message){
        if (logIsEnabled) Log.v("Log",Integer.toString(message));
    }

    public static void v(double message){
        if (logIsEnabled) Log.v("Log",Double.toString(message));
    }

    public static void v(String message){
        if (logIsEnabled && message!=null) Log.v("Log",message);
    }

    public static void v(String title,String message){
        if (logIsEnabled && message!=null && title!=null) Log.v(title,message);
    }


    public static void v(String prefix,int content, String suffix){
        if (logIsEnabled && prefix!=null && suffix!=null) Log.v("Log",prefix+Integer.toString(content)+suffix);
    }

    public static void v(String prefix,int content){
        if (logIsEnabled && prefix!=null) Log.v("Log",prefix+Integer.toString(content));
    }

    public static void v(String prefix,double content, String suffix){
        if (logIsEnabled && prefix!=null && suffix!=null) Log.v("Log",prefix+Double.toString(content)+suffix);
    }

    public static void v(String prefix,double content){
        if (logIsEnabled && prefix!=null) Log.v("Log",prefix+Double.toString(content));
    }




    public static void e(int message){
        if (logIsEnabled) Log.e("Log",Integer.toString(message));
    }

    public static void e(String message){
        if (logIsEnabled && message!=null) Log.e("Log",message);
    }

    public static void e(String title,String message){
        if (logIsEnabled && message!=null && title!=null) Log.e(title,message);
    }


    public static void e(String prefix,int content, String suffix){
        if (logIsEnabled && prefix!=null && suffix!=null) Log.e("Log",prefix+Integer.toString(content)+suffix);
    }

    public static void e(String prefix,int content){
        if (logIsEnabled && prefix!=null) Log.e("Log",prefix+Integer.toString(content));
    }

}