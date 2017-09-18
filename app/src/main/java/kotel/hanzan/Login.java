package kotel.hanzan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import kotel.hanzan.function.AssetsHelper;
import kotel.hanzan.function.BitmapHelper;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;
import kotel.hanzan.listener.SlideListener;
import kotel.hanzan.view.HorizontalSlideView;
import kotel.hanzan.view.HorizontalSlideViewChild;
import kotel.hanzan.view.JActivity;
import kotel.hanzan.view.Loading;
import kotel.hanzan.view.SlideCountView;
import pl.droidsonroids.gif.GifImageView;

public class Login extends JActivity {
    private HashMap<String, String> map;
    private Bitmap bitmap = null;

    private Loading loading;

    private RelativeLayout layout;

    private CallbackManager callbackManager;

    private HorizontalSlideView slideView;
    private SlideCountView slideCountView;
    private ImageView lowerIcon;
    private LinearLayout lowerButton;

    private SessionCallback callback;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layout = (RelativeLayout) findViewById(R.id.login_login);
        slideView = (HorizontalSlideView) findViewById(R.id.login_slideView);
        slideCountView = (SlideCountView) findViewById(R.id.login_slideCountView);
        lowerButton = (LinearLayout) findViewById(R.id.login_lowerButton);
        lowerIcon = (ImageView) findViewById(R.id.login_lowerIcon);
        loading = (Loading) findViewById(R.id.login_loading);

        initFacebook();
        initKakao();
        tryLogin();

