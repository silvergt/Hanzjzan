package kotel.hanzan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import kotel.hanzan.listener.JButtonClickListener;

@SuppressLint("AppCompatCustomView")
public class JButton extends TextView{
    private Context context;
    private JButtonClickListener listener;
    private int upRes = 0,downRes = 0;
    private float oldX,oldY,newX,newY;

    public JButton(Context context) {
        super(context);
        init(context);
    }

    public JButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.context = context;
    }

    public void setClickImages(int upResource, int downResource){
        upRes = upResource;
        downRes = downResource;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setButtonClickListener(JButtonClickListener listener){
        this.listener = listener;
        setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                oldX = motionEvent.getX();
                oldY = motionEvent.getY();

                this.listener.onButtonDown();

                if(downRes != 0){
                    setBackgroundResource(downRes);
                }

                return true;
            }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                newX = motionEvent.getX();
                newY = motionEvent.getY();

                this.listener.onButtonUp();

                if(upRes != 0){
                    setBackgroundResource(upRes);
                }

                float Xsquare = (oldX-newX)*(oldX-newX);
                float Ysquare = (oldY-newY)*(oldY-newY);

                if(Math.sqrt(Xsquare+Ysquare) <= 50){
                    this.listener.onClick();
                }
            }

            return false;
        });

    }


    @Deprecated
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }
}
