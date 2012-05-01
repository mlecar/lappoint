package br.com.mlcsys.lappoint;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class AppointWidgetActivity extends Activity {
    
    private DatabaseHelper mDbHelper;
    private DayDbAdapter mDayDbAdapter;
    private HourDbAdapter mHourDbAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.open();
        mDayDbAdapter = new DayDbAdapter(mDbHelper.getmDb());
        mHourDbAdapter = new HourDbAdapter(mDbHelper.getmDb());
        
        Calendar cal = Calendar.getInstance();
        String day = DateUtil.formataDia(cal);
        String hour = DateUtil.formataHora(cal);
        
        long idDay = mDayDbAdapter.selectDay(day);
        
        boolean createdHour = false;
        if(idDay == 0){
            idDay = mDayDbAdapter.createDay(day);
            mHourDbAdapter.createHour(idDay, hour);
            createdHour = true;
        }else{
            long idHour = mHourDbAdapter.selectHour(idDay, hour);
            if(idHour == 0){
                mHourDbAdapter.createHour(idDay, hour);
                createdHour = true;
            }
        }
        if(createdHour){
            Toast.makeText(getApplicationContext(), R.string.success_appointed_hour, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), R.string.already_appointed_hour, Toast.LENGTH_SHORT).show();
        }
        mDbHelper.close();
        finish();
    }

}
