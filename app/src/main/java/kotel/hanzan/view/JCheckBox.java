package kotel.hanzan.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import kotel.hanzan.R;
import kotel.hanzan.listener.JCheckBoxClickListener;

/** Use setOnCheckListener() to implement click listener.
 *  DO NOT USE setOnClickListener() since it has already been used in JCheckBox. */

public class JCheckBox extends android.support.v7.widget.AppCompatImageView {
    private JCheckBoxClickListener listener;
    private Context context;
    private boolean checked = false;

    public JCheckBox(Context context) {
        super(context);
        init(context);
    }

    public JCheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOnClickListener(view -> {
            setChecked(!checked);

            ValueAnimator anim = ValueAnimator.ofInt(0,10,0).setDuration(300);
            anim.addUpdateListener(valueAnimator -> {
                int i = (int)valueAnimator.getAnimatedValue();
                setPadding(i,i,i,i);
            });
            anim.start();
        });
        
        callOnClick();
    }

    public void setChecked(boolean checked) {
        this.checked = checked;

        if(checked){
            this.setImageResource(R.drawable.checkbox_checked);
//            this.setImageDrawable(DrawableHelper.getDrawable(getResources(), R.drawable.checkbox_checked));
        }else{
            this.setImageResource(R.drawable.checkbox_unchecked);
//            this.setImageDrawable(DrawableHelper.getDrawable(getResources(), R.drawable.checkbox_unchecked));
        }

        if (listener != null) {
            listener.onClick();
        }
    }
    
    public boolean isChecked(){
        return checked;
    }
    


    public void setOnCheckListener(JCheckBoxClickListener listener) {
        this.listener = listener;
    }

    @Deprecated
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }
}
