package br.com.mlcsys.lappoint;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DayDbAdapter {

    public static final String ID_DAY = "_id";
    public static final String DAY_COLUMN = "day";

    public static final String TAG = "DAY";

    private static final String DAY_TABLE = "day";

    /**
     * Activity table creation sql statement
     */
    public static final String DAY_TABLE_CREATE = " CREATE TABLE DAY (_id integer primary key autoincrement, "
            + "day text not null);";

    private SQLiteDatabase mDb;

    public DayDbAdapter(SQLiteDatabase mDb) {
        this.mDb = mDb;
    }

    public void createDayTable() {
        mDb.execSQL(DAY_TABLE_CREATE);
    }

    public long createDay(String day) {
        ContentValues content = new ContentValues();
        content.put(DAY_COLUMN, day);
        return mDb.insert(DAY_TABLE, null, content);
    }

    public boolean deleteDay(long idDay) {
        return mDb.delete(DAY_TABLE, ID_DAY + "=" + idDay, null) > 0;
    }

    public Cursor fetchAllDays() {
        String[] columns = new String[] { ID_DAY, DAY_COLUMN };
        return mDb.query(DAY_TABLE, columns, null, null, null, null, null);
    }

    public Cursor fetchDay(long idDay) throws SQLException {
        String[] columns = new String[] { ID_DAY, DAY_COLUMN };
        Cursor mCursor = mDb.query(DAY_TABLE, columns, ID_DAY + "=" + idDay, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor fetchDaysByMonthYear(int month, int year) throws SQLException {
        String adjustedMonthYear = String.valueOf(year) + "-" + String.format("%02d", month+1);
        String[] columns = new String[] { ID_DAY, DAY_COLUMN };
        //2011-04-05
        Cursor mCursor = mDb.query(DAY_TABLE, columns, "substr(" + DAY_COLUMN + ",1,7)"  + "='" + adjustedMonthYear + "'", null, null, null, null);
        return mCursor;
    }
    
    public long selectDay(String day) throws SQLException{
        long id = 0;
        try{
            
            String[] columns = new String[] { ID_DAY};
            Cursor mCursor = mDb.query(DAY_TABLE, columns, DAY_COLUMN + "='" + day+"'", null, null, null, null);
            
            if(mCursor != null){
                if(mCursor.moveToFirst()){
                    id = mCursor.getInt(0);
                }
                mCursor.close();
            }
            
        }catch (Exception e) {
            Log.e(this.toString(), e.getMessage());
        }
        return id;
    }
    
    public Map<Integer, String> selectAllDays(){
        Map<Integer, String> days = new HashMap<Integer, String>();
        String[] columns = new String[] { ID_DAY, DAY_COLUMN };
        Cursor cDays = mDb.query(DAY_TABLE, columns, null, null, null, null, DAY_COLUMN);
        if(cDays != null && cDays.moveToFirst()){
            for(int i = 0; i<cDays.getCount() ; i++){
                int id = cDays.getInt(0);
                String day = cDays.getString(1);
                days.put(id, day);
            }
            cDays.close();
        }
        
        return days;
    }

    public boolean updateDay(long idDay, String day) {
        ContentValues values = new ContentValues();
        values.put(DAY_COLUMN, day);

        return mDb.update(DAY_TABLE, values, ID_DAY + "=" + idDay, null) > 0;
    }
    
    public void dropTable(){
        mDb.execSQL("DROP TABLE " + DAY_TABLE);
    }
}