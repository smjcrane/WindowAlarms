package uk.ac.cam.sc989.windowalarms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon on 23/09/2017.
 */

public class RingtoneListAdapter extends ArrayAdapter<Pair<String, String>> {
    private Context mContext;
    private ArrayList<Pair<String, String>> infoSet;


    public RingtoneListAdapter(Context context, int resource, List<Pair<String, String>> items) {
        super(context, resource, items);
        this.mContext = context;
        this.infoSet = (ArrayList<Pair<String, String>>) items;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_ringtone, null);
        }

        TextView nameText = (TextView) v.findViewById(R.id.textRingName);
        TextView authorText = (TextView) v.findViewById(R.id.textRingAuthor);

        nameText.setText(infoSet.get(position).first);
        authorText.setText(infoSet.get(position).second);


        return v;
    }
}
