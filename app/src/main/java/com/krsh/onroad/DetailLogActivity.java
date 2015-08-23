package com.krsh.onroad;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.krsh.onroad.utils.ReusableClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailLogActivity extends AppCompatActivity {

    ListView myListView;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    String sessionId = "nothing";
    String whichActivity = "nothing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_log);

        myListView = (ListView)findViewById(R.id.myListViewDetailLog);

        sessionId = getIntent().getStringExtra("sessionId");
        whichActivity = getIntent().getStringExtra("whichPage");

        if(!sessionId.equalsIgnoreCase("nothing"))
            populatingList();
    }

    private void populatingList()
    {
        //----------------------------------------------
        // Get all the Session Name
        //----------------------------------------------
        SQLiteDatabase db = ReusableClass.createAndOpenDb(this);

        Cursor cur = db.rawQuery("SELECT date_time_milli_sec, distance, speed, diff_curve_value FROM log_details WHERE session_id=" + sessionId.substring(15)+ " ORDER BY date_time_milli_sec DESC" , null);

        data.clear();
        if (cur.moveToFirst())
        {
            do
            {
                Map<String, String> datum = new HashMap<String, String>(2);
                datum.put("noOfSession", "Time: " + ReusableClass.getFormattedDateFromTimestamp(Long.parseLong(cur.getString(0))));
                datum.put("sessionStartedTime", "Distance Covered: " + cur.getString(1) + " meter\nSpeed: " + cur.getString(2) + " km/h" + "\nCurve Value: " + cur.getString(3));
                data.add(datum);
            }while (cur.moveToNext());
        }

        //----------------------------------------------
        // Get all the session name
        //----------------------------------------------

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.simplerow,
                new String[] {"noOfSession", "sessionStartedTime" },
                new int[] {R.id.rowTextView, R.id.rowTextView2 });

        myListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reports_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            populatingList();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, ShowLogActivity.class);
        i.putExtra("sessionId", sessionId);
        i.putExtra("whichPage","showingLog");
        finish();
        startActivity(i);
    }
}
