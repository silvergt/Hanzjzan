package kotel.hanzan.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kotel.hanzan.R;

import static android.animation.ObjectAnimator.ofFloat;

public class Loading extends RelativeLayout {
    private Context context;

    private RelativeLayout layout;
    private ImageView loadingIcon;

    private ObjectAnimator anim1;
    private boolean isShowingLoadingFrontIcon=true;
    private Handler handler;

    public Loading(Context context) {
        super(context);
        init(context);
    }

    public Loading(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context=context;
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.loading,null);
        loadingIcon = layout.findViewById(R.id.loading_image);

        handler = new Handler(Looper.getMainLooper());

        anim1= ofFloat(loadingIcon,"rotationY",-90,90).setDuration(1000);
        anim1.setRepeatMode(ObjectAnimator.RESTART);
        anim1.setRepeatCount(Integer.MAX_VALUE);
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {
                handler.post(()->{
                    if(isShowingLoadingFrontIcon) {
                        loadingIcon.setImageResource(R.drawable.loading_back);
                        isShowingLoadingFrontIcon=false;
                    }else{
                        loadingIcon.setImageResource(R.drawable.loading_front);
                        isShowingLoadingFrontIcon=true;
                    }
                });

            }
        });


        this.setVisibility(INVISIBLE);

        addView(layout);
    }




    public void setLoadingStarted(){
        handler.post(()->{
            this.setVisibility(VISIBLE);
            anim1.start();
        });

    }

    public void setLoadingCompleted(){
        handler.post(()->{
            this.setVisibility(INVISIBLE);
            anim1.cancel();
        });
    }
}
