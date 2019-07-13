package com.tripmee.findmee.Models;

import android.util.Log;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Utilities.Utility;

import java.text.ParseException;
import java.util.Date;

public class EmergencyRequest extends EmergencyContact {

    //private String mDateTimeString;
    private Date mDateTime = null;
    private Boolean mIsCheck = false;
    private Boolean mShowed = false;
    private String emergencyImageUrl;


    public EmergencyRequest(String requestID, String UserID, String DateTime, String Mobile){
        super(UserID,Mobile);
        super.setEmergencyRequestID(requestID);
        //mDateTimeString = DateTime;

        Date value = null;

        try{

            value = Utility.StringToDate(DateTime, "EEE MMM dd HH:mm:ss ZZZZ yyyy");
        }catch (ParseException ex){
            Log.e("Emergency DateEr", ex.getMessage());
        }

        if(value == null){
            try{

                value = Utility.StringToDate(DateTime, GlobalConstants.DATE_FORMAT);
            }catch (ParseException ex){
                Log.e("Emergency DateEr", ex.getMessage());
            }
        }

        mDateTime = value;
    }
    public String getEmergencyMessage(){

        String msg = this.getUserName() + " sent you an Emergency Request " + mDateTime;
        return msg;
    }

    public Date getmDateTime() {
        return mDateTime;
    }

    public void setCheckStatus(String status){

        if (status == null){
            return;
        }

        if(status.equals(GlobalConstants.TRUE)){
            mIsCheck = true;
        }
    }
    public boolean IsChecked(){
        return mIsCheck;
    }

    public void setShowStatus(String status) {

        if (status == null){
            return;
        }

        if(status.equals(GlobalConstants.TRUE)){
            mShowed = true;
        }
    }

    public boolean IsShowed() {
        return mShowed;
    }

    public boolean IsValidToPushNotification(){

        //if findmee request is not checked or showed then it is ok to push notification
        if (mIsCheck == false && mShowed == false){
            return true;
        }
        return false;
    }

    public String getEmergencyImageUrl(){
        return emergencyImageUrl;
    }

    public void setEmergencyImageUrl(String imageUrl){
        emergencyImageUrl = imageUrl;
    }

    public boolean HasImage(){

        if(emergencyImageUrl != null){
            if(!emergencyImageUrl.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
