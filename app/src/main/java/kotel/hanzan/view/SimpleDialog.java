package kotel.hanzan.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import kotel.hanzan.R;


public class SimpleDialog extends Dialog {
    private Context context;
    private View.OnClickListener positiveListener,negativeListener;
    private String text,positiveText,negativeText;


    public SimpleDialog(@NonNull Context context, String text, String positiveText, View.OnClickListener positiveListener,
                        String negativeText, View.OnClickListener negativeListener){
        super(context);
        this.context = context;
        this.positiveListener = positiveListener;
        this.negativeListener = negativeListener;
        this.text = text;
        this.positiveText = positiveText;
        this.negativeText = negativeText;
        init();
    }

    public SimpleDialog(@NonNull Context context, String text, String positiveText, View.OnClickListener positiveListener){
        super(context);
        this.context = context;
        this.positiveListener = positiveListener;
        this.text = text;
        this.positiveText = positiveText;
        init();
    }

    private void init(){
        try {
            this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }catch (Exception e){e.printStackTrace();}

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.popupbox_normal,null);
        TextView mainText = layout.findViewById(R.id.popupBox_text);
        TextView positiveButton = layout.findViewById(R.id.popupBox_yes);
        TextView negativeButton = layout.findViewById(R.id.popupBox_no);

        mainText.setText(text);
        positiveButton.setText(positiveText);
        positiveButton.setOnClickListener(positiveListener);
        if(negativeText != null && negativeListener != null){
            negativeButton.setText(negativeText);
            negativeButton.setOnClickListener(negativeListener);
        }else{
            negativeButton.setVisibility(View.GONE);
        }

        this.setContentView(layout);
        this.show();
    }

}
