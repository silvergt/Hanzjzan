package kotel.hanzan.function;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import kotel.hanzan.Data.StaticData;

public class PaymentHelper {

    public static HashMap<String,String> tossPayment(String ticketID, String itemName, int amount){
        final HashMap<String,String> map = new HashMap<>();

        map.put("apiKey",StaticData.TESTAPI);

        Thread thread = new Thread(()->{
            try {
                URL url = new URL("https://toss.im/tosspay/api/v1/payments");
                URLConnection connection = url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                org.json.simple.JSONObject jsonBody = new org.json.simple.JSONObject();
                String orderNo = StaticData.currentUser.id+"HANJAN"+ticketID+"HANJAN"+System.currentTimeMillis();
                JLog.v("order number",orderNo);
                jsonBody.put("orderNo", orderNo);
                jsonBody.put("amount", amount);
                jsonBody.put("autoExecute", false);
                jsonBody.put("productDesc", itemName);
                jsonBody.put("apiKey", StaticData.TESTAPI);
                jsonBody.put("retUrl","https://90labs.com/");

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

    public static boolean tossPaymentConfirm(String apiKey, String payToken){
        final boolean[] returnValue = {false};

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

                if(Integer.parseInt(map.get("code"))==0){
                    JLog.v("PURCHASE SUCCESS!!");
                    returnValue[0] = true;
                }else{
                    JLog.v("PURCHASE FAILED!!");
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
