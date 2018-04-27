package kotel.hanzan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import kotel.hanzan.function.JLog;
import kotel.hanzan.view.JVrView;

public class VrPage extends AppCompatActivity {
    RelativeLayout layout;
    JVrView vrView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrpage);

        layout = findViewById(R.id.vrpage_layout);

        vrView = new JVrView(this);

        String src = getIntent().getStringExtra("src");
        vrView.setVrImage(src);

        layout.addView(vrView);

        vrView.setVrListener(() -> {
            JLog.v("LOADING COMPLETE!!!!");
        });

    }
}
