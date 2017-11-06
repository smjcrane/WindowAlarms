package uk.ac.cam.sc989.windowalarms;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;
public class MyDialogFragment extends DialogFragment {
    //the variables we need to get
    private int timeHour;
    private int timeMinute;
    //the handler we will pass them to
    private Handler handler;
    public MyDialogFragment(){
        super();
    }
    @SuppressLint("ValidFragment")
    public MyDialogFragment(Handler handler){
        this.handler = handler;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //get the initial values
        Bundle bundle = getArguments();
        timeHour = bundle.getInt(MyConstants.HOUR);
        timeMinute = bundle.getInt(MyConstants.MINUTE);
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //bundle up the data
                timeHour = hourOfDay;
                timeMinute = minute;
                Bundle b = new Bundle();
                b.putInt(MyConstants.HOUR, timeHour);
                b.putInt(MyConstants.MINUTE, timeMinute);
                Message msg = new Message();
                msg.setData(b);
                //and pass it to the handler for processing
                handler.sendMessage(msg);
            }
        };
        return new TimePickerDialog(getActivity(), listener, timeHour, timeMinute, true);
    }
}