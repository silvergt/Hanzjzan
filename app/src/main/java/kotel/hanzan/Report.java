package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import kotel.hanzan.data.StaticData;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.Loading;

public class Report extends AppCompatActivity {

    LinearLayout panel1, panel2;
    ImageView back;
    TextView reportMessage;
    TextView report1, report2, report3, report4, report5, report6;
    EditText specificDesc, contactInfo;
    TextView reportButton;
    Loading loading;

    long pubId,contactNumber;
    String pubName;
    int reportCode;

    boolean reportButtonActivated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Intent data = getIntent();
        pubId = data.getLongExtra("pubId",0);
        pubName = data.getStringExtra("pubName");

        loading = findViewById(R.id.report_loading);
        back = findViewById(R.id.report_back);
        reportMessage = findViewById(R.id.report_reportText);
        panel1 = findViewById(R.id.report_panel1);
        panel2 = findViewById(R.id.report_panel2);
        report1 = findViewById(R.id.report_report1);
        report2 = findViewById(R.id.report_report2);
        report3 = findViewById(R.id.report_report3);
        report4 = findViewById(R.id.report_report4);
        report5 = findViewById(R.id.report_report5);
        report6 = findViewById(R.id.report_report6);
        specificDesc = findViewById(R.id.report_specificDesc);
        contactInfo = findViewById(R.id.report_contactInfo);
        reportButton = findViewById(R.id.report_reportButton);

        loading.setLoadingCompleted();

        report1.setOnClickListener(view -> {
            reportChoiceClicked(0);
            reportMessage.setText(report1.getText().toString());
        });
        report2.setOnClickListener(view -> {
            reportChoiceClicked(1);
            reportMessage.setText(report2.getText().toString());
        });
        report3.setOnClickListener(view -> {
            reportChoiceClicked(2);
            reportMessage.setText(report3.getText().toString());
        });
        report4.setOnClickListener(view -> {
            reportChoiceClicked(3);
            reportMessage.setText(report4.getText().toString());
        });
        report5.setOnClickListener(view -> {
            reportChoiceClicked(4);
            reportMessage.setText(report5.getText().toString());
        });
        report6.setOnClickListener(view -> {
            reportChoiceClicked(5);
            reportMessage.setText(report6.getText().toString());
        });

        back.setOnClickListener(view -> onBackButtonClicked());

        contactInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >= 11){
                    toggleReportButton(true);
                }else{
                    toggleReportButton(false);
                }
            }
        });

        reportButton.setOnClickListener(view -> onReportButtonClicked());

        openPanel1();
    }

    private void reportChoiceClicked(int choice){
        reportCode = choice;
        openPanel2();
    }

    private void openPanel1(){
        panel1.setVisibility(View.VISIBLE);
        panel2.setVisibility(View.GONE);

        reportMessage.setText(getString(R.string.report_message));
    }


    private void openPanel2(){
        panel2.setVisibility(View.VISIBLE);
        panel1.setVisibility(View.GONE);

        reportButtonActivated = false;

        specificDesc.setText("");
        contactInfo.setText("");

    }

    private void toggleReportButton(boolean activate){
        reportButtonActivated = activate;

        if(activate){
            reportButton.setBackgroundResource(R.drawable.roundbox_maincolor);
        }else{
            reportButton.setBackgroundResource(R.drawable.roundbox_gray);
        }
    }

    private void onReportButtonClicked(){
        if(reportButtonActivated) {
            new Thread(() -> {
                HashMap<String, String> map = new HashMap<>();
                map.put("id_member", Long.toString(StaticData.currentUser.id));
                map.put("id_place", Long.toString(pubId));
                map.put("code_report", Integer.toString(reportCode));
                map.put("phone_member", Long.toString(contactNumber));
                map.put("description_report", specificDesc.getText().toString());

                map = ServerConnectionHelper.connect("Sending report", "report", map);

                if (map.get("report_result") == null || map.get("report_result").equals("FALSE")) {
                    new Handler(getMainLooper()).post(() -> {
                        Toast.makeText(Report.this, getString(R.string.pleaseTryAgain), Toast.LENGTH_SHORT).show();
                    });
                } else if (map.get("report_result").equals("TRUE")) {
                    new Handler(getMainLooper()).post(() -> {
                        finish();
                        Toast.makeText(Report.this, getString(R.string.thanksForYourReport), Toast.LENGTH_SHORT).show();
                    });
                }

            }).start();
        }else{
            Toast.makeText(Report.this, getString(R.string.fillContactInfo), Toast.LENGTH_SHORT).show();
        }

    }

    private void onBackButtonClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> finish());
        builder.setNegativeButton(getString(R.string.no), null);
        builder.setMessage(getString(R.string.report_back));
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        onBackButtonClicked();
    }
}
