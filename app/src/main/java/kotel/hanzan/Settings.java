package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import kotel.hanzan.function.LocaleHelper;
import kotel.hanzan.view.JActivity;

public class Settings extends JActivity {
    ImageView back;

    TextView korean,english,termsConditions,announcements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = (ImageView)findViewById(R.id.settings_back);
        korean = (TextView)findViewById(R.id.settings_korean);
        english = (TextView)findViewById(R.id.settings_english);
        termsConditions = (TextView)findViewById(R.id.settings_termsConditions);
        announcements = (TextView)findViewById(R.id.settings_announcements);


        if(Locale.getDefault().getLanguage().equals(Locale.KOREA.getLanguage())){
            korean.setTextColor(getResources().getColor(R.color.mainColor_light));
            korean.setBackgroundResource(R.drawable.roundbox_maincolor_hollow);

        }else if(Locale.getDefault().getLanguage().equals(Locale.US.getLanguage())){
            english.setTextColor(getResources().getColor(R.color.mainColor_light));
            english.setBackgroundResource(R.drawable.roundbox_maincolor_hollow);
        }


        korean.setOnClickListener(view -> {
            if(!Locale.getDefault().getLanguage().equals(Locale.KOREA.getLanguage())){
                LocaleHelper.setLocale(getApplicationContext(), Locale.KOREA.getLanguage());
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(),Home.class);
                startActivity(intent);
            }
        });

        english.setOnClickListener(view -> {
            if(!Locale.getDefault().getLanguage().equals(Locale.US.getLanguage())){
                LocaleHelper.setLocale(getApplicationContext(), Locale.US.getLanguage());
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(),Home.class);
                startActivity(intent);
            }
        });

        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),Announcement.class);
            startActivity(intent);
        });


        back.setOnClickListener(view -> finish());
    }


}
