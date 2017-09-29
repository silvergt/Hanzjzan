package kotel.hanzan;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import kotel.hanzan.function.AssetsHelper;
import kotel.hanzan.view.JActivity;

public class TermsConditions extends JActivity {
    ImageView back;
    TextView termsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsconditions);

        back = findViewById(R.id.termsConditions_back);
        termsText = findViewById(R.id.termsConditions_termsText);

        back.setOnClickListener(view -> finish());
        termsText.setText(AssetsHelper.loadText(getApplicationContext(),"terms","terms"));
    }
}
