package kotel.hanzan;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;

import java.util.HashMap;
import java.util.Locale;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.function.ColorHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.LocaleHelper;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.Loading;

public class Settings extends JActivity {
    ImageView back;

    TextView korean,english,termsConditions,announcements, tutorial, withdrawal;
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
        tutorial = (TextView)findViewById(R.id.settings_tutorial);
        withdrawal = (TextView)findViewById(R.id.settings_withdrawal);
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

        termsConditions.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),TermsConditions.class);
            startActivity(intent);
        });

        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),Announcement.class);
            startActivity(intent);
        });

        tutorial.setOnClickListener(view -> {
            StaticData.currentUser.finishedTutorial = false;
            Intent intent = new Intent(getApplicationContext(),Home.class);
            startActivity(intent);
            finishAffinity();
        });

        withdrawal.setOnClickListener(view -> openWithdrawalConfirm());

        facebook.setOnClickListener(view -> openFacebook());
        instagram.setOnClickListener(view -> openInstagram());
        webpage.setOnClickListener(view -> openWebpage());

        back.setOnClickListener(view -> finish());
    }


    private void openWithdrawalConfirm(){
        Loading loading = (Loading)findViewById(R.id.settings_loading);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.withdrawalDialog));
        dialog.setNegativeButton(getString(R.string.no),null);
        dialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
            loading.setLoadingStarted();
            new Thread(()->{
                HashMap<String,String> map = new HashMap<>();
                map.put("id_member",Long.toString(StaticData.currentUser.id));

                map = ServerConnectionHelper.connect("withdrawal on progress","deletemember",map);

                final String result = map.get("delete_result");
                new Handler(getMainLooper()).post(()->{
                    if(result==null ){
                        JLog.e("withdrawal server connection failed!");
                        loading.setLoadingCompleted();
                    }else if(result.equals("TRUE")){
                        StaticData.currentUser = null;
                        AccessToken.setCurrentAccessToken(null);
                        UserManagement.requestUnlink(null);
                        Session.getCurrentSession().close();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finishAffinity();
                    }else if(result.equals("FALSE")){
                        JLog.e("withdrawal failed! - returned FALSE");
                        loading.setLoadingCompleted();
                    }
                });

                }).start();
        });

        dialog.show();
    }



    private void openFacebook() {
        Uri uri = Uri.parse("https://www.facebook.com/HANZANN/");
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/HANZANN/");
            }
        } catch (PackageManager.NameNotFoundException ignored) {}
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void openInstagram(){
        Uri uri = Uri.parse("https://www.instagram.com/hanjan_ninetylabs/");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/hanjan_ninetylabs/")));
        }
    }

    private void openWebpage(){
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.90labs.com")));
    }


}
