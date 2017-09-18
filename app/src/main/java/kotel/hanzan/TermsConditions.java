package kotel.hanzan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import kotel.hanzan.function.AssetsHelper;

public class TermsConditions extends AppCompatActivity {
    ImageView back;
    TextView termsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsconditions);

        back = (ImageView)findViewById(R.id.termsConditions_back);
        termsText = (TextView)findViewById(R.id.termsConditions_termsText);

        back.setOnClickListener(view -> finish());
        termsText.setText(AssetsHelper.loadText(getApplicationContext(),"terms","terms"));
    }
}
