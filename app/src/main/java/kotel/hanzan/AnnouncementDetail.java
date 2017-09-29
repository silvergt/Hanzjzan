package kotel.hanzan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import kotel.hanzan.Data.AnnouncementInfo;

public class AnnouncementDetail extends AppCompatActivity {
    AnnouncementInfo announcementInfo;
    ImageView back;
    TextView title,content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcementdetail);

        announcementInfo = (AnnouncementInfo) getIntent().getSerializableExtra("info");

        back = findViewById(R.id.announcementDetail_back);
        title = findViewById(R.id.announcementDetail_title);
        content = findViewById(R.id.announcementDetail_text);

        back.setOnClickListener(view -> finish());

        title.setText(announcementInfo.title);
        content.setText(announcementInfo.content);
    }
}
