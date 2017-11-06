package uk.ac.cam.sc989.windowalarms;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        //get the message
        long id = intent.getLongExtra("id", 0);
        MySQLHelper db = new MySQLHelper(context);
        double volume = (double) db.getVolume(id);
        boolean on = db.queryOn(id);
        int ringID = db.getRingID(id);
        if (on){
            //Show Notification
            String message = db.getMessage(id);
            MyNotificationManager myManager = new MyNotificationManager(context);
            myManager.showNotification(message);
            /*
            //Play default alarm ringtone
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            ringtone.play();
            */
            //Play a song
            Common.alarmPlayer = MediaPlayer.create(context, Common.getMedia(ringID));
            Common.alarmPlayer.setLooping(true);
            Common.alarmPlayer.setAudioAttributes(
                    new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_ALARM).
                    build());
            //logarithms and shit
            if (volume != 0) {
                volume = Math.log(volume / 100);
            }
            Common.alarmPlayer.setVolume((float) volume, (float) volume);
            try {
                Common.alarmPlayer.prepare();
            } catch (Exception e){
                e.printStackTrace();
            }
            Common.alarmPlayer.start();
        }
        boolean repeat = false;
        boolean[] days = db.getRepeatDays(id);
        for (boolean b : days){
            if (b){
                repeat = true;
            }
        }
        if (!repeat){
            db.turnOff(id);
        }
        db.close();
    }
}