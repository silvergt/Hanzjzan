package kotel.hanzan.kakao;

import android.app.Application;

import com.kakao.auth.KakaoSDK;

public class GlobalApplication extends Application{
    private static volatile GlobalApplication instance = null;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        KakaoSDK.init(new KakaoSDKAdapter());
    }



    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

}
