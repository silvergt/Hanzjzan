package kotel.hanzan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.Data.UserInfo;
import kotel.hanzan.function.BitmapHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.SlideListener;
import kotel.hanzan.view.HorizontalSlideView;
import kotel.hanzan.view.HorizontalSlideViewChild;
import kotel.hanzan.view.Loading;
import kotel.hanzan.view.SlideCountView;
import pl.droidsonroids.gif.GifImageView;

public class Login extends AppCompatActivity {
    private HashMap<String,String> map;
    private Bitmap bitmap=null;

    private Loading loading;

    private RelativeLayout facebookLogin;

    private CallbackManager callbackManager;

    private HorizontalSlideView slideView;
    private SlideCountView slideCountView;
    private TextView lowerButton;
    private ImageView lowerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFacebook();

        slideView = (HorizontalSlideView)findViewById(R.id.login_slideView);
        slideCountView = (SlideCountView)findViewById(R.id.login_slideCountView);
        lowerButton = (TextView)findViewById(R.id.login_lowerButton);
        lowerIcon = (ImageView)findViewById(R.id.login_lowerIcon);
        loading = (Loading)findViewById(R.id.login_loading);

        LinearLayout.LayoutParams slideViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*7/10);
        slideView.setLayoutParams(slideViewParams);

        slideCountView.initialize(3,40,5);

        addSlideChildViews();



        lowerIcon.setVisibility(View.GONE);


        slideView.setOnSlideListener(new SlideListener() {
            @Override
            public void afterSlide() {
                slideCountView.setCountTo(slideView.getCurrentIndex());
                setLowerButton(slideView.getCurrentIndex());
            }

            @Override
            public void beforeSlide() {

            }

            @Override
            public void whileSlide() {

            }
        });

        lowerButton.setOnClickListener(view -> {
            switch (slideView.getCurrentIndex()){
                case 0:
                case 1:
                    slideView.slideTo(slideView.getCurrentIndex()+1);
                    break;
                case 2:
                    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                    break;
            }
        });
    }

    private void addSlideChildViews(){
        for(int i=0;i<3;i++){
            LinearLayout childLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.login_slideviewchild,null);
            GifImageView gifView = (GifImageView)childLayout.findViewById(R.id.login_gif);
            TextView text1 = (TextView)childLayout.findViewById(R.id.login_childText1);
            TextView text2 = (TextView)childLayout.findViewById(R.id.login_childText2);

            RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            childLayout.setLayoutParams(childParams);

            switch (i){
                case 0:
                    gifView.setImageResource(R.drawable.login_gif1);
                    text1.setText("하루에 한번 한잔 하세요!");
                    text2.setText("한잔 멤버쉽에 가입하면 하루에 한번,\n다양한 종류의 술 한병, 혹은 한잔이 무료입니다.");
                    break;
                case 1:
                    gifView.setImageResource(R.drawable.login_gif2);
                    text1.setText("앱을 보여주세요!");
                    text2.setText("매장 점원에게 한잔 앱을 보여주세요.\n원하시는 음료를 가져다 드릴 것 입니다.\n무료로요!");
                    break;
                case 2:
                    gifView.setImageResource(R.drawable.login_gif3);
                    text1.setText("다양한 제휴 매장을 확인해 보세요");
                    text2.setText("한잔과 여러 매장이 제휴되어 있습니다.\n주변의 매장을 확인해보고 원하는 매장을 방문해주세요.");
                    break;
            }

            HorizontalSlideViewChild childContainer = new HorizontalSlideViewChild(this);
            childContainer.addView(childLayout);

            slideView.setChildWidth(StaticData.displayWidth);
            slideView.addViewToList(childContainer);

        }

        setLowerButton(0);
    }

    private void setLowerButton(int num){
        switch (num){
            case 0:
            case 1:
                lowerIcon.setVisibility(View.GONE);
                lowerButton.setText("다음으로");
                break;
            case 2:
                lowerIcon.setVisibility(View.VISIBLE);
                lowerButton.setText("페이스북으로 시작하기");
                break;
        }
    }




    private void initFacebook(){
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                JLog.v("user ID ", AccessToken.getCurrentAccessToken().getUserId());

                tryLogin();
            }

            @Override
            public void onCancel() {
                JLog.v("facebook login cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                JLog.e("facebook login ERROR!");
            }
        });
    }

    private void tryLogin(){
        loading.setLoadingStarted();
        new Thread(()->{
            map = new HashMap<>();
            map.put("fb_key",AccessToken.getCurrentAccessToken().getUserId());
            map = ServerConnectionHelper.connect("checking account existence","login",map);

            if(map.get("signup_history")==null){
                JLog.e("Connection failed!");
                loading.setLoadingCompleted();
                return;
            }

            if(map.get("signup_history").equals("TRUE")){
                makeUserInfoAndLogin(map);
            }else if(map.get("signup_history").equals("FALSE")){
                map.clear();
                map.put("fb_key",AccessToken.getCurrentAccessToken().getUserId());
                map.put("imageincluded","1");
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large");
                    bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                }catch (Exception e){
                    e.printStackTrace();
                    loading.setLoadingCompleted();
                    return;
                }
                GraphRequest request=GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
                    try {
                        map.put("name_member",object.get("name").toString());

                        map = ServerConnectionHelper.connect("signing up","signup",map,"profileimage", BitmapHelper.getCompressedImageByteArray(bitmap));
                        if(map.get("signupresult").equals("TRUE")){
                            map.clear();
                            map.put("fb_key",AccessToken.getCurrentAccessToken().getUserId());
                            map = ServerConnectionHelper.connect("checking account existence","login",map);
                            if(map.get("signup_history").equals("TRUE")){
                                makeUserInfoAndLogin(map);
                            }else{
                                loading.setLoadingCompleted();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        loading.setLoadingCompleted();
                    }
                });
                request.executeAsync();
            }
        }).start();
    }

    private void makeUserInfoAndLogin(HashMap<String,String> map){
        StaticData.currentUser = new UserInfo(map);
        Intent intent = new Intent(getApplicationContext(),Home.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bitmap.recycle();
        }catch (Exception e){}
    }
}
