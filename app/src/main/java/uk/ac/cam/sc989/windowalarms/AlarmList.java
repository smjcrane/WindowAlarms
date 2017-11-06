package uk.ac.cam.sc989.windowalarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmList extends AppCompatActivity {

    private ArrayAdapter myAdapter;
    private List times;
    private List ids;
    private ListView listView;
    private MySQLHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        populateList();

        Button buttonAdd = (Button) findViewById(R.id.addAlarm);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addAlarm = new Intent(AlarmList.this, ActivitySetAlarm.class);
                startActivity(addAlarm);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        populateList();
    }

    private void populateList(){
        listView = (ListView) findViewById(R.id.listOfAlarms);
        times = new ArrayList<String>();
        ids = new ArrayList<Integer>();

        db = new MySQLHelper(this);
        ids = db.getAll();
        if (ids.isEmpty()){
            times.add("No Alarms");
            myAdapter = new ArrayAdapter<String>(this, R.layout.list_item_blank, R.id.listItemText, times);
            listView.setAdapter(myAdapter);
        } else {
            for (Object l : ids){
                Log.d("ALARM LIST", Long.toString((long) l));
            }
            myAdapter = new ListAdapter(this, R.layout.list_item_alarm, ids);
            listView.setAdapter(myAdapter);
        }
    }

}
