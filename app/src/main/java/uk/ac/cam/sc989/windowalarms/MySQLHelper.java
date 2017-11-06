package uk.ac.cam.sc989.windowalarms;

/**
 * Created by Simon on 24/08/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MySQLHelper extends SQLiteOpenHelper {

    //constants related to our database
    public static final String DATABASE_NAME = "alarms.db";
    public static final String TABLE_NAME = "alarmstable";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_REPEAT_DAYS = "repeats";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_ON = "onoff";
    public static final String COLUMN_VOLUME = "volume";
    public static final String COLUMN_RINGTONE = "ringid";

    //default constructor
    public MySQLHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    //make the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_START + " INTEGER, " +
                COLUMN_END + " INTEGER, " +
                COLUMN_REPEAT_DAYS + " CHAR(7), " +
                COLUMN_MESSAGE + " VARCHAR(50), " +
                COLUMN_ON + " INTEGER, " +
                COLUMN_VOLUME + " INTEGER, " +
                COLUMN_RINGTONE + " INTEGER " +
                ")"
        );
    }

    //delete and create a new one
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //same
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //add new alarm in the interval [start, end]
    //times given in milliseconds
    public long addAlarm(int start, int end, boolean[] repeatDays, String message, int volume, int ringtone) {
        //Sunday is first day
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_START, start);
        contentValues.put(COLUMN_END, end);
        contentValues.put(COLUMN_ON, 1);
        contentValues.put(COLUMN_VOLUME, volume);
        contentValues.put(COLUMN_RINGTONE, ringtone);
        String daysAsChar = "";
        for (int i= 1; i < 8; i++){
            daysAsChar = daysAsChar + (repeatDays[i] ? "Y" : "N");
        }
        contentValues.put(COLUMN_REPEAT_DAYS, daysAsChar.toString());
        contentValues.put(COLUMN_MESSAGE, message);
        long id = db.insert(TABLE_NAME, null, contentValues);
        Log.d("ADDED TO DB", Long.toString(id));
        return id;
    }

    //delete an entry
    public boolean deleteEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "='" + id + "';");
        return true;
    }

    //perform a search
    public Cursor getData(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME+" where "+COLUMN_ID+"="+id+"", null);
        return res;
    }

    //Get the alarms just for one day of the week
    //Here we use Monday = 0 through to Sunday = 6
    public List<Long> queryDay(int day){
        List<Long> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            String days = getStringEntry(res, COLUMN_REPEAT_DAYS);
            if (days.substring(day - 1, day) == "Y") {
                ids.add(getLongEntry(res, COLUMN_ID));
            }
            res.moveToNext();
        }
        res.close();
        return ids;
    }

    public ArrayList<Long> getAll() {
        ArrayList<Long> array_list = new ArrayList<Long>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_NAME, null );
        if (res == null){
            Log.d("GET ALL", "res is null");
        }
        res.moveToFirst();
        while(res.isAfterLast() == false){
            long id = getLongEntry(res, COLUMN_ID);
            array_list.add(id);
            Log.d("FOUND IN DB", Long.toString(id));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public String getStringEntry(Cursor res, String columnName){
        int index = res.getColumnIndex(columnName);
        String value = res.getString(index);
        return value;
    }

    public int getIntEntry(Cursor res, String columnName){
        int index = res.getColumnIndex(columnName);
        Log.d("GETINT", columnName+", index: "+Integer.toString(index));
        int value = res.getInt(index);
        return value;
    }

    public long getLongEntry(Cursor res, String columnName){
        int index = res.getColumnIndex(columnName);
        long value = res.getLong(index);
        return value;
    }


    public void turnOn(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String myCommand = "UPDATE "+TABLE_NAME+" SET " +
                COLUMN_ON+"=1 WHERE "+COLUMN_ID+"="+Long.toString(id);
        db.execSQL(myCommand);
    }

    public void turnOff(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String myCommand = "UPDATE "+TABLE_NAME+" SET " +
                COLUMN_ON+"=0 WHERE "+COLUMN_ID+"="+Long.toString(id);
        db.execSQL(myCommand);
    }

    public boolean queryOn(long id){
        Cursor res = query(id);
        int on = getIntEntry(res, COLUMN_ON);
        res.close();
        return (on == 1);
    }

    public String getDisplayTimes(long id){
        Cursor res = query(id);
        int start = getIntEntry(res, COLUMN_START);
        int end = getIntEntry(res, COLUMN_END);
        String displayStart = ((start / 60) < 10 ? "0" : "") + (start / 60) + ":" +
                ((start % 60) < 10 ? "0" : "") + (start % 60);
        String displayEnd = ((end / 60) < 10 ? "0" : "") + (end / 60) + ":" +
                ((end % 60) < 10 ? "0" : "") + (end % 60);
        return displayStart+"-"+displayEnd;
    }

    public boolean[] getRepeatDays(long id){
        Cursor res = query(id);
        boolean[] daysArr = new boolean[7];
        String daysStr =  getStringEntry(res, COLUMN_REPEAT_DAYS);
        res.close();
        Log.d("repeatDays", daysStr);
        for (int i = 0; i < 7; i++){
            daysArr[i] = (daysStr.substring(i, i+1).equals("Y"));
        }
        return daysArr;
    }

    public String getMessage(long id){
        Cursor res = query(id);
        String msg = getStringEntry(res, COLUMN_MESSAGE);
        res.close();
        return msg;
    }

    public int getRingID(long id){
        Cursor res = query(id);
        int ringID = getIntEntry(res, COLUMN_RINGTONE);
        res.close();
        return ringID;
    }

    public int getVolume(long id){
        Cursor res = query(id);
        int volume = getIntEntry(res, COLUMN_VOLUME);
        res.close();
        return volume;
    }

    Cursor query (long id){
        Cursor res = getData(Long.toString(id));
        res.moveToFirst();
        return res;
    }
}
