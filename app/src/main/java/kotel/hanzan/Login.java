package kotel.hanzan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.net.URL;
import java.util.Arrays;

import kotel.hanzan.function.JLog;

public class Login extends AppCompatActivity {
    RelativeLayout facebookLogin;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFacebook();

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


                //String userID = loginResult.getAccessToken().getUserId();
                JLog.v("user ID ", AccessToken.getCurrentAccessToken().getUserId());

                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large");
//                    Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
//                    BitmapHelper.getResizedCompressedByteArray(bitmap);
                }catch (Exception e){e.printStackTrace();}


                if(tryLogin()){
                    Intent intent = new Intent(Login.this,Home.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private boolean tryLogin(){


        return true;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);

    }
}
