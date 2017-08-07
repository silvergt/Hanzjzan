package kotel.hanzan.function;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 *  1. .getResizedBitmap(~) returns bitmap with resized width, height with a value PROPER_BITMAP_LENGTH
 *  2. .getCompressedImageByteArray(~) returns byte array which can be decoded into bitmap file
 *  3. .getResizedCompressedByteArray(~) retures byte array which is resized and compressed
 */

public class BitmapHelper {
    final public static int COMPRESS_RATIO=90;
    final public static int COMPRESS_RATIO_LARGEIMAGE=50;
    final public static int COMPRESS_RATIO_VERYLARGEIMAGE=30;

    final public static int PROPER_BITMAP_LENGTH=500;

    public static Bitmap getResizedBitmap(Bitmap originalBitmap){
        if(originalBitmap==null){
            return null;
        }
        int oriHeight = originalBitmap.getHeight();
        int oriWidth = originalBitmap.getWidth();
        int newHeight,newWidth;
        if(oriHeight<PROPER_BITMAP_LENGTH&&oriWidth<PROPER_BITMAP_LENGTH){
            return originalBitmap;
        }else if(oriHeight>oriWidth){
            newHeight=PROPER_BITMAP_LENGTH;
            newWidth=(int)((float)oriWidth/oriHeight*PROPER_BITMAP_LENGTH);
        }else{
            newWidth=PROPER_BITMAP_LENGTH;
            newHeight=(int)((float)oriHeight/oriWidth*PROPER_BITMAP_LENGTH);
        }

        return Bitmap.createScaledBitmap(originalBitmap,newWidth,newHeight,false);
    }

    public static byte[] getCompressedImageByteArray(Bitmap bitmap){
        byte[] byteArray=new byte[]{};
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JLog.v("Log", "image size : " + Float.toString(((float) bitmap.getByteCount() / 8000000f)) + "MB");
            if (((float) bitmap.getByteCount() / 8000000f) >= 10f) {
                JLog.v("Log", "compressing very large image!!!");
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_RATIO_VERYLARGEIMAGE, stream);
            } else if (((float) bitmap.getByteCount() / 8000000f) >= 2f) {
                JLog.v("Log", "compressing large image");
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_RATIO_LARGEIMAGE, stream);
            } else {
                JLog.v("Log", "compressing small image");
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_RATIO, stream);
            }
            byteArray = stream.toByteArray();
        }catch (Exception e){}

        return byteArray;
    }

    public static byte[] getResizedCompressedByteArray(Bitmap bitmap){
        return getCompressedImageByteArray(getResizedBitmap(bitmap));
    }

}
