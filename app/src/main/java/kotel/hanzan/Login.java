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
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

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
    private TextView kakaoLogin;
    private ImageView lowerIcon;
    private LinearLayout lowerButton;

    private SessionCallback callback;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFacebook();

        initKakao();

        slideView = (HorizontalSlideView)findViewById(R.id.login_slideView);
        slideCountView = (SlideCountView)findViewById(R.id.login_slideCountView);
        lowerButton = (LinearLayout) findViewById(R.id.login_lowerButton);
        lowerIcon = (ImageView)findViewById(R.id.login_lowerIcon);
        loading = (Loading)findViewById(R.id.login_loading);


        LinearLayout.LayoutParams slideViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,StaticData.displayHeight*7/10);
        slideView.setLayoutParams(slideViewParams);
        addSlideChildViews();

        slideCountView.initialize(4,40,5);

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
            if(slideView.getCurrentIndex()!=3){
                slideView.slideTo(slideView.getCurrentIndex()+1);
            }
            setLowerButton(slideView.getCurrentIndex());
        });

    }


    private void initFacebook(){
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                JLog.v("user ID ", AccessToken.getCurrentAccessToken().getUserId());

                tryLoginWithFacebook();
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

    private void initKakao(){
        callback = new SessionCallback();
        session = Session.getCurrentSession();
        session.addCallback(callback);
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

        LinearLayout childLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.login_slideviewchild_login,null);
        LinearLayout facebookLogin = (LinearLayout)childLayout.findViewById(R.id.login_facebookLogin);
        facebookLogin.setOnClickListener(view -> LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile")));

        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        childLayout.setLayoutParams(childParams);
        HorizontalSlideViewChild childContainer = new HorizontalSlideViewChild(this);
        childContainer.addView(childLayout);

        slideView.setChildWidth(StaticData.displayWidth);
        slideView.addViewToList(childContainer);


        setLowerButton(0);
    }

    private void setLowerButton(int num){
        switch (num){
            case 0:
            case 1:
            case 2:
                lowerButton.setBackgroundResource(R.drawable.roundbox_maingradient);
//                lowerButton.setVisibility(View.VISIBLE);
                break;
            case 3:
                lowerButton.setBackgroundResource(R.drawable.roundbox_gray);
//                lowerButton.setVisibility(View.INVISIBLE);
                break;
        }
    }




    private void tryLoginWithKakaoTalk(UserProfile userProfile){
        JLog.v("profile ID",Long.toString(userProfile.getId()));
        JLog.v("profile Image",userProfile.getThumbnailImagePath());
        JLog.v("profile Name",userProfile.getNickname());

        loading.setLoadingStarted();

        new Thread(()->{
            map = new HashMap<>();
            map.put("member_key",StaticData.IDENTIFIER_KAKAO+Long.toString(userProfile.getId()));
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
                map.put("member_key",StaticData.IDENTIFIER_KAKAO+Long.toString(userProfile.getId()));
                map.put("imageincluded","1");
                try {
                    URL imageURL = new URL(userProfile.getThumbnailImagePath());
                    bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                }catch (Exception e){
                    e.printStackTrace();
                    loading.setLoadingCompleted();
                    return;
                }
                try {
                    map.put("name_member",userProfile.getNickname());

                    map = ServerConnectionHelper.connect("signing up","signup",map,"profileimage", BitmapHelper.getCompressedImageByteArray(bitmap));
                    if(map.get("signupresult").equals("TRUE")){
                        map.clear();
                        map.put("member_key",StaticData.IDENTIFIER_KAKAO+Long.toString(userProfile.getId()));
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
            }
        }).start();
    }

    private void tryLoginWithFacebook(){
        loading.setLoadingStarted();

        new Thread(()->{
            if(AccessToken.getCurrentAccessToken()==null){
                loading.setLoadingCompleted();
                return;
            }

            map = new HashMap<>();
            JLog.v("FBLOGIN!",AccessToken.getCurrentAccessToken().getToken());
            map.put("member_key",StaticData.IDENTIFIER_FACEBOOK+AccessToken.getCurrentAccessToken().getUserId());
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
                map.put("member_key",StaticData.IDENTIFIER_FACEBOOK+AccessToken.getCurrentAccessToken().getUserId());
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
                            map.put("member_key",StaticData.IDENTIFIER_FACEBOOK+AccessToken.getCurrentAccessToken().getUserId());
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

        //FACEBOOK
        callbackManager.onActivityResult(requestCode,resultCode,data);

        //KAKAOTALK
        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Session.getCurrentSession().removeCallback(callback);
        super.onDestroy();
        try {
            bitmap.recycle();
        }catch (Exception e){}
    }





    //****KAKAO

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    tryLoginWithKakaoTalk(userProfile);
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
            // 세션 연결이 실패했을때
        }
    }


}
