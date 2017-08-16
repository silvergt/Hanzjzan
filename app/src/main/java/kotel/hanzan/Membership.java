package kotel.hanzan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Membership extends AppCompatActivity {
    ImageView back;
    RelativeLayout item1,item2,item3,item4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        back = (ImageView)findViewById(R.id.membership_back);
        item1 = (RelativeLayout)findViewById(R.id.membership_item1);
        item2 = (RelativeLayout)findViewById(R.id.membership_item2);
        item3 = (RelativeLayout)findViewById(R.id.membership_item3);
        item4 = (RelativeLayout)findViewById(R.id.membership_item4);


        back.setOnClickListener(view -> finish());
    }
}
