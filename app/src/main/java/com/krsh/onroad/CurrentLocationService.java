package com.krsh.onroad;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.krsh.onroad.utils.ReusableClass;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class CurrentLocationService extends Service {
    double glat = 0;
    double glng = 0;


    LocationManager glocManager;
    LocationListener glocListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gettingCurrentLoc();
        return START_STICKY;
    }

    private void gettingCurrentLoc() {
        try {
            Log.w("Service_location", "Inside Location Service");

            glocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            glocListener = new MyLocationListenerGPS();
            glocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    ReusableClass.timeDiff,
                    0,
                    glocListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (glocManager != null) {
                glocManager.removeUpdates(glocListener);
                Log.d("ServiceForLatLng", "GPS Update Released");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public class MyLocationListenerGPS implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            glat = loc.getLatitude();
            glng = loc.getLongitude();

            Log.d("LAT & LNG GPS:", glat + " " + glng);

            String updatedTime = ReusableClass.getFromPreference("updated_time", CurrentLocationService.this);
            String currentMilliSec = String.valueOf(System.currentTimeMillis());
            String lastLat = ReusableClass.getFromPreference("glat", CurrentLocationService.this);
            String lastLng = ReusableClass.getFromPreference("glng", CurrentLocationService.this);
            String lastSpeed = ReusableClass.getFromPreference("lastSpeed", CurrentLocationService.this);
            String sessionId = ReusableClass.getFromPreference("session_id", CurrentLocationService.this);

            if (!updatedTime.equalsIgnoreCase("")) {
                Log.i("TAG", "Next Time onwards");
                long time_diff = Long.parseLong(currentMilliSec) - Long.parseLong(updatedTime);
                float distanceCovered = distFrom(Float.parseFloat(lastLat), Float.parseFloat(lastLng), Float.parseFloat(String.valueOf(glat)), Float.parseFloat(String.valueOf(glng)));

                if (time_diff != 0) {
//                    float speed = (distanceCovered / (time_diff / 1000)) * 18 / 5; // Km/Hours
//                    float speed = (distanceCovered / ReusableClass.timeDiff) * 18 / 5; // Km/Hours
                    float speed = loc.getSpeed() * 18 / 5; // Km/Hours

                    float speedDiff = Float.parseFloat(lastSpeed) - speed;
                    Log.i("TAG", "speedDiff: " + speedDiff);

                    String curveValue = curveFunction(glat, glng, Double.parseDouble(lastLat), Double.parseDouble(lastLng));
                    Float diffCurveValue = Float.parseFloat(ReusableClass.getFromPreference("lastCurveValue", CurrentLocationService.this)) - Float.parseFloat(curveValue);
                    Log.i("TAG", "Curve Value: " + diffCurveValue);

                    ReusableClass.saveInPreference("lastCurveValue", curveValue, CurrentLocationService.this);


                    //------------------------------------------------------
                    //Checking Accident
                    //------------------------------------------------------

                    if (speedDiff > ReusableClass.globalSpeedDiff && speed == 0) {
                        Log.i("TAG", "Accident Happened!!");
                        Toast.makeText(CurrentLocationService.this, "Accident Happened !!", Toast.LENGTH_SHORT).show();
                        Vibrator v = (Vibrator) CurrentLocationService.this.getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(1000 * 5); //2 Sec

                        if (ReusableClass.getFromPreference("emergencyMobileNo", CurrentLocationService.this).toString().trim().length() > 0)
                            sendSMS(ReusableClass.getFromPreference("emergencyMobileNo", CurrentLocationService.this), "I mate an accident  !! My location is  " + getPlaceName(glat, glng));
                        else
                            Toast.makeText(CurrentLocationService.this, "Please Enter a mobile no !!", Toast.LENGTH_SHORT).show();
                    }

                    //------------------------------------------------------
                    //Checking Accident
                    //------------------------------------------------------

                    ReusableClass.saveInPreference("glat", glat + "", CurrentLocationService.this);
                    ReusableClass.saveInPreference("glng", glng + "", CurrentLocationService.this);
                    ReusableClass.saveInPreference("updated_time", currentMilliSec + "", CurrentLocationService.this);
                    ReusableClass.saveInPreference("lastSpeed", speed + "", CurrentLocationService.this);

                    updatingLogDetailsDb(currentMilliSec, String.valueOf(distanceCovered), String.valueOf(glat), String.valueOf(glng), String.valueOf(new DecimalFormat("##.#######").format(speed)), sessionId, String.valueOf(diffCurveValue));
                }
            } else {
                Log.i("TAG", "First Time");
                ReusableClass.saveInPreference("glat", glat + "", CurrentLocationService.this);
                ReusableClass.saveInPreference("glng", glng + "", CurrentLocationService.this);
                long time = System.currentTimeMillis();
                ReusableClass.saveInPreference("updated_time", time + "", CurrentLocationService.this);

                ReusableClass.saveInPreference("lastSpeed", "0", CurrentLocationService.this);
                ReusableClass.saveInPreference("lastCurveValue", "0", CurrentLocationService.this);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("LOG", "GPS is OFF!");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("LOG", "Thanks for enabling GPS !");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    //----------------------------------------------
    // inserting to local database
    //----------------------------------------------

    private void updatingLogDetailsDb(String currentMilliSec, String distance, String lat, String lng, String speed, String sessionId, String diffCurveValue) {
        SQLiteDatabase db = ReusableClass.createAndOpenDb(this);

        ContentValues values = new ContentValues();
        values.put("date_time_milli_sec", currentMilliSec);
        values.put("distance", distance);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("speed", speed);
        values.put("session_id", sessionId);
        values.put("diff_curve_value", diffCurveValue);
        db.insert("log_details", null, values);
        db.close();
    }

    //----------------------------------------------
    // inserting to local database
    //----------------------------------------------


    //----------------------------------------------
    // Calculation for curve
    //----------------------------------------------

    public static String curveFunction(double latA, double lonA, double latB, double lonB) {
        double lat1 = Math.tan(((latB / 2) + (Math.PI / 4)));
        double lat2 = Math.tan((latA / 2 + Math.PI / 4));
        double latDiff = 0;
        if (lat2 != 0) {
            latDiff = Math.log(lat1 / lat2);
        }
        double longDiff = Math.abs(lonA - lonB);
        String bearingval = new DecimalFormat("##.##").format(Math.atan2(longDiff, latDiff));
        Log.i("TAG", "bearingval- " + bearingval);
        return bearingval;
    }
    //----------------------------------------------
    // Calculation for curve
    //----------------------------------------------

    //----------------------------------------------
    // Send sms
    //----------------------------------------------

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getBaseContext(), "SMS sent",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        //Toast.makeText(getBaseContext(), "Generic failure",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        //Toast.makeText(getBaseContext(), "No service",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        //Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        //Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Toast.makeText(getBaseContext(), "SMS not delivered",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    //----------------------------------------------
    // Send sms
    //----------------------------------------------

    //----------------------------------------------
    // Get Place Name from Lat Lng
    //----------------------------------------------

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

    //----------------------------------------------
    // Get Place Name from Lat Lng
    //----------------------------------------------



}

