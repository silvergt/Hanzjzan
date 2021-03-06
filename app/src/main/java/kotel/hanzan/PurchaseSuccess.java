package kotel.hanzan;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import kotel.hanzan.data.StaticData;
import kotel.hanzan.function.CalendarHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.PaymentHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;

import static kotel.hanzan.function.PaymentHelper.PAYMENT_NOT_ENOUGH_CASH;
import static kotel.hanzan.function.PaymentHelper.PAYMENT_SUCCESS;
import static kotel.hanzan.function.PaymentHelper.PAYMENT_UNKNOWN_ISSUE;

public class PurchaseSuccess extends JActivity {
    final public static String PURCHASE_TOSS="TOSS";
    final public static String PURCHASE_GOOGLEPLAY="GOOGLEPLAY";

    public static String tossPayToken="";
    public static String ticketID="";
    public static String paymentMethod="";

    private TextView title,membershipName,membershipDue,confirm;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_success);

        title = findViewById(R.id.purchaseSuccess_title);
        membershipName = findViewById(R.id.purchaseSuccess_membershipName);
        membershipDue = findViewById(R.id.purchaseSuccess_expireDate);
        confirm = findViewById(R.id.purchaseSuccess_confirm);
        image = findViewById(R.id.purchaseSuccess_image);

        Uri uri = getIntent().getData();
//        try{
//            JLog.v("URI ",uri.toString());
//            JLog.v("URI QUERY ",uri.getQuery());
//            JLog.v("URI QUERY PARAM peymentMethod : ",uri.getQueryParameter("paymentMethod"));
//
//            paymentMethod = uri.getQueryParameter("paymentMethod");
//
//        }catch (Exception e){
//            return;
//        }

        if(StaticData.currentUser != null){
            if(paymentMethod.equals(PURCHASE_TOSS)) {
                JLog.v("URI QUERY PARAM orderNo : ",uri.getQueryParameter("orderNo"));
                String orderNumber = uri.getQueryParameter("orderNo");
                startTossPayment(orderNumber);
            }else if(paymentMethod.equals(PURCHASE_GOOGLEPLAY)){
                JLog.v("Purchasing with google play.. ticketID :",ticketID);
                registerNewMembershipInfo();
            }
        }else{
            JLog.e("User info is empty");
            title.setText(getString(R.string.purchaseFailed));
            membershipName.setText(getString(R.string.pleaseTryAgain));
            membershipDue.setVisibility(View.INVISIBLE);
            image.setImageResource(R.drawable.purchasefailed);
        }

        confirm.setOnClickListener(view -> finish());
    }


    private void startTossPayment(String orderNumber){
        if(!tossPayToken.equals("") && !ticketID.equals("")){
            int paymentResult = PaymentHelper.tossPaymentConfirm(StaticData.TOSSKEY,tossPayToken);

            if(paymentResult == PAYMENT_SUCCESS){
                JLog.v("Toss payment Success!");
                registerNewMembershipInfo();
            }else if(paymentResult == PAYMENT_NOT_ENOUGH_CASH){
                JLog.e("Not enough cash");
                title.setText(getString(R.string.purchaseFailed));
                membershipName.setText(getString(R.string.notEnoughCash));
                membershipDue.setVisibility(View.INVISIBLE);
                image.setImageResource(R.drawable.purchasefailed);
            }else if(paymentResult == PAYMENT_UNKNOWN_ISSUE){
                JLog.e("User not accessed properly");
                title.setText(getString(R.string.purchaseFailed));
                membershipName.setText(getString(R.string.pleaseTryAgain));
                membershipDue.setVisibility(View.INVISIBLE);
                image.setImageResource(R.drawable.purchasefailed);
            }
        }
    }


    private void registerNewMembershipInfo(){
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map.put("id_ticket",ticketID);

            map = ServerConnectionHelper.connect("registering new membership","payment",map);

            if(map.get("availabletoday") == null){
                new Handler(getMainLooper()).post(()->{
                    title.setText(getString(R.string.purchaseFailed));
                    membershipName.setText(getString(R.string.paymentError));
                    membershipDue.setText(StaticData.adminEmail);
                    image.setImageResource(R.drawable.purchasefailed);
                });
            }else{
                boolean availableToday = false;
                if(map.get("availabletoday").equals("TRUE")){
                    availableToday = true;
                }
                int[] newDueDate = CalendarHelper.parseDate(map.get("membershipdue"));
                String name = map.get("name_ticket");
                String dueDate = "~"+Integer.toString(newDueDate[0])+"."+Integer.toString(newDueDate[1])+"."+ Integer.toString(newDueDate[2]);

                StaticData.currentUser.isHanjanAvailableToday = availableToday;
                StaticData.currentUser.expireYYYY = newDueDate[0];
                StaticData.currentUser.expireMM = newDueDate[1];
                StaticData.currentUser.expireDD = newDueDate[2];

                new Handler(getMainLooper()).post(()->{
                    title.setText(getString(R.string.successfullyPurchased));
                    membershipName.setText(name);
                    membershipDue.setText(dueDate);
                });
            }
            tossPayToken = "";
            ticketID = "";
        }).start();

    }
}
