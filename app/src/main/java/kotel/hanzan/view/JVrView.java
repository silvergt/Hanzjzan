package kotel.hanzan.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.AttributeSet;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kotel.hanzan.listener.VrViewListener;

public class JVrView extends VrPanoramaView {
    VrViewListener listener;

    class VrEventListener extends VrPanoramaEventListener{
        @Override
        public void onLoadSuccess() {
            if(listener!=null) {
                listener.onLoadingComplete();
            }
            super.onLoadSuccess();
        }
    }

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


        new Thread(() -> {
            final Bitmap bitmap = getBitmapFromURL(url);
            new android.os.Handler(Looper.getMainLooper()).post(() -> loadImageFromBitmap(bitmap,options));
        }).start();

    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setVrListener(VrViewListener listener){
        this.listener = listener;
        setEventListener(new VrEventListener());
    }
}
