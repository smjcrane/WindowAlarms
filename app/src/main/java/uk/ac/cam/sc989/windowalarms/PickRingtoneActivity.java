package uk.ac.cam.sc989.windowalarms;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class PickRingtoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_ringtone);

        ListView ringList = (ListView) findViewById(R.id.listRing);
        RingtoneListAdapter myAdapter = new RingtoneListAdapter(this, R.layout.list_item_alarm, Common.getRings());
        ringList.setAdapter(myAdapter);
        ringList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("ringID",i);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
