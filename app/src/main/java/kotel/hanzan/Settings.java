package kotel.hanzan;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.function.LocaleHelper;
import kotel.hanzan.view.JActivity;

public class Settings extends JActivity {
    ImageView back;

    TextView korean,english,termsConditions,announcements;
    ImageView facebook,instagram,webpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = (ImageView)findViewById(R.id.settings_back);
        korean = (TextView)findViewById(R.id.settings_korean);
        english = (TextView)findViewById(R.id.settings_english);
        termsConditions = (TextView)findViewById(R.id.settings_termsConditions);
        announcements = (TextView)findViewById(R.id.settings_announcements);
        facebook = (ImageView)findViewById(R.id.settings_facebook);
        instagram = (ImageView)findViewById(R.id.settings_instagram);
        webpage = (ImageView)findViewById(R.id.settings_website);


        if(Locale.getDefault().getLanguage().equals(Locale.KOREA.getLanguage())){
            korean.setTextColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
            korean.setBackgroundResource(R.drawable.roundbox_maincolor_hollow);

        }else if(Locale.getDefault().getLanguage().equals(Locale.US.getLanguage())){
            english.setTextColor(ColorHelper.getColor(getResources(),R.color.mainColor_light));
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

        facebook.setOnClickListener(view -> openFacebook());

        instagram.setOnClickListener(view -> openInstagram());

        webpage.setOnClickListener(view -> openWebpage());

        back.setOnClickListener(view -> finish());
    }

    private void openFacebook() {
        Uri uri = Uri.parse("https://www.facebook.com/HANZANN/");
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + "https://www.facebook.com/HANZANN/");
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void openInstagram(){

    }

    private void openWebpage(){
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.90labs.com")));
    }


}
