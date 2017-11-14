package kotel.hanzan;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kotel.hanzan.data.StaticData;
import kotel.hanzan.function.CalendarHelper;
import kotel.hanzan.function.NumericHelper;
import kotel.hanzan.function.PaymentHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.Loading;

import static kotel.hanzan.data.StaticData.currentUser;

public class Membership extends JActivity {
    final public static int MEMBERSHIP_OPENED = 880;
    final public static int RESULT_MEMBERSHIP_APPLIED = 881;
    private InputMethodManager inputMethodManager;

    private Loading loading;
    private Dialog dialog;

    private ImageView back;
    private TextView expireDate;
    private RecyclerView recyclerView;
    private RelativeLayout promotionLowerBar;
    private MembershipAdapter adapter = new MembershipAdapter();
    private ArrayList<MembershipTicketInfo> ticketArray = new ArrayList<>();

    private int startYYYY, startMM, startDD;

            
    private class MembershipTicketInfo{
        String name,id,imageAddress;

        int originalPrice;
        int discountPrice;
        boolean isNowDiscounted;
        int[] dueDate;


        public MembershipTicketInfo(String id,String name,String imageAddress, int[] dueDate, int originalPrice, int discountPrice) {
            this.id = id;
            this.name = name;
            this.originalPrice = originalPrice;
            this.discountPrice = discountPrice;
            this.dueDate = dueDate;
            this.imageAddress = imageAddress;
            isNowDiscounted = discountPrice != 0;
        }
    }

