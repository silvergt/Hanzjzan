package kotel.hanzan.function;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

/** This class is to play sound without worrying about deprecation of older version of Android OS.
 * Create new SoundHelper wherever you want to play sound(put sound resource in soundRes).
 * Then call playSound() to play sound.
 * If soundIsOn has been set to false, sound won't be played. */

public class SoundHelper{
    private static boolean soundIsOn=true;
    private SoundPool soundPool;

    private int soundID;

    @SuppressWarnings("deprecation")
    public SoundHelper(Context context,int maxStreams,int soundRes){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 1);
        }


        soundID=soundPool.load(context, soundRes,1);
    }

    public void playSound(){
        if(soundIsOn){
            soundPool.play(soundID,1,1,1,0,1);
        }
    }


    public static void setSoundStatus(boolean turnOn){
        soundIsOn=turnOn;
    }

    public static boolean getSoundStatus(){
        return soundIsOn;
    }

}
