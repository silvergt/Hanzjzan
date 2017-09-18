package kotel.hanzan;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.CalendarHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.PaymentHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;

import static kotel.hanzan.function.PaymentHelper.PAYMENT_NOT_ENOUGH_CASH;
import static kotel.hanzan.function.PaymentHelper.PAYMENT_SUCCESS;
import static kotel.hanzan.function.PaymentHelper.PAYMENT_UNKNOWN_ISSUE;

public class PurchaseSuccess extends JActivity {
    public static String tossPayToken="";
    public static String ticketID="";

    private TextView title,membershipName,membershipDue,confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_success);

        title = (TextView)findViewById(R.id.purchaseSuccess_title);
        membershipName = (TextView)findViewById(R.id.purchaseSuccess_membershipName);
        membershipDue = (TextView)findViewById(R.id.purchaseSuccess_expireDate);
        confirm = (TextView)findViewById(R.id.purchaseSuccess_confirm);

        startTossPayment();

        confirm.setOnClickListener(view -> finish());
    }


    private void startTossPayment(){
        if(!tossPayToken.equals("") && !ticketID.equals("") ){
            int paymentResult = PaymentHelper.tossPaymentConfirm(StaticData.TOSSKEY,tossPayToken);

            if(paymentResult == PAYMENT_SUCCESS){
                JLog.v("Toss payment Success!");
                registerNewMembershipInfo();
            }else if(paymentResult == PAYMENT_NOT_ENOUGH_CASH){
                JLog.e("Not enough cash");
                title.setText(getString(R.string.purchaseFailed));
                membershipName.setText(getString(R.string.notEnoughCash));
                membershipDue.setVisibility(View.INVISIBLE);
            }else if(paymentResult == PAYMENT_UNKNOWN_ISSUE){
                JLog.e("User not accessed properly");
                title.setText(getString(R.string.purchaseFailed));
                membershipName.setText(getString(R.string.pleaseTryAgain));
                membershipDue.setVisibility(View.INVISIBLE);
            }
        }
    }


    private void registerNewMembershipInfo(){
        new Thread(()->{
            Uri uri = getIntent().getData();
            if(uri!=null){
                JLog.v("URI ",uri.toString());
                JLog.v("URI QUERY ",uri.getQuery());
                JLog.v("URI QUERY PARAM",uri.getQueryParameter("orderNo"));

                HashMap<String,String> map = new HashMap<>();
                map.put("id_member",Long.toString(StaticData.currentUser.id));
                map.put("id_ticket",ticketID);

                map = ServerConnectionHelper.connect("registering new membership","payment",map);

                if(map.get("availabletoday") == null){
                    new Handler(getMainLooper()).post(()->{
                        title.setText(getString(R.string.purchaseFailed));
                        membershipName.setText(getString(R.string.paymentError));
                        membershipDue.setText(StaticData.adminEmail);
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

            }else{
                JLog.e("URI HAS NO VALUE!");

                new Handler(getMainLooper()).post(()->{
                    title.setText(getString(R.string.purchaseFailed));
                    membershipName.setText(getString(R.string.pleaseTryAgain));
                    membershipDue.setVisibility(View.INVISIBLE);
                });
            }
            tossPayToken = "";
            ticketID = "";
        }).start();

    }
}
