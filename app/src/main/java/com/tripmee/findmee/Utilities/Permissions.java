package com.tripmee.findmee.Utilities;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Permissions {



    private static Boolean AllPermissionGranted = false;

    public final static int PERMISSION_ALL = 1;
    public final static String[] PERMISSIONS = {permission.ACCESS_COARSE_LOCATION, permission.SEND_SMS,
            permission.ACCESS_FINE_LOCATION, permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE,
            permission.CAMERA, permission.CALL_PHONE, permission.READ_CONTACTS};// permission.RECEIVE_BOOT_COMPLETED};

    //public final static String[] PERMISSIONS2 = {permission.RECEIVE_BOOT_COMPLETED};


    public static boolean IsPermissionGranted(Activity activity){

        AllPermissionGranted = false;

        if ((ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )){
            return false;
        }

        if (ContextCompat.checkSelfPermission(activity,ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){
            return false;
        }

        if(ContextCompat.checkSelfPermission(activity, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }

        if(ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }

        if(ContextCompat.checkSelfPermission(activity, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }

        if(ContextCompat.checkSelfPermission(activity, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }

        if(ContextCompat.checkSelfPermission(activity, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }

        //if(ContextCompat.checkSelfPermission(activity, RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            //return false;
        //}

        if(ContextCompat.checkSelfPermission(activity, SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            //Log.v("All Permission", " Granted");
            return false;
        }


        AllPermissionGranted = true;
        return  true;
    }

    public static Boolean getAllPermissionGranted() {
        return AllPermissionGranted;
    }
}
