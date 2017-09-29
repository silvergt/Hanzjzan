package kotel.hanzan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.HashMap;

import kotel.hanzan.Data.EventInfo;
import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;

public class Event extends JActivity {
    ImageView back,image,share;
    TextView title,content;

    EventInfo eventInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        back = findViewById(R.id.event_back);
        image = findViewById(R.id.event_mainImage);
        title = findViewById(R.id.event_title);
        content = findViewById(R.id.event_content);
        share = findViewById(R.id.event_share);

        Intent data = getIntent();

        Uri uri = data.getData();
        if(uri != null) {
            if(StaticData.currentUser == null){
                reopenLoginPage();
                return;
            }
            //Case if user entered this activity by clicking event link
            JLog.v("QUERY",uri.getQuery());
            JLog.v("QUERY eventID",uri.getQueryParameter("eventId"));
            long eventId = Long.parseLong(uri.getQueryParameter("eventId"));
            retrieveEventInfo(eventId);
        }else{
            //Case if user entered this activity via our application
            eventInfo = (EventInfo) data.getSerializableExtra("info");
            title.setText(eventInfo.title);
            Picasso.with(this).load(eventInfo.mainImageAddress).placeholder(R.drawable.loading_store).into(image);
            content.setText(eventInfo.content);
        }


        back.setOnClickListener(view -> finish());

        image.setOnClickListener(view -> {
            String[] images = new String[]{eventInfo.mainImageAddress};
            ImageViewer.Builder builder = new ImageViewer.Builder(this, images);
            builder.show();
        });

        share.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_TEXT,"\nhttps://90labs.com/share_event/?provider=Drinkat&eventId="+Long.toString(eventInfo.id));

            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent,getString(R.string.eventShare)));

        });
    }

    private void retrieveEventInfo(long eventId){
        new Thread(()->{
            HashMap<String, String> map = new HashMap<>();
            map.put("id_event", Long.toString(eventId));
            map.put("id_member",Long.toString(StaticData.currentUser.id));
            map = ServerConnectionHelper.connect("retrieving event info","shareevent",map);

            if(map.get("titleimgadd_event")==null){
                return;
            }
            String titleImageAddress = map.get("titleimgadd_event");
            String titleText = map.get("title_event");
            String mainImageAddress = map.get("mainimgadd_event");
            String contentText = map.get("content_event");

            eventInfo = new EventInfo(eventId,titleImageAddress,mainImageAddress,titleText,contentText);
            new Handler(getMainLooper()).post(()->{
                title.setText(eventInfo.title);
                Picasso.with(this).load(eventInfo.mainImageAddress).placeholder(R.drawable.loading_store).into(image);
                content.setText(eventInfo.content);
            });
        }).start();
    }

    private void reopenLoginPage(){
        Intent intent = new Intent(getApplicationContext(), Initial.class);
        startActivity(intent);
        finishAffinity();
    }


}
