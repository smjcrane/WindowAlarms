package uk.ac.cam.sc989.windowalarms;

import java.security.SecureRandom;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Math.abs;

public class ActivitySetAlarm extends FragmentActivity{
    //Initial variables
    private int timeHourStart = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private int timeMinuteStart = Calendar.getInstance().get(Calendar.MINUTE);
    private int timeHourEnd = timeHourStart + 1;
    private int timeMinuteEnd = timeMinuteStart;
    private String message = "Good morning!";
    private int volume = 70;
    private int ringID = -1;

    private boolean startSet = false;
    private boolean endSet = false;
    private boolean editing = false;
    private long id;

    private TextView textDisplayStart;
    private TextView textDisplayEnd;
    private EditText editMessage;
    private SeekBar volumeBar;
    private TextView textDisplayRingtone;

    private SecureRandom rand;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private MySQLHelper sqlHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //setup UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        textDisplayStart = (TextView)findViewById(R.id.textDisplayTimeStart);
        textDisplayEnd = (TextView)findViewById(R.id.textDisplayTimeEnd);
        editMessage = (EditText) findViewById(R.id.editMessage);
        volumeBar = (SeekBar) findViewById(R.id.seekBarVolume);
        textDisplayRingtone = (TextView) findViewById(R.id.textRing);

        //check if we are editing a previously saved alarm or adding a new one
        Intent caller = getIntent();

        // Get the extras (if there are any)
        Bundle extras = caller.getExtras();
        if (extras != null) {
            if (extras.containsKey("alarmid")) {
                id = extras.getLong("alarmid", 0);
                editing = true;
            }
        }

        if (editing){
            Log.d("SET ALARM", "EDITING");
            //both things have been picked
            startSet = true;
            endSet = true;

            //get data out of the database
            sqlHelper = new MySQLHelper(this);

            String times = sqlHelper.getDisplayTimes(id);
            timeHourStart = Integer.parseInt(times.substring(0,2));
            timeMinuteStart = Integer.parseInt(times.substring(3,5));
            timeHourEnd = Integer.parseInt(times.substring(6,8));
            timeMinuteEnd = Integer.parseInt(times.substring(9,11));

            boolean[] days = sqlHelper.getRepeatDays(id);
            ((CheckBox) findViewById(R.id.Mon)).setChecked(days[1]);
            ((CheckBox) findViewById(R.id.Tue)).setChecked(days[2]);
            ((CheckBox) findViewById(R.id.Wed)).setChecked(days[3]);
            ((CheckBox) findViewById(R.id.Thu)).setChecked(days[4]);
            ((CheckBox) findViewById(R.id.Fri)).setChecked(days[5]);
            ((CheckBox) findViewById(R.id.Sat)).setChecked(days[6]);
            ((CheckBox) findViewById(R.id.Sun)).setChecked(days[0]);

            message = sqlHelper.getMessage(id);
            editMessage.setText(message);

            volume = sqlHelper.getVolume(id);
            ringID = sqlHelper.getRingID(id);
            textDisplayRingtone.setText(Common.getRings().get(ringID).first);

            //now delete the old version
            sqlHelper.deleteEntry(id);

        } else {
            //neither one has been picked
            startSet=false;
            endSet=false;

        }

        //display the start and end times
        //by default, these are now and in an hour
        //but we might have changed them in the if statement above
        textDisplayStart.setText(timeHourStart + ":" + (timeMinuteStart < 10 ? "0" : "") + timeMinuteStart);
        textDisplayEnd.setText(timeHourEnd + ":" + (timeMinuteEnd < 10 ? "0" : "") + timeMinuteEnd);
        volumeBar.setProgress(volume);

