package br.com.mlcsys.lappoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class lCalendarActivity extends Activity {
    
    private DatabaseHelper mDbHelper;
    private DayDbAdapter mDayDbAdapter;
    
    private SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
    private Calendar calendar = Calendar.getInstance();
    
    private static final int HOUR_DELETE = 0;
    
    private int monthView;
    private int yearView;
    private Map<Integer, Long> mapDays = new HashMap<Integer, Long>();
    
    private GridView gridview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcalendar_main);
        
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.open();
        mDayDbAdapter = new DayDbAdapter(mDbHelper.getmDb());
        
        Button previousMonth = (Button) findViewById(R.id.previousMonth);
        previousMonth.setText("<<");
        
        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMonthButtonListener();
            }
        });
        
        Button nextMonth = (Button) findViewById(R.id.nextMonth);
        nextMonth.setText(">>");
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonthButtonListener();
            }
        });

        GridView weekDaysView = (GridView) findViewById(R.id.weekDaysView);
        weekDaysView.setAdapter(new WeekDaysAdapter(this));

        gridview = (GridView) findViewById(R.id.gridview);
        monthView = calendar.get(Calendar.MONTH);
        yearView = calendar.get(Calendar.YEAR);
        gridview.setAdapter(new CalendarAdapter(this, monthView, yearView));
        
