package com.tripmee.findmee.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;

public class Phone {

    public static void SendSMS(String msg, String MobileNumber) {

        try{
            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage(MobileNumber,null,msg,null,null);
            Log.e("success", "ok");
        }
        catch (Exception e){
            Log.e("error", e.getMessage());
        }

    }

    public static void LunchSMSActivity(Activity activity, String Message, String PhoneNumber){

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + PhoneNumber));
        sendIntent.putExtra("sms_body", Message);
       //sendIntent.setType("vnd.android-dir/mms-sms");
        //sendIntent.putExtra("address", "12125551212");
        //sendIntent.putExtra("sms_body","Body of Message");
        activity.startActivity(sendIntent);

    }

    public static void Call(String Mobile, Activity activity){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Mobile));
        if (ActivityCompat.checkSelfPermission(activity,
                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        activity.startActivity(callIntent);
    }

    public static boolean SameMobileNumber(String Number1, String Number2){

        Number1 = Number1.replaceAll(" ", "");
        Number2 = Number2.replaceAll(" ", "");
        if (Number1.contains(GlobalConstants.MYANMAR_COUNTRY_CODE)){
            Number1 = Number1.replace(GlobalConstants.MYANMAR_COUNTRY_CODE,"0");
            Number1 = Number1.trim();
        }

        if (Number2.contains(GlobalConstants.MYANMAR_COUNTRY_CODE)){
            Number2 = Number2.replace(GlobalConstants.MYANMAR_COUNTRY_CODE,"0");
            Number2 = Number2.trim();
        }

        return Number1.equals(Number2);
    }

    public static String ReplaceCountryCode(String Number){

        if (Number.contains(GlobalConstants.MYANMAR_COUNTRY_CODE)){
            Number = Number.replace(GlobalConstants.MYANMAR_COUNTRY_CODE,"0");
        }

        return Number;
    }
}
