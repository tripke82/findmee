package com.tripmee.findmee.Services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.LocationUtility;

/**
 * Created by Jerry on 1/5/2018.
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {

    private final static String SCREEN_TOGGLE_TAG = "SCREEN_TOGGLE_TAG";
    private static final int TRIGGER_EMERGENCY = 3; // number of clicks it needs to trigger findmee request
    private static final int POWER_OFF_TIMEOUT = 2000;
    private Handler handler = new Handler();
    private Runnable powerOffCounterReset = new PowerOfTimeoutReset();

    static int countPowerButtonClicks = 0;
    private Activity activity = null;
    private Context _Context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        _Context = context;
        countPowerButtonClicks++;

        if(Intent.ACTION_SCREEN_OFF.equals(action))
        {
            Log.d(SCREEN_TOGGLE_TAG, "Screen is turn off.");
        }else if(Intent.ACTION_SCREEN_ON.equals(action))
        {
            Log.d(SCREEN_TOGGLE_TAG, "Screen is turn on.");
        }

        if(countPowerButtonClicks == TRIGGER_EMERGENCY) {

            Log.d(SCREEN_TOGGLE_TAG, "Emergency Button Clicked 1");
            UserGlobalHandler UserHandler = UserGlobalHandler.get_instance();

            if (UserHandler.get_CurrentLocation()==null) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                Location l = LocationUtility.get_CurrentLocation(_Context, _Context);
                Toast.makeText(_Context, "Location Found", Toast.LENGTH_SHORT).show();
                Log.v("SCREEN_TOGGLE_TAG", "Location is found");
                UserHandler.setCurrentLocation(l);
                SendEmergencyRequest(UserHandler);
            }else{
                Log.v("SCREEN_TOGGLE_TAG", "Location is not null");
                SendEmergencyRequest(UserHandler);
            }



            //Log.v("countPowerButtonClicks", "Emergency button is pressed.");
            //Toast.makeText(context, _Context.getString(R.string.title_emergency_request), Toast.LENGTH_LONG).show();

            countPowerButtonClicks = 0;
            canclePowerOffTimeout();
        } else {
            resetPowerOffTimeout();
        }
    }

    private void SendEmergencyRequest(UserGlobalHandler UserHandler){

        if (UserHandler.get_CurrentLocation()!=null) {

            UserHandler.RequestEmergency();
            String msg = _Context.getString(R.string.emergency_message_sent) + ". Your location: Latitude:"
                    + UserHandler.get_CurrentLocation().getLatitude() + " Longitude:" + UserHandler.get_CurrentLocation().getLongitude();
            Toast.makeText(_Context, msg, Toast.LENGTH_SHORT).show();

            Intent newIntent = new Intent();
            newIntent.putExtra(GlobalConstants.EMERGENCY_REQUEST, msg);
            newIntent.setClassName("com.paranoidcompany.sam.findmee", "com.paranoidcompany.sam.findmee.MainActivity");
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            _Context.startActivity(newIntent); // start the app main activity
        }
    }

    private void resetPowerOffTimeout() {
        canclePowerOffTimeout();
        startPowerOffTimeout();
    }

    private void startPowerOffTimeout() {
        //Toast.makeText(_Context, "Power Time out start", Toast.LENGTH_SHORT).show();
        handler.postDelayed( powerOffCounterReset, POWER_OFF_TIMEOUT );
    }

    private void canclePowerOffTimeout() {
        handler.removeCallbacks( powerOffCounterReset );
    }

    private class PowerOfTimeoutReset implements Runnable {

        public void run() {
            //countPowerOff = 0;
            countPowerButtonClicks = 0;
            //Toast.makeText(_Context, "Power Time out reset", Toast.LENGTH_SHORT).show();
        }

    }
}