    class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView saleIcon,title,currentPrice,strikeThroughedPrice,duration;
            ImageView ticketImage;
            public ViewHolder(View itemView) {
                super(itemView);
                saleIcon = itemView.findViewById(R.id.membership_ticket_saleIcon);
                title = itemView.findViewById(R.id.membership_ticket_name);
                currentPrice = itemView.findViewById(R.id.membership_ticket_currentPrice);
                strikeThroughedPrice = itemView.findViewById(R.id.membership_ticket_strikeThroughedPrice);
                duration = itemView.findViewById(R.id.membership_ticket_date);
                ticketImage = itemView.findViewById(R.id.membership_ticket_image);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.membership_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MembershipTicketInfo ticketInfo = ticketArray.get(position);
            if(ticketInfo.isNowDiscounted){
                holder.saleIcon.setVisibility(View.VISIBLE);
                holder.saleIcon.setText(Integer.toString(100 - (int)(100*(float)ticketInfo.discountPrice/ticketInfo.originalPrice))+"%\nSALE");
                holder.strikeThroughedPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.originalPrice))+getString(R.string.won) );
                holder.currentPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.discountPrice))+getString(R.string.won) );
            }else{
                holder.saleIcon.setVisibility(View.INVISIBLE);
                holder.strikeThroughedPrice.setVisibility(View.INVISIBLE);
                holder.currentPrice.setText( NumericHelper.toMoneyFormat(Integer.toString(ticketInfo.originalPrice))+getString(R.string.won) );
            }

            Picasso.with(Membership.this).load(ticketInfo.imageAddress).into(holder.ticketImage);

            holder.title.setText(ticketInfo.name);

            int[] MembershipExpire = ticketInfo.dueDate;


            String expireDate = "~"+Integer.toString(MembershipExpire[0])+"."+Integer.toString(MembershipExpire[1])+"."+
                    Integer.toString(MembershipExpire[2]);
            holder.duration.setText(expireDate);

            holder.itemView.setOnClickListener(view -> {
                openPurchasePopup( ticketInfo.id, ticketInfo.name, ticketInfo.imageAddress,
                        ticketInfo.isNowDiscounted ? ticketInfo.discountPrice : ticketInfo.originalPrice, expireDate );
            });
        }

        @Override
        public int getItemCount() {
            return ticketArray.size();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        try {
            inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }catch (Exception e){
            e.printStackTrace();
        }

        back = findViewById(R.id.membership_back);
        expireDate = findViewById(R.id.membership_expireDate);
        recyclerView = findViewById(R.id.membership_recycler);
        promotionLowerBar = findViewById(R.id.membership_promotion);
        loading = findViewById(R.id.membership_loading);

        if(currentUser.expireYYYY==0){
            //if user is not a member of Hanzan
            int[] startDate = CalendarHelper.getCurrentDate();
            startYYYY = startDate[0];
            startMM = startDate[1];
            startDD = startDate[2];
            expireDate.setText(getString(R.string.notMemberYet));
        }else{
            //if user is a member of Hanzan
            startYYYY = currentUser.expireYYYY;
            startMM = currentUser.expireMM;
            startDD = currentUser.expireDD;
            expireDate.setText(Integer.toString(startYYYY)+"."+Integer.toString(startMM)+"."+Integer.toString(startDD));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        back.setOnClickListener(view -> finish());

        promotionLowerBar.setOnClickListener(view -> {
            openPromotionPopup();
        });
    }



    private void openPromotionPopup(){
        dialog = new Dialog(this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.promotion_inputpopup,null);
        EditText editText = layout.findViewById(R.id.promotion_editText);
        ImageView delete = layout.findViewById(R.id.promotion_delete);
        TextView apply = layout.findViewById(R.id.promotion_apply);
        TextView cancel = layout.findViewById(R.id.promotion_cancel);


        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                apply.callOnClick();
            }
            return false;
        });

        delete.setOnClickListener(view -> editText.setText(""));

        apply.setOnClickListener(view -> {
            try {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }catch (Exception e){e.printStackTrace();}

            String word = editText.getText().toString();

            while(word.startsWith(" ")){
                word = word.substring(1,word.length());
            }
            while(word.endsWith(" ")){
                word = word.substring(0,word.length()-1);
            }
            if (word.length() == 0) {
                Toast.makeText(getApplicationContext(),getString(R.string.notValidPromotionCode),Toast.LENGTH_SHORT).show();
            }else if(word.equals("JUNYORU")){
                Toast.makeText(getApplicationContext(),getString(R.string.yes)+"?",Toast.LENGTH_SHORT).show();
            }else{
                loading.setLoadingStarted();
                usePromotionCode(word);
            }
        });

        cancel.setOnClickListener(view -> dialog.cancel());

        dialog.setContentView(layout);
        dialog.show();
    }

    private void openPromotionSuccessPopup(){
        RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.promotion_successpopup,null);
        TextView confirm = layout.findViewById(R.id.promotion_successConfirm);

        confirm.setOnClickListener(view -> dialog.cancel());

        dialog.setContentView(layout);
    }

    private void usePromotionCode(String code){
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(currentUser.id));
            map.put("name_promotion",code);

            map = ServerConnectionHelper.connect("sending promotion code","usepromotion",map);

            if(map.get("promotion_result")!=null){
                switch (Integer.parseInt(map.get("promotion_result"))){
                    case 0 :
                        new Handler(getMainLooper()).post(() -> {
                            Toast.makeText(getApplicationContext(), getString(R.string.alreadyAppliedPromotion), Toast.LENGTH_SHORT).show();
                        });
                        break;
                    case 1 :
                        new Handler(getMainLooper()).post(() -> {
                            Toast.makeText(getApplicationContext(), getString(R.string.notValidPromotionCode), Toast.LENGTH_SHORT).show();
                        });
                        break;
                    case 2 :
                        new Handler(getMainLooper()).post(() -> {
                            Toast.makeText(getApplicationContext(), getString(R.string.alreadyUsedPromotionCode), Toast.LENGTH_SHORT).show();
                        });
                        break;
                    case 3 :
                        int duration = Integer.parseInt(map.get("durationdays_promotion"));
                        String newDue = map.get("membershipdue");

                        int[] newExpireDate = CalendarHelper.parseDate(newDue);
                        if(StaticData.currentUser.expireYYYY == 0){
                            StaticData.currentUser.isHanjanAvailableToday = true;
                        }
                        StaticData.currentUser.expireYYYY = newExpireDate[0];
                        StaticData.currentUser.expireMM = newExpireDate[1];
                        StaticData.currentUser.expireDD = newExpireDate[2];

                        new Handler(getMainLooper()).post(() -> {
                            try{
                                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }catch (Exception e){e.printStackTrace();}
                            updateMembershipInfo();
                            openPromotionSuccessPopup();
                        });
                        break;
                }
            }

            new Handler(getMainLooper()).post(() -> {
                loading.setLoadingCompleted();
            });

        }).start();
    }



    private void openPurchasePopup(String ticketID, String ticketName, String ticketImage, int price, String expireDate){
        Dialog purchaseDialog = new Dialog(Membership.this);

        purchaseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.membership_purchasepopup,null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*3/4);

        ImageView image = layout.findViewById(R.id.membership_purchaseImage);
        TextView name = layout.findViewById(R.id.membership_purchaseMembershipName);
        TextView purchasePrice = layout.findViewById(R.id.membership_purchaseMembershipPrice);
        TextView expire = layout.findViewById(R.id.membership_purchaseExpireDate);
        RelativeLayout purchaseWithToss = layout.findViewById(R.id.membership_purchaseWithToss);
        RelativeLayout purchaseWithBankTransfer = layout.findViewById(R.id.membership_purchaseWithBankTransfer);
        TextView cancel = layout.findViewById(R.id.membership_purchaseCancel);

        Picasso.with(Membership.this).load(ticketImage).into(image);
        name.setText(ticketName);
        expire.setText(expireDate);
        purchasePrice.setText(NumericHelper.toMoneyFormat(Integer.toString(price))+getString(R.string.won));


        purchaseWithToss.setOnClickListener(view -> {
            purchaseWithToss(ticketID,ticketName,price);
            purchaseDialog.cancel();
        });

        purchaseWithBankTransfer.setOnClickListener(view -> {
            openPurchaseBankTransferPopup(price);
            purchaseDialog.cancel();
        });


        cancel.setOnClickListener(view -> purchaseDialog.cancel());


        purchaseDialog.setContentView(layout,params);
        purchaseDialog.show();
    }

    private void openPurchaseBankTransferPopup(int price){
        Dialog bankTransferDialog = new Dialog(Membership.this);

        bankTransferDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.membership_purchasebanktransferpopup,null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*2/3);

        TextView priceText = layout.findViewById(R.id.membership_purchaseBankTransfer_price);
        TextView confirm = layout.findViewById(R.id.membership_purchaseBankTransfer_confirm);

        priceText.setText(NumericHelper.toMoneyFormat(Integer.toString(price))+getString(R.string.won));
        confirm.setOnClickListener(view -> bankTransferDialog.cancel());

        bankTransferDialog.setContentView(layout,params);
        bankTransferDialog.show();
    }


    private synchronized void retrieveMembershipTicketInfo(){
        ticketArray.clear();
        new Thread(()->{
            HashMap<String,String> map = new HashMap<>();
            map.put("id_member",Long.toString(currentUser.id));
            map = ServerConnectionHelper.connect("retrieving membership ticket info","ticketinfo",map);

            int i=0;
            while (true) {
                String num = Integer.toString(i++);
                if (map.get("durationdays_" + num)==null){
                    break;
                }
                String ticketName = map.get("name_ticket_" + num);
                String ticketID = map.get("id_ticket_" + num);
                String ticketDue = map.get("new_membershipdue_" + num);
                int originalPrice = Integer.parseInt(map.get("originalprice_" + num));
                int discountPrice = Integer.parseInt(map.get("discountprice_" + num));
                String ticketImage = map.get("imgadd_ticket_" + num);

                ticketArray.add(new MembershipTicketInfo(ticketID,ticketName,ticketImage, CalendarHelper.parseDate(ticketDue),originalPrice,discountPrice));
            }
            new Handler(getMainLooper()).post(()->{
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void updateMembershipInfo(){
        loading.setLoadingStarted();
        setResult(RESULT_MEMBERSHIP_APPLIED);

        new Thread(()->{
            HashMap<String, String> map = new HashMap<>();
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving membership status","membershipinfo",map);

            if(map.get("availabletoday")==null){
                loading.setLoadingCompleted();
                return;
            }
            int[] newExpireDate = CalendarHelper.parseDate(map.get("membershipdue"));
            boolean availableToday = false;
            if(map.get("availabletoday").equals("TRUE")){
                availableToday = true;
            }
            StaticData.currentUser.expireYYYY = newExpireDate[0];
            StaticData.currentUser.expireMM = newExpireDate[1];
            StaticData.currentUser.expireDD = newExpireDate[2];
            StaticData.currentUser.isHanjanAvailableToday = availableToday;

            new Handler(getMainLooper()).post(()->{
                if(StaticData.currentUser.expireYYYY == 0){
                    expireDate.setText(getString(R.string.notMemberYet));
                }else{
                    expireDate.setText(Integer.toString(StaticData.currentUser.expireYYYY) + "." + Integer.toString(StaticData.currentUser.expireMM) + "." + Integer.toString(StaticData.currentUser.expireDD));
                }
                retrieveMembershipTicketInfo();
                loading.setLoadingCompleted();
            });
        }).start();

    }


    @Override
    protected void onResume() {
        super.onResume();
        updateMembershipInfo();
    }


    //TOSS

    private void purchaseWithToss(String ticketID,String itemName, int price){
        HashMap<String,String> tossHashMap = PaymentHelper.tossPayment(ticketID,itemName,price);
        if(tossHashMap.get("code") == null) {
            openPurchaseErrorPopup();
        }else if(tossHashMap.get("code").equals("0")) {
            PurchaseSuccess.tossPayToken = tossHashMap.get("payToken");
            PurchaseSuccess.ticketID = tossHashMap.get("ticketID");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tossHashMap.get("checkoutPage")));
            startActivity(intent);
        }
    }


    private void openPurchaseErrorPopup(){
        Dialog dialog = new Dialog(this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.popupbox_normal, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        TextView text = layout.findViewById(R.id.popupBox_text);
        TextView yes = layout.findViewById(R.id.popupBox_yes);
        TextView no = layout.findViewById(R.id.popupBox_no);

        text.setText(getString(R.string.purchaseErrorMessage));

        no.setVisibility(View.INVISIBLE);
        yes.setOnClickListener(view -> {
            dialog.cancel();
        });

        dialog.setContentView(layout);
        dialog.show();
    }
}
