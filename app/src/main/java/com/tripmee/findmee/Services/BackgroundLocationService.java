package com.tripmee.findmee.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Utilities.LocationUtility;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Utility;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by deepshikha on 24/11/16.
 */

public class BackgroundLocationService extends Service implements LocationListener{

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude,longitude;
    LocationManager _LocationManager;
    private Location _CurrentLocation;
    private Location _PreviousLocation;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 60000; // 60 seconds
    private UserGlobalHandler UserHandler;

    //public static String str_receiver = "servicetutorial.service.receiver";

    public static String str_receiver = "BackgroundLocationService";
    Intent intent;


    public BackgroundLocationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UserHandler = UserGlobalHandler.get_instance();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(),5,notify_interval);
        intent = new Intent(str_receiver);
//        fn_getlocation();
    }

    @Override
    public void onLocationChanged(Location location) {

        _CurrentLocation = location;
        //fn_update(location);
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

    @SuppressLint("MissingPermission")
    private void fn_getlocation(){
        _LocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = _LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = _LocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable){
            Utility.displayMessage(getApplicationContext(), "Please enable your GPS or Network");
        }else {

            if (isNetworkEnable){
                _CurrentLocation = null;

                _LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,this);


                if (_LocationManager!=null){

                    boolean b = Permissions.getAllPermissionGranted();

                    _CurrentLocation = _LocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    //Log.d("747356", "Location null 1");
                    if (_CurrentLocation!=null){


                        latitude = _CurrentLocation.getLatitude();
                        longitude = _CurrentLocation.getLongitude();

                        if(LocationUtility.IsLocationChanged(_CurrentLocation, UserHandler.get_CurrentLocation(), 0.1)) {

                            fn_update(_CurrentLocation);
                        }
                    }
                }

            }


            if (isGPSEnable){
                _CurrentLocation = null;
                _LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                if (_LocationManager!=null){
                    _CurrentLocation = _LocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //Log.d("747356", "Location null 1");
                    if (_CurrentLocation!=null){

                        //Log.e("latitude",_CurrentLocation.getLatitude()+"");
                        //Log.e("longitude",_CurrentLocation.getLongitude()+"");
                        latitude = _CurrentLocation.getLatitude();
                        longitude = _CurrentLocation.getLongitude();

                        if(LocationUtility.IsLocationChanged(_CurrentLocation, UserHandler.get_CurrentLocation(), 0.1)) {
                            //Log.e("GPS location:"," updated");
                            fn_update(_CurrentLocation);
                        }
                    }
                }
            }


        }

    }

    private class TimerTaskToGetLocation extends TimerTask{
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void fn_update(Location location){

        intent.putExtra("latutide",location.getLatitude()+"");
        intent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(intent);
    }


}
