package br.com.mlcsys.lappoint;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "lAppointment";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TAG = "lAppointment";
    
    private Context mCtx;
    private SQLiteDatabase mDb;
    
    private DayDbAdapter dayAdapter;
    private HourDbAdapter hourAdapter;
    

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        dayAdapter = new DayDbAdapter(db);
        hourAdapter = new HourDbAdapter(db);
        dayAdapter.createDayTable();
        hourAdapter.createHourTable();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        //deleteTables();
        onCreate(db);

    }
    
    public void deleteTables(){
        if(dayAdapter != null){
            dayAdapter.dropTable();
        }
        if(hourAdapter != null){
            hourAdapter.dropTable();
        }
    }
    
    public void open(){
        mDb = this.getWritableDatabase();
    }
    
    public SQLiteDatabase getmDb() {
        return mDb;
    }
    
    public Context getContext() {
        return mCtx;
    }

}
