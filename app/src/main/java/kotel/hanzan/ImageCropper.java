package kotel.hanzan;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import kotel.hanzan.function.BitmapHelper;

public class ImageCropper extends AppCompatActivity {
    public final static int IMAGE_CROP_IMAGESELECT=209;
    public final static int IMAGE_CROP_REQUEST=210;
    public final static int IMAGE_CROP_RESELECT_IMAGE=211;
    public final static int IMAGE_CROP_ABORT=212;
    public final static int IMAGE_CROP_CROPSUCCESS=213;
    public Bitmap croppedImage;
    public static byte[] croppedImageByteArray;

    private CropImageView cropper;
    private ImageView back,rotate;
    private Uri imageUri;
    private TextView[] ratios;
    private TextView confirm;

    private String attribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagecropper);

        attribute=getIntent().getStringExtra("attr");

        init();
    }

    private void init(){
        imageUri=getIntent().getData();

        back=(ImageView)findViewById(R.id.cropper_back);
        rotate=(ImageView)findViewById(R.id.cropper_rotate);
        cropper=(CropImageView)findViewById(R.id.cropper_cropper);
        ratios=new TextView[3];
        ratios[0]=(TextView)findViewById(R.id.portion1);
        ratios[1]=(TextView)findViewById(R.id.portion2);
        ratios[2]=(TextView)findViewById(R.id.portion3);
        confirm=(TextView)findViewById(R.id.cropper_confirm);

        ratios[0].setOnClickListener(v -> cropper.setAspectRatio(1,1));
        ratios[1].setOnClickListener(v -> cropper.setAspectRatio(4,3));
        ratios[2].setOnClickListener(v -> cropper.clearAspectRatio());

        back.setOnClickListener(v -> {
            setResult(CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE);
            finish();
        });

        confirm.setOnClickListener(v -> {
//            croppedImage = new BitmapDrawable(getResources(),cropper.getCroppedImage());
//            croppedImage = cropper.getCroppedImage();
            croppedImageByteArray = BitmapHelper.getResizedCompressedByteArray(cropper.getCroppedImage());


            setResult(RESULT_OK);
            finish();
        });

        rotate.setOnClickListener(v -> cropper.rotateImage(90));

        //cropper.setImageResource(R.drawable.pic2);
        cropper.setImageUriAsync(imageUri);
    }

    @Override
    protected void onStop() {
        try{
            croppedImage.recycle();
        }catch (Exception e){}
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        setResult(IMAGE_CROP_RESELECT_IMAGE);
        super.onBackPressed();
    }
}
