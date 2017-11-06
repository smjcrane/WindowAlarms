package uk.ac.cam.sc989.windowalarms;

import android.media.MediaPlayer;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Simon on 23/08/2017.
 */

public class Common {
    //used to start and stop the alarm tone from separate activities
    public static MediaPlayer alarmPlayer;

    //To store the names of ringtones
    public static ArrayList<Pair<String,String>> getRings(){
        Pair<String,String>[] ringNames = new Pair[5];
        ringNames[0] = new Pair<>("Surprise", "David Szestay");
        ringNames[1] = new Pair<>("Adrenaline", "Dave Depper");
        ringNames[2] = new Pair<>("In the Dust", "Alex Fitch");
        ringNames[3] = new Pair<>("Toy Piano Trader", "Adam Selzer");
        ringNames[4] = new Pair<>("Gilding the Lily", "Lee Rosevere");
        return new ArrayList<>(Arrays.asList(ringNames));
    }

    public static int getMedia(int ringID){
        int[] ids = {R.raw.surprise_david_szesztay,
        R.raw.adrenaline_dave_depper,
        R.raw.in_the_dust_alex_fitch,
        R.raw.toy_piano_trader_adam_selzer,
        R.raw.gilding_the_lily_lee_rosevere};
        return ids[ringID];
    }
}