        //get an alarm thingy
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //make a time-picker dialogue when the user clicks on the "set start time" button
        OnClickListener listenerStart = new OnClickListener() {
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(MyConstants.HOUR, timeHourStart);
                bundle.putInt(MyConstants.MINUTE, timeMinuteStart);
                MyDialogFragment fragment = new MyDialogFragment(new MyHandlerStart());
                fragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, MyConstants.TIME_PICKER);
                transaction.commit();
            }
        };

        Button btnStart = (Button)findViewById(R.id.buttonStart);
        btnStart.setOnClickListener(listenerStart);

        //and when they click on the "set end time" button
        OnClickListener listenerEnd = new OnClickListener() {
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(MyConstants.HOUR, timeHourEnd);
                bundle.putInt(MyConstants.MINUTE, timeMinuteEnd);
                MyDialogFragment fragment = new MyDialogFragment(new MyHandlerEnd());
                fragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, MyConstants.TIME_PICKER);
                transaction.commit();
            }
        };
        Button btnEnd = (Button) findViewById(R.id.buttonEnd);
        btnEnd.setOnClickListener(listenerEnd);

        //Pick the alarm ringtone
        Button btnRing = (Button) findViewById(R.id.buttonRingtone);
        btnRing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getRingtone = new Intent(ActivitySetAlarm.this, PickRingtoneActivity.class);
                startActivityForResult(getRingtone, 1);
            }
        });

        //cancel the alarm if they click on the cancel button
        Button btnCancel = (Button)findViewById(R.id.cancel);
        if (editing){
            btnCancel.setText("Delete");
        }
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sqlHelper.deleteEntry(id);
                finish();
            }
        });

        //Set alarm
        Button btnSave = (Button) findViewById(R.id.save);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the message
                if (editMessage.getText().toString().length() > 0){
                    message = editMessage.getText().toString();
                }
                //Calendar days go from Sunday (1) to Saturday (7)
                boolean days[] = new boolean[8];
                days[0] = false;
                days[1] = ((CheckBox) findViewById(R.id.Sun)).isChecked();
                days[2] = ((CheckBox) findViewById(R.id.Mon)).isChecked();
                days[3] = ((CheckBox) findViewById(R.id.Tue)).isChecked();
                days[4] = ((CheckBox) findViewById(R.id.Wed)).isChecked();
                days[5] = ((CheckBox) findViewById(R.id.Thu)).isChecked();
                days[6] = ((CheckBox) findViewById(R.id.Fri)).isChecked();
                days[7] = ((CheckBox) findViewById(R.id.Sat)).isChecked();
                boolean repeating = false;
                for (boolean b : days){
                    if (b){repeating=true;}
                }
                volume = volumeBar.getProgress();
                saveAlarm(days);
                if(!repeating) {
                    setAlarmToday();
                }
                finish();
            }
        });
    }

    @Override
    //expects the position in the list
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //get the ringtone id
                ringID = data.getIntExtra("ringID", -1);
                if (id != -1){
                    textDisplayRingtone.setText(Common.getRings().get(ringID).first);
                }
            }
        }
    }

    class MyHandlerStart extends Handler {
        //deals with the result of the timepicker dialogues
        @Override
        public void handleMessage (Message msg){
            Bundle bundle = msg.getData();
            timeHourStart = bundle.getInt(MyConstants.HOUR);
            timeMinuteStart = bundle.getInt(MyConstants.MINUTE);
            textDisplayStart.setText(timeHourStart + ":" + (timeMinuteStart < 10 ? "0": "") + timeMinuteStart);
            startSet=true;
        }
    }
    class MyHandlerEnd extends Handler {
        @Override
        //see above
        public void handleMessage (Message msg){
            Bundle bundle = msg.getData();
            timeHourEnd = bundle.getInt(MyConstants.HOUR);
            timeMinuteEnd = bundle.getInt(MyConstants.MINUTE);
            textDisplayEnd.setText(timeHourEnd + ":" + (timeMinuteEnd < 10 ? "0": "") + timeMinuteEnd);
            endSet=true;
        }
    }
    private void setAlarmToday(){
        Log.d("setAlarmToday", "starting...");
        if (startSet && endSet) {
            rand = new SecureRandom();
            //Get the entered values
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.HOUR_OF_DAY, timeHourStart);
            calendar1.set(Calendar.MINUTE, timeMinuteStart);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(Calendar.HOUR_OF_DAY, timeHourEnd);
            calendar2.set(Calendar.MINUTE, timeMinuteEnd);
            //overnight
            if (calendar1.getTimeInMillis()>calendar2.getTimeInMillis()){
                Log.d("SETALARM", "overnight tonight");
                calendar2.add(Calendar.DATE, 1);
            }
            //pick a random time in the interval
            long r = abs(rand.nextLong());
            long diff = calendar2.getTimeInMillis()-calendar1.getTimeInMillis();
            long timeMilli = calendar1.getTimeInMillis() + (r % diff);
            //if it is earlier in the day, set it for tomorrow instead
            if (timeMilli < Calendar.getInstance().getTimeInMillis()){
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTimeInMillis(timeMilli);
                calendar3.add(Calendar.DAY_OF_MONTH, 1);
                timeMilli = calendar3.getTimeInMillis();
            }
            //set the alarm
            Intent myIntent = new Intent(ActivitySetAlarm.this, AlarmReceiver.class);
            myIntent.putExtra("id", id);
            pendingIntent = PendingIntent.getBroadcast(ActivitySetAlarm.this, (int) r, myIntent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMilli, pendingIntent);
            Log.d("setAlarmToday", "done");
        }
    }

    private void saveAlarm(boolean[] days){
        Log.d("saveAlarm", "starting...");
        rand = new SecureRandom();
        //Get the entered values
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, timeHourStart);
        calendar1.set(Calendar.MINUTE, timeMinuteStart);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, timeHourEnd);
        calendar2.set(Calendar.MINUTE, timeMinuteEnd);
        if (calendar1.getTimeInMillis()>calendar2.getTimeInMillis()){
            Log.d("SETALARM", "overnight");
        }
        //save the alarm to a database
        sqlHelper = new MySQLHelper(this);
        id = sqlHelper.addAlarm(
            60 * timeHourStart + timeMinuteStart,
            60 * timeHourEnd + timeMinuteEnd,
            days,
            message,
            volume,
            ringID);
        sqlHelper.close();
        Log.d("saveAlarm", "saved");
        //If it needs to be today as well, set one for today
        int today = calendar1.get(Calendar.DAY_OF_WEEK);
        if (days[today]){
            setAlarmToday();
        }
    }
}