        LinearLayout.LayoutParams slideViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticData.displayHeight * 7 / 10);
        slideView.setLayoutParams(slideViewParams);
        addSlideChildViews();

        slideCountView.initialize(slideView.getListChildCount(), 40, 5);

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
            if (slideView.getCurrentIndex() != 3) {
                slideView.slideTo(slideView.getCurrentIndex() + 1);
            }
            setLowerButton(slideView.getCurrentIndex());
        });

    }


    private void initFacebook() {
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

    private void initKakao() {
        callback = new SessionCallback();
        session = Session.getCurrentSession();
        session.addCallback(callback);
    }


    private void addSlideChildViews() {
        for (int i = 0; i < 3; i++) {
            LinearLayout childLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.login_slideviewchild, null);
            GifImageView gifView = (GifImageView) childLayout.findViewById(R.id.login_gif);
            TextView text1 = (TextView) childLayout.findViewById(R.id.login_childText1);
            TextView text2 = (TextView) childLayout.findViewById(R.id.login_childText2);

            RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            childLayout.setLayoutParams(childParams);

            switch (i) {
                case 0:
                    gifView.setImageResource(R.drawable.login_gif1);
                    text1.setText(getString(R.string.loginText1Main));
                    text2.setText(getString(R.string.loginText1Sub));
                    break;
                case 1:
                    gifView.setImageResource(R.drawable.login_gif2);
                    text1.setText(getString(R.string.loginText2Main));
                    text2.setText(getString(R.string.loginText2Sub));
                    break;
                case 2:
                    gifView.setImageResource(R.drawable.login_gif3);
                    text1.setText(getString(R.string.loginText3Main));
                    text2.setText(getString(R.string.loginText3Sub));
                    break;
            }

            HorizontalSlideViewChild childContainer = new HorizontalSlideViewChild(this);
            childContainer.addView(childLayout);

            slideView.setChildWidth(StaticData.displayWidth);
            slideView.addViewToList(childContainer);

        }

        LinearLayout childLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.login_slideviewchild_login, null);
        LinearLayout facebookLogin = (LinearLayout) childLayout.findViewById(R.id.login_facebookLogin);
        facebookLogin.setOnClickListener(view -> LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile")));

        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        childLayout.setLayoutParams(childParams);
        HorizontalSlideViewChild childContainer = new HorizontalSlideViewChild(this);
        childContainer.addView(childLayout);

        slideView.setChildWidth(StaticData.displayWidth);
        slideView.addViewToList(childContainer);


        setLowerButton(0);
    }

    private void setLowerButton(int num) {
        switch (num) {
            case 0:
            case 1:
            case 2:
//                lowerButton.setBackgroundResource(R.drawable.roundbox_maingradient);
                lowerButton.setVisibility(View.VISIBLE);
                break;
            case 3:
//                lowerButton.setBackgroundResource(R.drawable.roundbox_gray);
                lowerButton.setVisibility(View.INVISIBLE);
                break;
        }
    }



    private void tryLogin(){
        layout.setVisibility(View.INVISIBLE);
        if(AccessToken.getCurrentAccessToken()==null && session==null) {
            JLog.v("No login information");
            onLoginFailed();
        }else if(AccessToken.getCurrentAccessToken()!=null){
            JLog.v("Trying facebook login");
            tryLoginWithFacebook();
        }else if(session != null && session.isOpened()){
            JLog.v("Trying kakaotalk login");
            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    JLog.v(errorResult.getErrorMessage());
                    JLog.v("session closed");
                    onLoginFailed();
                }

                @Override
                public void onNotSignedUp() {
                    JLog.v("session not signed up");
                    onLoginFailed();
                }

                @Override
                public void onSuccess(UserProfile result) {
                    JLog.v("kakaotalk login onSuccess");
                    tryLoginWithKakaoTalk(result);
                }
            });
        }else{
            JLog.v("No login information");
            onLoginFailed();
        }
    }

    private void tryLoginWithKakaoTalk(UserProfile userProfile) {
        new Thread(() -> {
            map = new HashMap<>();
            map.put("member_key", StaticData.IDENTIFIER_KAKAO + Long.toString(userProfile.getId()));
            map = ServerConnectionHelper.connect("checking account existence", "login", map);

            if (map.get("signup_history") == null) {
                JLog.e("Connection failed!");
                onLoginFailed();
                return;
            }

            if (map.get("signup_history").equals("TRUE")) {
                makeUserInfoAndLogin(map);
            } else if (map.get("signup_history").equals("FALSE")) {
                openTermsPage(userProfile);
            }
        }).start();
    }

    private void signUpWithKakaoTalk(UserProfile userProfile){
        map = new HashMap<>();
        try {
            URL imageURL = new URL(userProfile.getThumbnailImagePath());
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            map.put("imageincluded", "1");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("imageincluded", "0");
        }
        try {
            map.put("member_key", StaticData.IDENTIFIER_KAKAO + Long.toString(userProfile.getId()));
            map.put("name_member", userProfile.getNickname());
            if(userProfile.getEmail()!=null){
                map.put("member_email",userProfile.getEmail());
            }

            if(map.get("imageincluded").equals("1")) {
                map = ServerConnectionHelper.connect("signing up", "signup", map, "profileimage", BitmapHelper.getCompressedImageByteArray(bitmap));
            }else{
                map = ServerConnectionHelper.connect("signing up", "signup", map);
            }

            if (map.get("signupresult").equals("TRUE")) {
                map.clear();
                map.put("member_key", StaticData.IDENTIFIER_KAKAO + Long.toString(userProfile.getId()));
                map = ServerConnectionHelper.connect("checking account existence", "login", map);
                if (map.get("signup_history").equals("TRUE")) {
                    makeUserInfoAndLogin(map);
                } else {
                    onLoginFailed();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onLoginFailed();
        }
    }

    private void tryLoginWithFacebook() {
        loading.setLoadingStarted();

        new Thread(() -> {
            if (AccessToken.getCurrentAccessToken() == null) {
                onLoginFailed();
                return;
            }

            map = new HashMap<>();
            JLog.v("FBLOGIN!", AccessToken.getCurrentAccessToken().getToken());
            map.put("member_key", StaticData.IDENTIFIER_FACEBOOK + AccessToken.getCurrentAccessToken().getUserId());
            map = ServerConnectionHelper.connect("checking account existence", "login", map);

            if (map.get("signup_history") == null) {
                JLog.e("Connection failed!");
                onLoginFailed();
                return;
            }

            if (map.get("signup_history").equals("TRUE")) {
                makeUserInfoAndLogin(map);
            } else if (map.get("signup_history").equals("FALSE")) {
                openTermsPage(null);
            }
        }).start();
    }

    private void signUpWithFacebook(){
        map.clear();
        try {
            URL imageURL = new URL("https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            onLoginFailed();
            return;
        }
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
            try{
                map.put("member_email", object.get("email").toString());
            }catch (Exception e){e.printStackTrace();}

            try {
                map.put("member_key", StaticData.IDENTIFIER_FACEBOOK + AccessToken.getCurrentAccessToken().getUserId());
                map.put("imageincluded", "1");
                map.put("name_member", object.get("name").toString());

                map = ServerConnectionHelper.connect("signing up", "signup", map, "profileimage", BitmapHelper.getCompressedImageByteArray(bitmap));
                if (map.get("signupresult").equals("TRUE")) {
                    map.clear();
                    map.put("member_key", StaticData.IDENTIFIER_FACEBOOK + AccessToken.getCurrentAccessToken().getUserId());
                    map = ServerConnectionHelper.connect("checking account existence", "login", map);
                    if (map.get("signup_history").equals("TRUE")) {
                        makeUserInfoAndLogin(map);
                    } else {
                        onLoginFailed();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                onLoginFailed();
            }
        });
        request.executeAsync();
    }

    private void onLoginFailed(){
        new Handler(getMainLooper()).post(()->{
            layout.setVisibility(View.VISIBLE);
            loading.setLoadingCompleted();
        }) ;
    }



    private void openTermsPage(@Nullable UserProfile userProfile){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout termPage = (RelativeLayout)getLayoutInflater().inflate(R.layout.login_agreeterms,null);
        termPage.setLayoutParams(params);

        TextView terms = (TextView)termPage.findViewById(R.id.login_termsText);
        TextView cancel = (TextView)termPage.findViewById(R.id.login_termsCancel);
        TextView agree = (TextView)termPage.findViewById(R.id.login_termsAgree);

        terms.setText(AssetsHelper.loadText(getApplicationContext(),"terms","terms"));

        cancel.setOnClickListener(view -> {
            map.clear();
            deleteTemporaryLoginData();
            onLoginFailed();
            layout.removeView(termPage);
        });

        agree.setOnClickListener(view -> {
            new Thread(()->{
                if(userProfile == null){
                    signUpWithFacebook();
                }else{
                    signUpWithKakaoTalk(userProfile);
                }
            }).start();
        });

        new Handler(getMainLooper()).post(()->{
            layout.addView(termPage);
        });

    }



    private void makeUserInfoAndLogin(HashMap<String, String> map) {
        new Handler(getMainLooper()).post(()-> {
            StaticData.currentUser = new UserInfo(map);
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void deleteTemporaryLoginData() {
        StaticData.currentUser = null;
        AccessToken.setCurrentAccessToken(null);
        UserManagement.requestLogout(null);
        Session.getCurrentSession().close();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //FACEBOOK
        callbackManager.onActivityResult(requestCode, resultCode, data);

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
        } catch (Exception e) {}
    }

    boolean finishIfBackButtonClickedOnceMore = false;
    @Override
    public void onBackPressed() {
        if(finishIfBackButtonClickedOnceMore){
            super.onBackPressed();
        }else{
            Toast.makeText(this,getString(R.string.oneMoreBackButton),Toast.LENGTH_SHORT).show();
            finishIfBackButtonClickedOnceMore = true;
            new Thread(()->{
                try{
                    Thread.sleep(2500);
                    finishIfBackButtonClickedOnceMore = false;
                }catch (Exception e){e.printStackTrace();}
            }).start();
        }
    }


    //****KAKAO

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            loading.setLoadingStarted();

            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    JLog.v(message);
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    String message = "Session closed. msg=" + errorResult;
                    JLog.v(message);
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
            // 세션 연결이 실패했을때
            if (exception != null) {
                Logger.e(exception);
            }
        }
    }

}