//        gridview.setOnTouchListener(new View.OnTouchListener() {
//            
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getEdgeFlags() == MotionEvent.EDGE_LEFT){
//                    previousMonthButtonListener();
//                }else if(event.getEdgeFlags() == MotionEvent.EDGE_RIGHT){
//                    nextMonthButtonListener();
//                }
//                return false;
//            }
//        });
        
        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                TextView textView = (TextView) view;
                int day = Integer.parseInt(textView.getText().toString());
                ColorStateList colors = textView.getTextColors();
                if(colors.getDefaultColor() == Color.DKGRAY){
                    if(day > 20){
                        previousMonthButtonListener();
                    } else {
                        nextMonthButtonListener();
                    }                    
                }else{
                    if(mapDays.containsKey(day)){
                        deleteHour(mapDays.get(day));
                    }else{
                        Toast.makeText(lCalendarActivity.this, R.string.no_appointments_day, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                TextView textView = (TextView) view;
                ColorStateList colors = textView.getTextColors();
                if(colors.getDefaultColor() == Color.GREEN){
                    Toast.makeText(lCalendarActivity.this, R.string.appointed_day, Toast.LENGTH_SHORT).show();
                } else if(colors.getDefaultColor() == Color.YELLOW){
                    Toast.makeText(lCalendarActivity.this, R.string.today_appointed_day, Toast.LENGTH_SHORT).show();
                } else if(colors.getDefaultColor() == Color.CYAN){
                    Toast.makeText(lCalendarActivity.this, R.string.today, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }
    
    public void nextMonthButtonListener(){
        
        if(monthView == 11){
            monthView = 0;
            yearView++;
        }else{
            monthView++;
        }
        
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new CalendarAdapter(this, monthView, yearView));
    }
    
    public void previousMonthButtonListener(){
        
        if(monthView == 0){
            monthView = 11;
            yearView--;
        }else{
            monthView--;
        }
        
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new CalendarAdapter(this, monthView, yearView));
    }

    public class CalendarAdapter extends BaseAdapter {

        private Context mContext;

        private int daysInCurrentView = 0; // days of previous month + days in
                                           // current month + days in next month

        private int firstDayOfMonthView = 1;
        private int lastDayOfMonthView;

        private int daysPreviousMonth;
        private int daysNextMonth;
        
        private Map<Integer, Integer> mapPreviousDays = new TreeMap<Integer, Integer>();
        int countingPreviousDays = -1;

        private CalendarAdapter(Context mContext, int month, int year) {
            this.mContext = mContext;
            
            passedPositionZero = false;
            
            TextView currentMonthYear = (TextView) findViewById(R.id.currentMonthYear);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, firstDayOfMonthView);
            String monthYearView = sdf.format(cal.getTime());
            monthYearView = monthYearView.toUpperCase().charAt(0) + monthYearView.substring(1);
            currentMonthYear.setText(monthYearView);

            daysInCurrentView = getDaysInCurrentView(month, year);
            lastDayOfMonthView = getNumberOfDaysInMonth();
            
            getAppointedDaysByMonth();
            
            setNumberOfDaysInPreviousMonth();

        }

        public int getDaysInCurrentView(int month, int year) {
            int result = getNumberOfDaysInMonth();

            Calendar cal = Calendar.getInstance();
            // seta o dia 1 do mes da view e pega o dia da semana
            cal.set(year, month, 1);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek > 1 && dayOfWeek <= 7) {
                daysPreviousMonth = (dayOfWeek -1);
                result += daysPreviousMonth;
                
            } 
            comecarAEscreverNaPosicao = dayOfWeek;

            // seta o ultimo dia do mes da view e pega o dia da semana
            cal.set(year, month, getNumberOfDaysInMonth());
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            // Se acabar na quarta, ainda tem q colocar quinta, sex e sab
            if (dayOfWeek <= 6) {
                daysNextMonth = (7 - dayOfWeek);
                result += daysNextMonth;
            }

            return result;
        }
        
        private void setNumberOfDaysInPreviousMonth(){
            int previousMonth;
            int previousYear;
            if(monthView == 0){
                previousMonth = 12;
                previousYear = yearView - 1;
            }else{
                previousMonth = monthView - 1;
                previousYear = yearView;
            }
            
            Calendar cal = Calendar.getInstance();
            cal.set(previousYear, previousMonth, 1);
            
            int previousMonthDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            for(int i = previousMonthDays; i > previousMonthDays - daysPreviousMonth ; i--){
                mapPreviousDays.put(++countingPreviousDays,i);
            }
        }
        
        private int getPreviousDay(){
            if(countingPreviousDays < 0){
                return -1;
            }
            return mapPreviousDays.remove(countingPreviousDays--);
        }
        
        
        private int getNumberOfDaysInMonth() {
            Calendar cal = Calendar.getInstance();
            cal.set(yearView, monthView, 1);
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        
        private void getAppointedDaysByMonth(){
            mapDays = new HashMap<Integer, Long>();
            Cursor c = mDayDbAdapter.fetchDaysByMonthYear(monthView, yearView);
            startManagingCursor(c);
            
            if (c != null) {
                while(c.moveToNext()){
                    Long idDay = c.getLong(0);
                    Integer day = Integer.parseInt(c.getString(1).substring(8, 10));
                    mapDays.put(day, idDay);
                }
            }
        }

        @Override
        public int getCount() {
            return daysInCurrentView;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }
        
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        private int currentDay = 1;

        private int comecarAEscreverNaPosicao = 0;
        
        private boolean passedPositionZero = false;
        
        private boolean diasEmSegundoPlano = false;
        

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Position 0 is called twice - error

            TextView view = new TextView(mContext);

            if (position >= (comecarAEscreverNaPosicao - 1)) {

                if (currentDay > lastDayOfMonthView) {
                    currentDay = 1;
                    diasEmSegundoPlano = true;
                }

                if (convertView == null) {
                    view.setText("" + (currentDay));
                    view.setGravity(Gravity.RIGHT);
                    view.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    view = (TextView) convertView;
                    view.setText("" + (currentDay));
                    view.setGravity(Gravity.RIGHT);
                    view.setTypeface(Typeface.DEFAULT_BOLD);
                }
                
                view.setTextSize(25);
                
                if(monthView == calendar.get(Calendar.MONTH) && yearView == calendar.get(Calendar.YEAR) && currentDay == calendar.get(Calendar.DAY_OF_MONTH)){
                    if(mapDays.containsKey(currentDay)){
                        view.setTextColor(Color.YELLOW);
                    }else{
                        view.setTextColor(Color.CYAN);
                    }
                }else{
                    if(mapDays.containsKey(currentDay)){
                        view.setTextColor(Color.GREEN);
                    }
                }
                
                if(diasEmSegundoPlano){
                    view.setTextColor(Color.DKGRAY);
                }
                
                if(passedPositionZero){
                    currentDay++;
                }
                
            } else {
                
                if(passedPositionZero){
                    int previousDay = getPreviousDay();
                    if(previousDay > 0){
                        view.setText("" + previousDay);
                        view.setTextColor(Color.DKGRAY);
                        view.setTextSize(25);
                        view.setGravity(Gravity.RIGHT);
                        view.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                }
                
            }
            
            //Position 0 is called twice - error
            if(position == 0){
                passedPositionZero = true;
            }

            return view;
        }
    }

    private void deleteHour(long idDay){
        Intent i = new Intent(this, HourListActivity.class);
        i.putExtra(HourDbAdapter.ID_DAY, idDay);
        startActivityForResult(i, HOUR_DELETE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        gridview.setAdapter(new CalendarAdapter(this, monthView, yearView));
//        ((CalendarAdapter) gridview.getAdapter()).notifyDataSetChanged();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    public class WeekDaysAdapter extends BaseAdapter {

        private Context mContext;
        private String[] weekdays;

        private WeekDaysAdapter(Context mContext) {
            this.mContext = mContext;
            this.weekdays = getDaysOfWeek();
        }

        @Override
        public int getCount() {
            return weekdays.length;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = new TextView(mContext);
            } else {
                view = (TextView) convertView;
            }
            view.setText(weekdays[position]);
            view.setGravity(Gravity.RIGHT);
            view.setTypeface(Typeface.DEFAULT_BOLD);            
            view.setTextSize(16);
            return view;
        }
        
        private String[] getDaysOfWeek(){
            String[] daysOfWeek = new String[7];
            daysOfWeek[0] = mContext.getResources().getString(R.string.sunday);
            daysOfWeek[1] = mContext.getResources().getString(R.string.monday);
            daysOfWeek[2] = mContext.getResources().getString(R.string.tuesday);
            daysOfWeek[3] = mContext.getResources().getString(R.string.wednesday);
            daysOfWeek[4] = mContext.getResources().getString(R.string.thursday);
            daysOfWeek[5] = mContext.getResources().getString(R.string.friday);
            daysOfWeek[6] = mContext.getResources().getString(R.string.saturday);
            return daysOfWeek;
        }

    }
}