package br.com.mlcsys.lappoint;

import android.app.ListActivity;
import android.content.Intent;
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

public class DayListActivity extends ListActivity {
    
    private DatabaseHelper mDbHelper;
    private DayDbAdapter mDayDbAdapter;
    private HourDbAdapter mHourDbAdapter;
    private ListView list = null;
    private static final int DELETE_DAY_ID = Menu.FIRST;
    private static final int HOUR_DELETE = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.day_list);
            mDbHelper = new DatabaseHelper(this);
            mDbHelper.open();
            mDayDbAdapter = new DayDbAdapter(mDbHelper.getmDb());
            mHourDbAdapter = new HourDbAdapter(mDbHelper.getmDb());
            list = (ListView) findViewById(android.R.id.list);
            registerForContextMenu(list);
            fillData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void fillData(){

        Cursor c = mDayDbAdapter.fetchAllDays();
        startManagingCursor(c);
        
        String[] from = new String[]{DayDbAdapter.DAY_COLUMN};
        int[] to = new int[]{R.id.day};
        
        SimpleCursorAdapter days = 
            new SimpleCursorAdapter(this, R.layout.day_row, c, from, to);
        setListAdapter(days);
        
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_DAY_ID, 0, R.string.menu_delete_day);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_DAY_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mHourDbAdapter.deleteDayHours(info.id);
                mDayDbAdapter.deleteDay(info.id);
                fillData();
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    private void deleteHour(long idDay){
        Intent i = new Intent(this, HourListActivity.class);
        i.putExtra(HourDbAdapter.ID_DAY, idDay);
        startActivityForResult(i, HOUR_DELETE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long idDay) {
        deleteHour(idDay);
        fillData();
    }
    
    

}
