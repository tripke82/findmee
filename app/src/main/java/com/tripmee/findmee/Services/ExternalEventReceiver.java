package com.tripmee.findmee.Services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.R;

import java.util.Locale;

public class ExternalEventReceiver extends BroadcastReceiver {

    private static final int TRIGGER_EMERGENCY = 3; // number of clicks it needs to trigger findmee request
    private static final int POWER_OFF_TIMEOUT = 2000;
    private Handler handler = new Handler();
    private Runnable powerOffCounterReset = new PowerOfTimeoutReset();

    //static int countPowerOff = 0;
    static int countPowerButtonClicks = 0;
    private Activity activity = null;
    private Context _Context;

    public static String str_receiver = "ExternalEventReceiver";

    public ExternalEventReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        _Context = context;
        countPowerButtonClicks++;

        /*if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            countPowerOff++;
        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) { }

        if(countPowerOff > 3) {
            //record insert
            countPowerOff = 0;
            canclePowerOffTimeout();
        } else {
            resetPowerOffTimeout();
        }*/


        if(countPowerButtonClicks == TRIGGER_EMERGENCY) {

            UserGlobalHandler UserHandler = UserGlobalHandler.get_instance();
            if (UserHandler.get_CurrentLocation()!=null) {

                UserHandler.RequestEmergency();
                String msg = activity.getString(R.string.emergency_message_sent) + ". Your location: Latitude:"
                        + UserHandler.get_CurrentLocation().getLatitude() + " Longitude:" + UserHandler.get_CurrentLocation().getLongitude();
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

                Intent newIntent = new Intent();
                newIntent.putExtra(GlobalConstants.EMERGENCY_REQUEST, msg);
                newIntent.setClassName("com.paranoidcompany.sam.findmee", "com.paranoidcompany.sam.findmee.MainActivity");
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(newIntent); // start the app main activity
            }

            //Log.v("countPowerButtonClicks", "Emergency button is pressed.");
            //Toast.makeText(context, _Context.getString(R.string.title_emergency_request), Toast.LENGTH_LONG).show();

            countPowerButtonClicks = 0;
            canclePowerOffTimeout();
        } else {
            resetPowerOffTimeout();
        }

        //Log.v("onReceive", "Power button is pressed.");
        //getSpeechInput();

    }

    public void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(_Context.getPackageManager()) != null) {
            activity.startActivityForResult(intent, 10);
        } else {
            Toast.makeText(_Context, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
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