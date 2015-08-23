package com.krsh.onroad.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dream on 22-Aug-15.
 */
public class ReusableClass {

    public static float globalSpeedDiff = 30;
    public static int timeDiff = 2;
    public static int overSpeedingLimit = 30;
    public static int suddenBrakeLimit = 30;
    //===================================================================================================================================
    //Preference variable
    //===================================================================================================================================

    //--------------------------------------------
    // method to save variable in preference
    //--------------------------------------------
    public static void saveInPreference(String name, String content, Context myActivity) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(myActivity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, content);
        editor.commit();
    }

    //--------------------------------------------
    // getting content from preferences
    //--------------------------------------------
    public static String getFromPreference(String variable_name, Context myActivity) {
        String preference_return;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(myActivity);
        preference_return = preferences.getString(variable_name, "");

        return preference_return;
    }



    //====================================================================================================================================
    //DataBase table crate and open
    //====================================================================================================================================

    public static SQLiteDatabase createAndOpenDb(Context con)
    {
        //----------------------------------------
        // Create the database
        //----------------------------------------

        DataBaseHelper myDbHelper = new DataBaseHelper(con);

        try
        {
            myDbHelper.createDataBase();
            Log.d("DB Log", "Database Created");
        }
        catch (IOException ioe)
        {
            Log.d("DB Log","Unable to create database Error: " + ioe + "\n");
        }

        //----------------------------------------
        //----------------------------------------


        //----------------------------------------
        // Open the database
        //----------------------------------------
        try
        {
            myDbHelper.openDataBase();
            Log.d("DB Log", "Database Opened");

        }
        catch (Throwable e)
        {
            Log.d("TAG", "catch " + e);
        }

        // Get the readable version
        return myDbHelper.getReadableDatabase();

        //----------------------------------------
        //----------------------------------------
    }
    //====================================================================================================================================
    //DataBase table crate and open
    //====================================================================================================================================

    public static String getFormattedDateFromTimestamp(long timestampInMilliSeconds)
    {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate=new SimpleDateFormat("dd'/'MM'/'yyyy HH:mm:ss").format(date);
        return formattedDate;

    }
}
