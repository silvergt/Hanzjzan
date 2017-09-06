package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kotel.hanzan.Data.EventInfo;

public class Event extends AppCompatActivity {
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

        share.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_TEXT,title.getText().toString());

            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,"공유하기"));

        });
    }


}
