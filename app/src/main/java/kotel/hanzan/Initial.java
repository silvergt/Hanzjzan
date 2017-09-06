package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.facebook.AccessToken;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.util.HashMap;

import kotel.hanzan.Data.StaticData;
import kotel.hanzan.Data.UserInfo;
import kotel.hanzan.function.JLog;
import kotel.hanzan.function.ServerConnectionHelper;

public class Initial extends AppCompatActivity {
    private HashMap<String,String> map;

    private SessionCallback callback;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        StaticData.displayWidth=metrics.widthPixels;
        StaticData.displayHeight=metrics.heightPixels;


        if(!Fresco.hasBeenInitialized()) {
            Fresco.initialize(this);
        }

        initKakao();

//        UserManagement.requestLogout(null);         //TEST
//        AccessToken.setCurrentAccessToken(null);    //TEST

        if(AccessToken.getCurrentAccessToken()==null && session==null) {
            JLog.v("No login information");
            Intent intent = new Intent(Initial.this, Login.class);
            startActivity(intent);
            finish();
        }else if(AccessToken.getCurrentAccessToken()!=null){
            JLog.v("Trying facebook login");
            tryLoginWithFacebook();
        }else if(session != null){
            JLog.v("Trying kakaotalk login");
            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    JLog.v("session closed");
                    Intent intent = new Intent(Initial.this, Login.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onNotSignedUp() {
                    JLog.v("session not signed up");
                    Intent intent = new Intent(Initial.this, Login.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onSuccess(UserProfile result) {
                    JLog.v("kakaotalk login onSuccess");
                    tryLoginWithKakaoTalk(result);
                }
            });
        }


    }

    private void initKakao(){
        callback = new SessionCallback();
        session = Session.getCurrentSession();
        session.addCallback(callback);
//        Session.getCurrentSession().checkAndImplicitOpen();
    }


    private void tryLoginWithKakaoTalk(UserProfile userProfile){
        JLog.v("profile ID : ",Long.toString(userProfile.getId()));
        JLog.v("profile Image : ",userProfile.getThumbnailImagePath());
        JLog.v("profile Name : ",userProfile.getNickname());

        new Thread(()->{
            map = new HashMap<>();
            map.put("member_key",StaticData.IDENTIFIER_KAKAO+Long.toString(userProfile.getId()));
            map = ServerConnectionHelper.connect("checking account existence","login",map);

            if(map.get("signup_history")==null || map.get("signup_history").equals("FALSE")){
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }else if (map.get("signup_history").equals("TRUE")) {
                makeUserInfoAndLogin(map);
            }
        }).start();
    }

    private void tryLoginWithFacebook(){
        new Thread(()->{
            map = new HashMap<>();
            map.put("member_key",StaticData.IDENTIFIER_FACEBOOK+AccessToken.getCurrentAccessToken().getUserId());
            map = ServerConnectionHelper.connect("checking account existence","login",map);

            if(map.get("signup_history")==null || map.get("signup_history").equals("FALSE")){
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }else if (map.get("signup_history").equals("TRUE")) {
                makeUserInfoAndLogin(map);
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
    public void startActivity(Intent intent) {
        try{
            Thread.sleep(2000);
        }catch (Exception e){e.printStackTrace();}
        super.startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {}




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
//                    tryLoginWithKakaoTalk(userProfile);
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
