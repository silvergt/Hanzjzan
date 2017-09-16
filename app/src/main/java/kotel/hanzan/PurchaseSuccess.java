package kotel.hanzan;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.PaymentHelper;
import kotel.hanzan.view.JActivity;

public class PurchaseSuccess extends JActivity {
    public static String tossPayToken="";
    public static String ticketID="";

    private TextView title,membershipName,membershipDue,confirm;

    private String name="",dueDate="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_success);

        title = (TextView)findViewById(R.id.purchaseSuccess_title);
        membershipName = (TextView)findViewById(R.id.purchaseSuccess_membershipName);
        membershipDue = (TextView)findViewById(R.id.purchaseSuccess_expireDate);
        confirm = (TextView)findViewById(R.id.purchaseSuccess_confirm);

        if(!tossPayToken.equals("") && PaymentHelper.tossPaymentConfirm(StaticData.TESTAPI,tossPayToken)){
            JLog.v("Toss payment Success!");
            registerNewMembershipInfo();
        }else{
            JLog.e("User not accessed properly");
            title.setText(getString(R.string.purchaseFailed));
            membershipName.setText(getString(R.string.pleaseTryAgain));
            membershipDue.setVisibility(View.INVISIBLE);
        }

        confirm.setOnClickListener(view -> finish());

    }


    private void registerNewMembershipInfo(){
        new Thread(()->{
            Uri uri = getIntent().getData();
            if(uri!=null){
                JLog.v("URI ",uri.toString());
                JLog.v("URI QUERY ",uri.getQuery());
                JLog.v("URI QUERY PARAM",uri.getQueryParameter("orderNo"));

                new Handler(getMainLooper()).post(()->{
                    membershipName.setText(name);
                    membershipDue.setText(dueDate);
                });
            }else{
                JLog.e("URI HAS NO VALUE!");
                title.setText(getString(R.string.purchaseFailed));
                membershipName.setText(getString(R.string.pleaseTryAgain));
                membershipDue.setVisibility(View.INVISIBLE);
            }
            tossPayToken = "";
            ticketID = "";
        }).start();

    }
}
