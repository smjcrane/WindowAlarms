package uk.ac.cam.sc989.windowalarms;

/**
 * Created by Simon on 25/08/2017.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
import static java.lang.Math.abs;

public class AlarmReceiveToSet extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        SecureRandom rand = new SecureRandom();
        //Get the stuff from SQL
        MySQLHelper db = new MySQLHelper(context);
        Calendar tmp = Calendar.getInstance();
        List<Long> ids = db.queryDay(tmp.get(Calendar.DAY_OF_WEEK));
        for (long id : ids){
            //Get the window details from database
            Cursor res = db.getData(Long.toString(id));
            res.moveToFirst();
            int timeStart = getEntry(res, MySQLHelper.COLUMN_START);
            int timeEnd = getEntry(res, MySQLHelper.COLUMN_END);
            res.close();
            //translate time to millis
            long now = tmp.getTimeInMillis();
            tmp.set(Calendar.HOUR_OF_DAY, timeStart / 60);
            tmp.set(Calendar.MINUTE, timeStart % 60);
            long start = tmp.getTimeInMillis();
            tmp.set(Calendar.HOUR_OF_DAY, timeEnd / 60);
            tmp.set(Calendar.MINUTE, timeEnd % 60);
            long end = tmp.getTimeInMillis();
            //overnight
            if (end < start){
                tmp.add(Calendar.DATE, 1);
                end = tmp.getTimeInMillis();
            }
            //pick the time
            long r = abs(rand.nextLong());
            long diff = end - start;
            long timeMilli = start + (r % diff);
            if (now > timeMilli){
                return;
            }
            //Set an alarm
            AlarmManager alarmManager;
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Intent myIntent = new Intent(context, AlarmReceiver.class);
            myIntent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, myIntent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMilli, pendingIntent);
        }
        db.close();
    }

    private int getEntry(Cursor res, String columnName){
        int index = res.getColumnIndex(columnName);
        int value = res.getInt(index);
        return value;
    }

}