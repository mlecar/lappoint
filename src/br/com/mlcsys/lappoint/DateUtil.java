package br.com.mlcsys.lappoint;

import java.util.Calendar;

public class DateUtil {
    
    
    public static String formataDia(Calendar cal){
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        
        return year + "-" + month + "-" + day;
    }
    
    
    
    public static String formataHora(Calendar cal){
        String hour = String.format("%02d", cal.getTime().getHours());
        String minutes = String.format("%02d", cal.getTime().getMinutes());
        
        return hour + ":" + minutes;
    }
    
    public static String formataHora(int ihour, int iminute){
        String hour = String.format("%02d", ihour);
        String minutes = String.format("%02d", iminute);
        
        return hour + ":" + minutes;
    }

}
