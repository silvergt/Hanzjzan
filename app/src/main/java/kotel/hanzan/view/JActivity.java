package kotel.hanzan.view;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import kotel.hanzan.function.LocaleHelper;


public class JActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
