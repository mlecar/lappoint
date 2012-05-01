package br.com.mlcsys.lappoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HourDbAdapter {

    public static final String ID_DAY = "id_day";
    public static final String ID_HOUR = "_id";
    public static final String HOUR_COLUMN = "hour";

    public static final String TAG = "HOUR";

    private static final String HOUR_TABLE = "hour";

    /**
     * Activity table creation sql statement
     */
    public static final String HOUR_TABLE_CREATE = " CREATE TABLE HOUR (_id integer primary key autoincrement, id_day integer not null, "
            + "hour text not null, foreign key(id_day) references DAY(_id));";

    private SQLiteDatabase mDb;

    public HourDbAdapter(SQLiteDatabase mDb) {
        this.mDb = mDb;
    }

    public void createHourTable() {
        mDb.execSQL(HOUR_TABLE_CREATE);
    }

    public long createHour(long idDay, String hour) {
        ContentValues content = new ContentValues();
        content.put(ID_DAY, idDay);
        content.put(HOUR_COLUMN, hour);
        return mDb.insert(HOUR_TABLE, null, content);
    }

    public boolean deleteHour(long idHour) {
        return mDb.delete(HOUR_TABLE, ID_HOUR + "=" + idHour, null) > 0;
    }

    public boolean deleteHourDay(long idDay, long idHour) {
        return mDb.delete(HOUR_TABLE, ID_DAY + "=" + idDay + " AND " + ID_HOUR + "=" + idHour, null) > 0;
    }
    
    public boolean deleteDayHours(long idDay) {
        return mDb.delete(HOUR_TABLE, ID_DAY + "=" + idDay, null) > 0;
    }

    public Cursor fetchAllHours() {
        String[] columns = new String[] { ID_HOUR, HOUR_COLUMN };
        return mDb.query(HOUR_TABLE, columns, null, null, null, null, null);
    }
    
    public Cursor fetchHoursByDay(long idDay) {
        String[] columns = new String[] { ID_HOUR, HOUR_COLUMN };
        return mDb.query(HOUR_TABLE, columns, ID_DAY + "=" + idDay, null, null, null, HOUR_COLUMN);
    }
    
    public long selectHour(String hour) throws SQLException{
        long id = 0;
        String[] columns = new String[] { ID_DAY};
        Cursor mCursor = mDb.query(HOUR_TABLE, columns, HOUR_COLUMN + "='" + hour+"'", null, null, null, null);
        
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                id = mCursor.getInt(0);
            }
            mCursor.close();
        }
        return id;
    }
    
    public long selectHour(long idDay, String hour) throws SQLException{
        long id = 0;
        String[] columns = new String[] { ID_DAY};
        Cursor mCursor = mDb.query(HOUR_TABLE, columns, ID_DAY + "=" + idDay + " AND " + HOUR_COLUMN + "='" + hour+"'", null, null, null, null);
        
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                id = mCursor.getInt(0);
            }
            mCursor.close();
        }
        return id;
    }
    

    public Cursor fetchHour(long idHour) throws SQLException {
        String[] columns = new String[] { ID_HOUR, HOUR_COLUMN };
        Cursor mCursor = mDb.query(HOUR_TABLE, columns, ID_HOUR + "=" + idHour, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public List<String> selectHoursByDaysList(Map<Integer, String> mDays){
        List<String> result = new ArrayList<String>();
        String days = "(";
        boolean primeiroPassou = false;
        for(Iterator<Integer> it = mDays.keySet().iterator() ; it.hasNext() ;){
            Integer id = it.next();
            if(primeiroPassou){
                days += id + ",";
            }else{
                days += id;
                primeiroPassou = true;
            }
        }
        days = days.substring(0, days.length()-2) + ")";
        String query = " SELECT d.id, d.day, h.hour FROM day d INNER JOIN hour ON h.id_day = d.id WHERE d.id IN " + days + " ORDER BY d.day, h.hour";
        
        Integer currentDayId = 0;
        String dayRef = "";
        String hours = "";
        Cursor mCursor = mDb.rawQuery(query, null);
        if(mCursor != null && mCursor.moveToFirst()){
            Integer dayId = mCursor.getInt(0);
            dayRef = mCursor.getString(1);
            currentDayId = dayId;
            hours += mCursor.getString(2);
            
            while(mCursor.moveToNext()){
                if(dayId.equals(currentDayId)){
                    hours += mCursor.getString(2) + ",";
                }else{
                    result.add(dayRef + "," + hours);
                    dayId = mCursor.getInt(0);
                    dayRef = mCursor.getString(1);
                    hours += mCursor.getString(2);
                }
            }
            mCursor.close();
        }
        
        return result;
    }

    public boolean updateHour(long idHour, String hour) {
        ContentValues values = new ContentValues();
        values.put(HOUR_COLUMN, hour);

        return mDb.update(HOUR_TABLE, values, ID_HOUR + "=" + idHour, null) > 0;
    }
    
    public void dropTable(){
        mDb.execSQL("DROP TABLE " + HOUR_TABLE);
    }

}
