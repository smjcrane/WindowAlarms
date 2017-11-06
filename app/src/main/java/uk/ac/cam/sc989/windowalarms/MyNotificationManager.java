package uk.ac.cam.sc989.windowalarms;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Simon on 23/08/2017.
 */

public class MyNotificationManager {
    private Context context;

    MyNotificationManager(Context context){
        this.context = context;
    }
    public void showNotification(String msg) {
        //set up a notification builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.round)
                .setContentTitle("IT IS TIME")
                .setContentText(msg);

        //when they click on the notification, open the AlarmDisplay activity
        Intent resultIntent = new Intent(context, AlarmDisplay.class);
        resultIntent.putExtra("msg",msg);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        //show notification
        int notificationID = 1;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }
}
