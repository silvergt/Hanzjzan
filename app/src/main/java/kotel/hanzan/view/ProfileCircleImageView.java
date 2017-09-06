package kotel.hanzan.view;


import android.content.Context;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import de.hdodenhof.circleimageview.CircleImageView;
import kotel.hanzan.R;

/**
 *  1. setProfileID is temporarily abundant
 *  2. use setImage(~) to set image. default image is R.drawable.profile_null
 *  3. use .openProfilePage(Context context) to manually open profile image viewer
 */

public class ProfileCircleImageView extends CircleImageView {
    Context context;
    long id;
    boolean openMyPage=false;

    String image;
    int res=0;

    public ProfileCircleImageView(Context context) {
        super(context);
        this.context=context;
        init();
    }

    public ProfileCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init();
    }

    private void init(){

    }

    private void setProfileID(long id,boolean openMyPage){
        this.id=id;
        this.openMyPage=openMyPage;
    }

    public void openProfilePage(Context context){
        try{
            if(image!=null&&!image.equals("")&&!image.equals("NULL")){
//                Log.v("Log","opening profile page... image address : "+image);
                new ImageViewer.Builder(context,new String[]{image}).show();
            }else{
                return;
            }

        }catch (Exception e){
//            Log.e("Log","opening profile page failed");
            e.printStackTrace();

        }
    }

    public void setImage(final Context context, String imageAddress){
        if(imageAddress==null||imageAddress.equals("")||imageAddress.toUpperCase().equals("NULL")) {
            Picasso.with(context).load(R.drawable.profile_null).into(this);
            image="NULL";
        }else{
            image=imageAddress;
            Picasso.with(context).load(image).placeholder(R.drawable.profile_null).into(this);
        }
//        setOnClickListener(v -> openProfilePage(context));
    }

    public void setImage(final Context context, int res){
        try {
//            Log.v("Log","image address PCIV : "+res);
        }catch (Exception e){
//            Log.v("Log","image address PCIV is null !! ");
        }
        Picasso.with(context).load(res).into(this);
    }

}
