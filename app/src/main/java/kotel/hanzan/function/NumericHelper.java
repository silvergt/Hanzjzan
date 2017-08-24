package kotel.hanzan.function;

import java.util.ArrayList;


public class NumericHelper {



    public static String toMoneyFormat(String original) {
        String returnString = "";
        ArrayList<Character> temp = new ArrayList<>();

        int length = original.length();
        int count = 0;
        for (int i = 1; i <= length; i++) {
            temp.add(original.charAt(length - i));
            count++;
            if (count % 3 == 0 && i !=
                    length){
                temp.add(',');
            }
        }
        for(int i=1;i<=temp.size();i++){
            returnString += temp.get(temp.size() - i);
        }

        return returnString;
    }

    public static String trimUnderPoint(String string,int underPoint){
        string = string.substring(0,string.indexOf(".")+underPoint);
        return string;
    }
}
