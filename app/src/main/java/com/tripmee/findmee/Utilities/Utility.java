package com.tripmee.findmee.Utilities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {


    public void GetSOSRequests(){
        /*
        GetSOSDataService service = RetrofitInstance.getRetrofitInstance().create(GetSOSDataService.class);
        Call<List<SOSRequest>> call = service.getSOSData();
        call.enqueue(new Callback<List<SOSRequest>>() {
            @Override
            public void onResponse(Call<List<SOSRequest>> call, Response<List<SOSRequest>> response) {
                //Log.d("SOS", response.toString());
                //List SOSRequests = response.body();
                //List SOSRequests = Lists<SOSRequest>;
                for (SOSRequest sos:response.body()) {
                    Log.d("SOSRequest", sos.getImageUrl());
                }
            }

            @Override
            public void onFailure(Call<List<SOSRequest>> call, Throwable t) {
                Log.d("SOSFail", t.getMessage());
            }
        });
        */

    }

    public static Date StringToDate(String sDateTime, String datePattern) throws ParseException {

        if (sDateTime != null) {

            if(!sDateTime.isEmpty()) {
                //DateFormat format =  Calendar.getInstance().
                //SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZ yyyy", Locale.getDefault());

                SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());

                Date date = dateFormat.parse(sDateTime);
                return date;
            }
        }
        return null;
    }

    public static String FormatDateToString(Date date, String format) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static boolean StringEquals(Object a, String b) {
       if (a != null && b != null){
           String a1 = a.toString();
           if(a1.equals(b)){
               return true;
           }
        }
        return false;
    }
    public static String ObjectToString(Object o) {
       if( o!=null){
          return o.toString();
       }
        return "";
    }

    public static void displayMessage(Context context, String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static String random(int size) {

        StringBuilder generatedToken = new StringBuilder();
        try {
            SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
            // Generate 20 integers 0..20
            for (int i = 0; i < size; i++) {
                generatedToken.append(number.nextInt(9));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedToken.toString();
    }

    public static boolean StringIsBlankOrEmpty(String value){

        if (value == null){
            return true;
        }else if(value.isEmpty()){
            return true;
        }
        if (value.equals("")){
            return true;
        }
        return false;
    }

    public static boolean IsStringEqual(String value1, String value2){

        if(StringIsBlankOrEmpty(value1)){
            return false;
        }

        if(StringIsBlankOrEmpty(value2)){
            return false;
        }

        return value1.equals(value2);
    }

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public static boolean IsFacebookLogin(FirebaseUser user){
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("facebook.com")) {
                return true;
            }
        }
        return false;
    }

    public static String GetProfileImageName(String UserID){
        String currentTime = FormatDateToString(Calendar.getInstance().getTime(), GlobalConstants.DATE_FORMAT);
        return UserID + currentTime;
    }

    public static String GetLargeProfileImageUrl(String profileImageUrl){
        return profileImageUrl + "?type=large";
    }

    public static String GetFileNameByPath(String path){

        if(path == null || path.isEmpty()){
            return "";
        }

        String filename=path.substring(path.lastIndexOf("/")+1);
        return filename;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static boolean IsFacebookLoggedin(Intent intent){

        String s1 = intent.getStringExtra("Facebook");
        if(!Utility.StringIsBlankOrEmpty(s1)) {
            if (s1.equals("Loggedin")) {
                return true;
            }
        }

        return false;
    }

    public static String GetEmergencySMSText(String ContactName){

        String part1 = "Hi ";
        String part2 = "I am in danger. Open Safie App to see where I am and call police.";

        /*String msg =  Resources.getSystem().getString(R.string.emergency_SMS_part_1) + UserName + Resources.getSystem().getString(R.string.emergency_SMS_part_2)
                + "Hi " + ContactName + Resources.getSystem().getString(R.string.emergency_SMS_part_3) + LocationUtility.GetUrlByLocation(Lat, Long)
                + Resources.getSystem().getString(R.string.emergency_SMS_part_4);*/

        String msg =  part1 + ContactName + part2;

        return msg;
    }

    public static String GetEmergencySMSTextBackup(String UserName, String ContactName, Double Lat, Double Long){

        String part1 = "Safie App - !!";
        String part2 = "Emergency Call - !!";
        String part3 = " I am in danger and my location is ";
        String part4 = "Please contact the police, give them my name, description and location.";


        /*String msg =  Resources.getSystem().getString(R.string.emergency_SMS_part_1) + UserName + Resources.getSystem().getString(R.string.emergency_SMS_part_2)
                + "Hi " + ContactName + Resources.getSystem().getString(R.string.emergency_SMS_part_3) + LocationUtility.GetUrlByLocation(Lat, Long)
                + Resources.getSystem().getString(R.string.emergency_SMS_part_4);*/

        String msg =  part1 + UserName + part2 + "Hi " + ContactName + part3 + LocationUtility.GetUrlByLocation(Lat, Long)
                      + part4;


        return msg;
    }

}
