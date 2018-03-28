package kotel.hanzan.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.AttributeSet;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JVrView extends VrPanoramaView {
    public JVrView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JVrView(Context context) {
        super(context);
    }

    public void setVrImage(final String url){
        final JVrView.Options options = new JVrView.Options();
        setInfoButtonEnabled(false);
        setStereoModeButtonEnabled(false);
        setFullscreenButtonEnabled(false);

        setPureTouchTracking(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = getBitmapFromURL(url);

                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        loadImageFromBitmap(bitmap,options);
                    }
                });

            }
        }).start();

    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
