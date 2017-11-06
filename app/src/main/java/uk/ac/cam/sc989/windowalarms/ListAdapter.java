package uk.ac.cam.sc989.windowalarms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon on 04/09/2017.
 */

public class ListAdapter extends ArrayAdapter<Long> {
    private ArrayList<Long> infoSet;
    private Context mContext;

    public ListAdapter(Context context, int resource, List<Long> items) {
        super(context, resource, items);
        this.mContext = context;
        this.infoSet = (ArrayList<Long>) items;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Log.d("LISTADAPT", "getting View");
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_alarm, null);
        }

        //Integer p = getItem(position);

        //if (p != null) {
        if (true){
            MySQLHelper db = new MySQLHelper(mContext);

            TextView displayTimes = (TextView) v.findViewById(R.id.listItemText);
            SwitchCompat onOff = (SwitchCompat) v.findViewById(R.id.listItemSwitch);
            Button editButton = (Button) v.findViewById(R.id.listItemButton);

            final long thisID = infoSet.get(position);


            if (displayTimes != null) {
                displayTimes.setText(db.getDisplayTimes(thisID));
            }


            if (editButton != null) {
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editAlarm = new Intent(mContext, ActivitySetAlarm.class);
                        editAlarm.putExtra("alarmid", thisID);
                        mContext.startActivity(editAlarm);
                    }
                });
            }

            if (onOff != null){
                boolean isOn = db.queryOn(thisID);
                onOff.setChecked(isOn);
                onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        MySQLHelper db = new MySQLHelper(mContext);
                        if (isChecked){
                            db.turnOn(thisID);
                        } else {
                            db.turnOff(thisID);
                        }
                    }
                });
            }
        }

        return v;
    }

}