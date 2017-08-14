package kotel.hanzan.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kotel.hanzan.R;

import static android.animation.ObjectAnimator.ofFloat;

public class Loading extends RelativeLayout {
    Context context;

    RelativeLayout layout;
    ImageView loadingIcon;

    ObjectAnimator anim1;
    boolean isShowingLoadingFrontIcon=true;

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
        loadingIcon = (ImageView)layout.findViewById(R.id.loading_image);

        anim1= ofFloat(loadingIcon,"rotationY",-90,90).setDuration(1000);
//        anim1.setRepeatCount(ObjectAnimator.RESTART);
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
                if(isShowingLoadingFrontIcon) {
                    loadingIcon.setImageResource(R.drawable.loading_back);
                    isShowingLoadingFrontIcon=false;
                }else{
                    loadingIcon.setImageResource(R.drawable.loading_front);
                    isShowingLoadingFrontIcon=true;
                }
            }
        });

        setLoadingCompleted();

        addView(layout);
    }




    public void setLoadingStarted(){
        this.setVisibility(VISIBLE);

        anim1.start();
    }

    public void setLoadingCompleted(){
        this.setVisibility(INVISIBLE);

        anim1.cancel();
    }
}
