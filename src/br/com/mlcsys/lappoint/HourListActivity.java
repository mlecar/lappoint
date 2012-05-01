package br.com.mlcsys.lappoint;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

public class HourListActivity extends ListActivity {
    
    private DatabaseHelper mDbHelper;
    private HourDbAdapter mHourDbAdapter;
    private DayDbAdapter mDayDbAdapter;
    private ListView list = null;
    private static final int DELETE_ID = Menu.FIRST;
    private static final int EDIT_ID = Menu.FIRST + 1;
    
    
    private Long mDayId;
    
    private String mHour;
    private String mMinute;
    private long currentHourId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.hour_list);
            mDbHelper = new DatabaseHelper(this);
            mDbHelper.open();
            mHourDbAdapter = new HourDbAdapter(mDbHelper.getmDb());
            mDayDbAdapter = new DayDbAdapter(mDbHelper.getmDb());
            
            Bundle extras = getIntent().getExtras();
            mDayId = extras != null ? extras.getLong(HourDbAdapter.ID_DAY) : null;

            list = (ListView) findViewById(android.R.id.list);
            registerForContextMenu(list);
            fillData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void fillData(){
        
        Cursor hoursCursor = mHourDbAdapter.fetchHoursByDay(mDayId);
        startManagingCursor(hoursCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{HourDbAdapter.HOUR_COLUMN};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.hour};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter hours = 
            new SimpleCursorAdapter(this, R.layout.hour_row, hoursCursor, from, to);
        setListAdapter(hours);
        
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit_hour);
        menu.add(0, DELETE_ID, 1, R.string.menu_delete_hour);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mHourDbAdapter.deleteHourDay(mDayId, info.id);
                Cursor daysCursor = mHourDbAdapter.fetchHoursByDay(mDayId);
                startManagingCursor(daysCursor);
                if(!daysCursor.moveToFirst()){
                    mDayDbAdapter.deleteDay(mDayId);
                }
                setResult(RESULT_OK);
                finish();
                return true;
            case EDIT_ID:
                AdapterContextMenuInfo info1 = (AdapterContextMenuInfo) item.getMenuInfo();
                Cursor chour = mHourDbAdapter.fetchHour(info1.id);
                
                currentHourId = info1.id;
                
                if(chour != null){
                    startManagingCursor(chour);
                    String hour = chour.getString(1);
                    mHour = hour.substring(0, 2);
                    mMinute = hour.substring(3, 5);
                    chour.close();
                }
                
                showDialog(EDIT_ID);
                
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case EDIT_ID:
            return new TimePickerDialog(this,
                    mTimeSetListener, new Integer(mHour), new Integer(mMinute), true);
        }
        return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case EDIT_ID:
            TimePickerDialog timeDialog = (TimePickerDialog) dialog;
            timeDialog.updateTime(new Integer(mHour), new Integer(mMinute));
        }
    }
    
    
    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour = DateUtil.formataHora(hourOfDay, minute);
                long idHour = mHourDbAdapter.selectHour(mDayId, hour);
                if(idHour == 0){
                    mHourDbAdapter.updateHour(currentHourId, DateUtil.formataHora(hourOfDay, minute));
                    fillData();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.already_appointed_hour, Toast.LENGTH_SHORT).show();
                }
                
            }
        };
    
    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        mDbHelper.close();
    }
}
