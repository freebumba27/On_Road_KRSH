package com.krsh.onroad;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.krsh.onroad.utils.ReusableClass;
/**
 * Created by Dream on 22-Aug-15.
 */
public class MainActivity extends AppCompatActivity {

    TextView startButtonCaption;
    ImageView ImageViewOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButtonCaption = (TextView)findViewById(R.id.startButtonCaption);
        ImageViewOnOff     = (ImageView)findViewById(R.id.ImageViewOnOff);

        if(ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase("off") || ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase(""))
        {
            ImageViewOnOff.setImageResource(R.drawable.on);
            startButtonCaption.setText("START");
        }
        else if(ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase("on"))
        {
            ImageViewOnOff.setImageResource(R.drawable.off);
            startButtonCaption.setText("STOP");
        }
        //((TextView)findViewById(R.id.textViewSpeedDiff)).setText("Speed difference for accidence alert: " + ReusableClass.globalSpeedDiff + "km/h");
    }

    public void startingAndStopTracking(View view)
    {
        if(ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase("off") || ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase(""))
        {
            ImageViewOnOff.setImageResource(R.drawable.off);
            startButtonCaption.setText("STOP");
            ReusableClass.saveInPreference("onOffFlag", "on", MainActivity.this);

            long id = updatingLogSession();

            ReusableClass.saveInPreference("updated_time", "", this);
            ReusableClass.saveInPreference("glat", "", this);
            ReusableClass.saveInPreference("glng", "", this);
            ReusableClass.saveInPreference("lastSpeed", "", this);
            ReusableClass.saveInPreference("session_id", String.valueOf(id), this);

            Intent i = new Intent(this, CurrentLocationService.class);
            startService(i);
        }
        else if(ReusableClass.getFromPreference("onOffFlag", MainActivity.this).equalsIgnoreCase("on"))
        {
            ImageViewOnOff.setImageResource(R.drawable.on);
            startButtonCaption.setText("START");
            ReusableClass.saveInPreference("onOffFlag", "off", MainActivity.this);
            stopService(new Intent(this, CurrentLocationService.class));
        }
    }

    private long updatingLogSession()
    {
        //----------------------------------------------
        // inserting to local database
        //----------------------------------------------
        SQLiteDatabase db = ReusableClass.createAndOpenDb(this);

        ContentValues values = new ContentValues();
        values.put("date_time_mili_sec", String.valueOf(System.currentTimeMillis()));
        long id = db.insert("session_master", null, values);
        db.close();

        //----------------------------------------------
        // inserting to local database
        //----------------------------------------------
        return id;
    }

    public void stopTracking(View view) {
        stopService(new Intent(this, CurrentLocationService.class));
    }


    public void showingLog(View view)
    {
        Intent i = new Intent(this, ShowLogActivity.class);
        i.putExtra("whichPage","showingLog");
        startActivity(i);
        finish();
    }

    public void showingReports(View view)
    {
        Intent i = new Intent(this, ShowLogActivity.class);
        i.putExtra("whichPage","showingReports");
        startActivity(i);
        finish();
    }

    public void showingSettings(View view)
    {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        finish();
    }
}
