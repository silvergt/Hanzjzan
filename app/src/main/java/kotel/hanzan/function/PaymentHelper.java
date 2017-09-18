package kotel.hanzan.function;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import kotel.hanzan.Data.StaticData;

public class PaymentHelper {
    final public static int PAYMENT_SUCCESS=0;
    final public static int PAYMENT_NOT_ENOUGH_CASH=1;
    final public static int PAYMENT_UNKNOWN_ISSUE=2;


    public static HashMap<String,String> tossPayment(String ticketID, String itemName, int amount){
        final HashMap<String,String> map = new HashMap<>();

        map.put("apiKey",StaticData.TOSSTESTKEY);
        map.put("ticketID",ticketID);

        Thread thread = new Thread(()->{
            try {
                URL url = new URL("https://toss.im/tosspay/api/v1/payments");
                URLConnection connection = url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                org.json.simple.JSONObject jsonBody = new org.json.simple.JSONObject();
                String orderNo = StaticData.currentUser.id+"HANJAN"+StaticData.currentUser.name+"HANJAN"+ticketID+"HANJAN"+System.currentTimeMillis();
                JLog.v("order number",orderNo);
                jsonBody.put("orderNo", orderNo);
                jsonBody.put("amount", amount);
                jsonBody.put("autoExecute", false);
                jsonBody.put("productDesc", itemName);
                jsonBody.put("apiKey", StaticData.TOSSTESTKEY);
                jsonBody.put("retUrl","https://90labs.com/%ea%b2%b0%ec%a0%9c-%ec%84%b1%ea%b3%b5-%ed%8e%98%ec%9d%b4%ec%a7%80/");

                BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
                bos.write(jsonBody.toJSONString().getBytes());
                bos.flush();
                bos.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String temp,total = "";
                String[] tempStrings;
                while ((temp = br.readLine()) != null) {
                    total += temp;
                }
                JLog.v(total);
                total = total.substring(1,total.length()-1);
                tempStrings = total.split(",");
                for(int i=0;i<tempStrings.length;i++){
                    String[] tempString2 = tempStrings[i].split(":",2);
                    map.put(tempString2[0].replace("\"",""),tempString2[1].replace("\"",""));
                    JLog.v(tempString2[0].replace("\"","") + " -> " + tempString2[1].replace("\"",""));
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try{
            thread.join();
        }catch (Exception e){
            e.printStackTrace();
        }

        return map;
    }

    public static int tossPaymentConfirm(String apiKey, String payToken){
        final int[] returnValue = {2};

        Thread thread = new Thread(()->{
            try {
                URL url = new URL("https://toss.im/tosspay/api/v1/execute");
                URLConnection connection = url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                org.json.simple.JSONObject jsonBody = new org.json.simple.JSONObject();
                jsonBody.put("payToken", payToken);
                jsonBody.put("apiKey", apiKey);

                BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
                bos.write(jsonBody.toJSONString().getBytes());
                bos.flush();
                bos.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String temp,total = "";
                String[] tempStrings;
                while ((temp = br.readLine()) != null) {
                    total += temp;
                }
                total = total.substring(1,total.length()-2);
                tempStrings = total.split(",");
                HashMap<String,String> map = new HashMap<>();
                for(int i=0;i<tempStrings.length;i++){
                    String[] tempString2 = tempStrings[i].split(":");
                    map.put(tempString2[0].replace("\"",""),tempString2[1].replace("\"",""));
                    JLog.v(tempString2[0].replace("\"","") + " -> " + tempString2[1].replace("\"",""));
                }

                if(map.get("code")!=null && Integer.parseInt(map.get("code"))==0){
                    JLog.v("PURCHASE SUCCESS!!");
                    returnValue[0] = 0;
                }else{
                    JLog.v("PURCHASE FAILED!!");
                    if(map.get("errorCode")!=null && map.get("errorCode").equals("COMMON_NOT_ENOUGH_CASH")){
                        returnValue[0] = 1;
                    }else{
                        returnValue[0] = 2;
                    }

                }

                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try{
            thread.join();
        }catch (Exception e){
            e.printStackTrace();
        }

        return returnValue[0];

    }



}
