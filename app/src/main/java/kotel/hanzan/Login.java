package kotel.hanzan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

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
import kotel.hanzan.view.Loading;

public class Login extends AppCompatActivity {
    HashMap<String,String> map;
    Bitmap bitmap=null;

    Loading loading;

    RelativeLayout facebookLogin;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFacebook();

        loading = (Loading)findViewById(R.id.login_loading);
        facebookLogin = (RelativeLayout)findViewById(R.id.login_facebookLogin);

        facebookLogin.setOnClickListener(view -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        });
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
