package kotel.hanzan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.PaymentHelper;

public class PurchaseSuccess extends AppCompatActivity {
    public static String tossPayToken="";

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
            membershipName.setText(name);
            membershipDue.setText(dueDate);
            tossPayToken = "";
        }else{
            title.setText(getString(R.string.purchaseFailed));
            membershipName.setText(getString(R.string.pleaseTryAgain));
        }

        confirm.setOnClickListener(view -> finish());

    }
}
