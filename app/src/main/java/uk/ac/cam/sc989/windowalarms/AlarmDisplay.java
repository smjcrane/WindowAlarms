package uk.ac.cam.sc989.windowalarms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlarmDisplay extends AppCompatActivity {
    String msg;
    TextView display;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_display);

        //find out what the message was and display it
        Intent caller = getIntent();
        msg = caller.getStringExtra("msg");
        display = (TextView) findViewById(R.id.textAlarmDisplay);
        display.setText(msg);

        //when they click on the stop button, stop playing the alarm tone
        stop = (Button) findViewById(R.id.stopAlarm);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.alarmPlayer != null) {
                    if (Common.alarmPlayer.isPlaying()) {
                        Common.alarmPlayer.stop();
                        stop.setVisibility(View.INVISIBLE);
                    }
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed(){
        return;
    }
}
