package br.com.mlcsys.lappoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class CSVExporter extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            
            String state = Environment.getExternalStorageState();
            if(Environment.MEDIA_MOUNTED.equals(state)){
                Log.d("MEDIA MOUNTED", "media is mounted");
            }
            
            
            File sdCard = Environment.getExternalStorageDirectory();//getFilesDir();
            File file = new File("lappoint.csv");
            file.createNewFile();
            if(file.mkdirs()){
                FileWriter writer = new FileWriter(sdCard + "/lappoint.csv");
                BufferedWriter out = new  BufferedWriter(writer);
                out.write("Alow!");
                out.close();
            }
            
//            FileOutputStream file = openFileOutput("lappoint.csv", MODE_WORLD_WRITEABLE);
//            file.write(new String().getBytes());
            finish();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
