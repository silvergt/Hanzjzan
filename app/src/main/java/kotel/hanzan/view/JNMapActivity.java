package kotel.hanzan.view;

import android.content.Context;

import com.nhn.android.maps.NMapActivity;

import kotel.hanzan.function.LocaleHelper;


public class JNMapActivity extends NMapActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
