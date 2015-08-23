package com.krsh.onroad;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.krsh.onroad.utils.ReusableClass;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShowLogActivity extends AppCompatActivity {

    ListView myListView;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    String whichActivity = "nothing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_log);

        myListView = (ListView)findViewById(R.id.myListView);
        whichActivity = getIntent().getStringExtra("whichPage");

        //----------------------------------------------
        // Get all the Session Name
        //----------------------------------------------
        SQLiteDatabase db = ReusableClass.createAndOpenDb(this);

//        Cursor cur = db.rawQuery("SELECT id,date_time_mili_sec FROM session_master", null);
        Cursor cur = db.rawQuery("SELECT session_master.id, session_master.date_time_mili_sec," +
                " (SELECT lat FROM log_details WHERE session_id = session_master.id ORDER BY date_time_milli_sec ASC LIMIT 1) lat_from," +
                " (SELECT lng FROM log_details WHERE session_id = session_master.id ORDER BY date_time_milli_sec ASC LIMIT 1) lng_from," +
                " (SELECT lat FROM log_details WHERE session_id = session_master.id ORDER BY date_time_milli_sec DESC LIMIT 1) lat_to," +
                " (SELECT lng FROM log_details WHERE session_id = session_master.id ORDER BY date_time_milli_sec DESC LIMIT 1) lng_to FROM session_master", null);
        int sessionId = cur.getCount();

        Log.w("Tag", "no Of Session: " + sessionId);

        if (cur.moveToFirst())
        {
            do
            {
                String fromTo = "UNKNOWN";
                Log.d("TAG", "from_lat " + cur.getString(2));
                Log.d("TAG", "from_lan " + cur.getString(3));
                Log.d("TAG", "to_lat " + cur.getString(4));
                Log.d("TAG", "to_lan " + cur.getString(5));

                if(cur.getString(2)!=null)
                {
                    fromTo = getPlaceName(Double.parseDouble(cur.getString(2)), Double.parseDouble(cur.getString(3))) + " - " +
                            getPlaceName(Double.parseDouble(cur.getString(4)), Double.parseDouble(cur.getString(5)));


                    Date date = new Date();
                    date.setTime(Long.parseLong(cur.getString(1)));
                    String today=new SimpleDateFormat(" 20\nEEE").format(date);

                    date.setTime(Long.parseLong(cur.getString(1)));
                    String monthDate = new SimpleDateFormat("hh:mm:ss a").format(date);

                    Map<String, String> datum = new HashMap<String, String>(2);
                    datum.put("sessionId", "No of Session: " + cur.getString(0));
                    datum.put("sessionStartedTime", "Journey started at: " + monthDate);
                    datum.put("sessionDateDay", today);
                    datum.put("fromTo", fromTo);
                    data.add(datum);
                }
            }while (cur.moveToNext());
        }

        //----------------------------------------------
        // Get all the session name
        //----------------------------------------------

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.simplerow,
                new String[] {"fromTo", "sessionStartedTime", "sessionDateDay", "sessionId"},
                new int[] {R.id.rowTextView, R.id.rowTextView2, R.id.date});

        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Log.d("TAG", "value: " + data.get(position).get("sessionId"));
                Log.d("TAG", "value: " + data.get(position).get("sessionStartedTime"));

                if(whichActivity.equalsIgnoreCase("showingLog"))
                {
                    Intent i = new Intent(ShowLogActivity.this, DetailLogActivity.class);
                    i.putExtra("sessionId", data.get(position).get("sessionId"));
                    i.putExtra("whichPage",whichActivity);
                    finish();
                    startActivity(i);
                }
                else if(whichActivity.equalsIgnoreCase("showingReports"))
                {
                    Intent i = new Intent(ShowLogActivity.this, ReportsDetailActivity.class);
                    i.putExtra("sessionId", data.get(position).get("sessionId"));
                    i.putExtra("whichPage",whichActivity);
                    finish();
                    startActivity(i);
                }
            }
        });
    }

    private String getPlaceName(double MyLat, double MyLong)
    {
        String placeName = "Unknown";
        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);

            placeName = cityName;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return placeName;
    }

    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }
}
