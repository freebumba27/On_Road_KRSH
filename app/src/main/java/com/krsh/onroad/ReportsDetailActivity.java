package com.krsh.onroad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RatingBar;
import android.widget.TextView;

import com.krsh.onroad.utils.ReusableClass;

public class ReportsDetailActivity extends AppCompatActivity {

    String sessionId = "nothing";
    String whichActivity = "nothing";

    TextView textViewTopSpeedValue;
    TextView textViewAvgSpeedValue;
    TextView textViewDistanceCoveredValue;
    TextView textViewSuddenBrakeValue;
    TextView textViewSpeedCurveCountValue;
    TextView textViewOverSpeedingDurationValue;

    double overSpeedingTime = 0;
    double totalDurationTime = 0;
    double totalSuddenBreak = 0;
    double totalDistanceCovered = 0;
    double totalHighSpeedCurved = 0;
    RatingBar driverRatingBar;

    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_detail);

        sessionId = getIntent().getStringExtra("sessionId");
        whichActivity = getIntent().getStringExtra("whichPage");
        progress = new ProgressDialog(this);

        textViewTopSpeedValue               = (TextView)findViewById(R.id.textViewTopSpeedValue);
        textViewAvgSpeedValue               = (TextView)findViewById(R.id.textViewAvgSpeedValue);
        textViewDistanceCoveredValue        = (TextView)findViewById(R.id.textViewDistanceCoveredValue);
        textViewSuddenBrakeValue            = (TextView)findViewById(R.id.textViewSuddenBrakeCountValue);
        textViewSpeedCurveCountValue        = (TextView)findViewById(R.id.textViewSpeedCurveCountValue);
        textViewOverSpeedingDurationValue   = (TextView)findViewById(R.id.textViewOverSpeedingDurationValue);
        driverRatingBar                     = (RatingBar)findViewById(R.id.driverRatingBar);

        progress.setTitle("Loading");
        progress.setMessage("Wait preparing your report ...");
        progress.show();
        preparingReports();
    }

    private void preparingReports()
    {
        SQLiteDatabase db = ReusableClass.createAndOpenDb(this);
        //----------------------------------------------
        // sudden break count
        //----------------------------------------------

        Cursor cur = db.rawQuery("select count(t.Id) from(SELECT d1.id, d1.speed, d1.session_id , " +
                "( select d2.speed from log_details d2 where d2.date_time_milli_sec<d1.date_time_milli_sec AND session_id=" + sessionId.substring(15) +
                " order by date_time_milli_sec desc LIMIT 1) prevspeed  FROM log_details d1 ) t where t.prevspeed-t.speed >"+ ReusableClass.suddenBrakeLimit +" AND session_id=" + sessionId.substring(15), null);

        if (cur.moveToFirst())
        {
            do
            {
                textViewSuddenBrakeValue.setText(cur.getString(0));
                totalSuddenBreak = Float.parseFloat(cur.getString(0));
            }while (cur.moveToNext());
        }

        //----------------------------------------------
        // sudden break count
        //----------------------------------------------

        //----------------------------------------------
        // high speed curve count
        //----------------------------------------------

        Cursor cur3 = db.rawQuery("SELECT count(id) FROM log_details where (diff_curve_value>.5 and diff_curve_value<.75 and speed > 30 ) " +
                "or (diff_curve_value>.75 and speed > 20 ) AND session_id=" + sessionId.substring(15), null);

        if (cur3.moveToFirst())
        {
            do
            {
                textViewSpeedCurveCountValue.setText(cur3.getString(0));
                totalHighSpeedCurved = Float.parseFloat(cur3.getString(0));
            }while (cur3.moveToNext());
        }

        //----------------------------------------------
        // high speed curve count
        //----------------------------------------------

        Cursor cur1 = db.rawQuery("SELECT MAX(speed), AVG(speed), SUM(distance) FROM log_details WHERE session_id=" + sessionId.substring(15), null);
        if (cur1.moveToFirst())
        {
            do
            {
                if(cur1.getString(0)!=null)
                    textViewTopSpeedValue.setText(cur1.getString(0) + " km/h" );
                if(cur1.getString(1)!=null)
                    textViewAvgSpeedValue.setText(cur1.getString(1) + " km/h");
                if(cur1.getString(2)!=null)
                    textViewDistanceCoveredValue.setText(Float.parseFloat(cur1.getString(2))/1000 + " km");
                if(cur1.getString(2)!=null)
                    totalDistanceCovered = Float.parseFloat(cur1.getString(2))/1000;
            }while (cur1.moveToNext());
        }

        //----------------------------------------------
        // Get all the session name
        //----------------------------------------------

        //----------------------------------------------
        // over speeding count
        //----------------------------------------------

        Cursor cur4 = db.rawQuery("SELECT count(id) * " + ReusableClass.timeDiff + " FROM log_details WHERE speed > " + ReusableClass.overSpeedingLimit + " AND session_id=" + sessionId.substring(15), null);
        if (cur4.moveToFirst())
        {
            do
            {
                overSpeedingTime = Float.parseFloat(cur4.getString(0));
                textViewOverSpeedingDurationValue.setText(Float.parseFloat(cur4.getString(0))/60 + " min" );
            }while (cur4.moveToNext());
        }

        //----------------------------------------------
        // over speeding count
        //----------------------------------------------

        //----------------------------------------------
        // Star rating
        //----------------------------------------------

        Cursor cur5 = db.rawQuery("SELECT count(id) * " + ReusableClass.timeDiff + " FROM log_details WHERE session_id=" + sessionId.substring(15), null);
        if (cur5.moveToFirst())
        {
            do
            {
                totalDurationTime = Float.parseFloat(cur5.getString(0));
            }while (cur5.moveToNext());
        }

        double suddenBrackPoints    = (totalSuddenBreak*1)/totalDistanceCovered;
        double highSpeedCurvePoints = (totalHighSpeedCurved*2)/totalDistanceCovered;
        double maxValue = 33;
        double totalDeduction = (overSpeedingTime *100)/totalDurationTime + ((suddenBrackPoints > maxValue) ? maxValue : suddenBrackPoints) +
                ((highSpeedCurvePoints > maxValue) ? maxValue : highSpeedCurvePoints);

        double driverPoint = 100 - totalDeduction;

        if(driverPoint >= 90 && driverPoint <=100)
            driverRatingBar.setRating(5);
        else if(driverPoint >= 70 && driverPoint <90)
            driverRatingBar.setRating(4);
        else if(driverPoint >= 40 && driverPoint <70)
            driverRatingBar.setRating(3);
        else if(driverPoint >= 10 && driverPoint <40)
            driverRatingBar.setRating(2);
        if(driverPoint <10)
            driverRatingBar.setRating(1);


        //----------------------------------------------
        // Star rating
        //----------------------------------------------

        progress.dismiss();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, ShowLogActivity.class);
        i.putExtra("sessionId", sessionId);
        i.putExtra("whichPage","showingReports");
        finish();
        startActivity(i);
    }
}