package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import kotel.hanzan.Data.EventInfo;
import kotel.hanzan.view.JActivity;

public class Event extends JActivity {
    ImageView back,image,share;
    TextView title;

    EventInfo eventInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        back = (ImageView)findViewById(R.id.event_back);
        image = (ImageView)findViewById(R.id.event_mainImage);
        title = (TextView)findViewById(R.id.event_title);
        share = (ImageView) findViewById(R.id.event_share);
        Intent data = getIntent();
        eventInfo = (EventInfo) data.getSerializableExtra("info");


        title.setText(eventInfo.title);
        Picasso.with(this).load(eventInfo.mainImageAddress).into(image);


        back.setOnClickListener(view -> finish());

        image.setOnClickListener(view -> {
            String[] images = new String[]{eventInfo.mainImageAddress};
            ImageViewer.Builder builder = new ImageViewer.Builder(this, images);
            builder.show();
        });

        share.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_TEXT,title.getText().toString());

            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,getString(R.string.eventShare)));

        });
    }


}
