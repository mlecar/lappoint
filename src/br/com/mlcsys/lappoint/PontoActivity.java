package br.com.mlcsys.lappoint;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

public class PontoActivity extends Activity {

    private DatabaseHelper mDbHelper;
    private DayDbAdapter mDayDbAdapter;
    private HourDbAdapter mHourDbAdapter;

    private static final int DELETE_HOUR_ID = Menu.FIRST + 1;
    private static final int EDIT_HOUR_ID = Menu.FIRST + 2;
    private static final int DAY_DELETE = 0;
    
    private ListView list = null;
    
    private String mHour;
    private String mMinute;
    private long currentHourId;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            mDbHelper = new DatabaseHelper(this);
            mDbHelper.open();
            mDayDbAdapter = new DayDbAdapter(mDbHelper.getmDb());
            mHourDbAdapter = new HourDbAdapter(mDbHelper.getmDb());

            Button confirmButton = (Button) findViewById(R.id.btnPoint);

            confirmButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Calendar cal = Calendar.getInstance();
                    String day = DateUtil.formataDia(cal);
                    String hour = DateUtil.formataHora(cal);
                    
                    long idDay = mDayDbAdapter.selectDay(day);
                    
                    if(idDay == 0){
                        idDay = mDayDbAdapter.createDay(day);
                        mHourDbAdapter.createHour(idDay, hour);
                    }else{
                        long idHour = mHourDbAdapter.selectHour(idDay, hour);
                        if(idHour == 0){
                            mHourDbAdapter.createHour(idDay, hour);
                        }
                    }
                    
                    fillData();
                }

            });

            list = (ListView) findViewById(R.id.listPoints);
            registerForContextMenu(list);
            fillData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void fillData(){
        
        Calendar cal = Calendar.getInstance();
        String day = DateUtil.formataDia(cal);
        long idDay = mDayDbAdapter.selectDay(day);
        
        Cursor hoursCursor = mHourDbAdapter.fetchHoursByDay(idDay);
        startManagingCursor(hoursCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{HourDbAdapter.HOUR_COLUMN};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.hourMain};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter hours = 
            new SimpleCursorAdapter(this, R.layout.main_hour_row, hoursCursor, from, to);
        list.setAdapter(hours);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appoint_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_view_days:
            viewDays();
            return true;
        case R.id.menu_about:
            about();
            return true;
        case R.id.export_csv:
            exportCsv();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void about(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.getResources().getString(R.string.about_text));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(sb.toString());
        builder.setNeutralButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.setTitle(this.getResources().getString(R.string.menu_about));
        dialog.show();
    }
    
    private void viewDays(){
//        Intent i = new Intent(this, DayListActivity.class);
        Intent i = new Intent(this, lCalendarActivity.class);
        startActivityForResult(i, DAY_DELETE);
    }
    
    private void exportCsv(){
//      Intent i = new Intent(this, DayListActivity.class);
      Intent i = new Intent(this, CSVExporter.class);
      startActivity(i);
  }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_HOUR_ID, 0, R.string.menu_edit_hour);
        menu.add(0, DELETE_HOUR_ID, 1, R.string.menu_delete_hour);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_HOUR_ID:

                Calendar cal = Calendar.getInstance();
                String day = DateUtil.formataDia(cal);
                long idDay = mDayDbAdapter.selectDay(day);
                
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mHourDbAdapter.deleteHourDay(idDay, info.id);
                Cursor daysCursor = mHourDbAdapter.fetchHoursByDay(idDay);
                startManagingCursor(daysCursor);
                if(!daysCursor.moveToFirst()){
                    mDayDbAdapter.deleteDay(idDay);
                }
                fillData();
                return true;
            case EDIT_HOUR_ID:
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
                
                showDialog(EDIT_HOUR_ID);
                
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case EDIT_HOUR_ID:
            return new TimePickerDialog(this,
                    mTimeSetListener, new Integer(mHour), new Integer(mMinute), true);
        }
        return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case EDIT_HOUR_ID:
            TimePickerDialog timeDialog = (TimePickerDialog) dialog;
            timeDialog.updateTime(new Integer(mHour), new Integer(mMinute));
        }
    }
    
    
    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar cal = Calendar.getInstance();
                String day = DateUtil.formataDia(cal);
                long idDay = mDayDbAdapter.selectDay(day);
                
                String hour = DateUtil.formataHora(hourOfDay, minute);
                long idHour = mHourDbAdapter.selectHour(idDay, hour);
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
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
}
