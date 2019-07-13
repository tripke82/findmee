package com.tripmee.findmee.Models;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class User {

    @SerializedName("id")
    @Expose
    private Integer id; // this is the database ID in azure

    @SerializedName("userID")
    @Expose
    private String UserID; // this is the database ID in firebase

    @SerializedName("userName")
    @Expose
    private String UserName; // name is optional

    @SerializedName("email")
    @Expose
    private String Email;

    @SerializedName("profileImageURL")
    @Expose
    private String ProfileImageUrl;

    @SerializedName("mobile")
    @Expose
    private String Mobile;

    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("longitude")
    @Expose
    private double longitude;

    @SerializedName("password")
    @Expose
    private String Password;

    private String _FireBaseProfileImage;

    private String token;
    //3 things below no longer required
    public com.tripmee.findmee.Models.UserStatus Status;
    private Date ActivatedDate;
    private Date RegisteredDate;

    public User(String userID, String number){

        this.UserID= userID;
        this.Mobile = number;
    }

    public void setID(Integer ID) {
        this.id = ID;
    }

    public Integer getID() {
        return id;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserName(String name) {
        UserName = name;
    }

    public String getUserName() {
        return UserName;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getEmail() {
        return Email;
    }

    public void setMobile(String mobileNumber) {
        Mobile = mobileNumber;
    }
    public String getMobile() {
        return Mobile;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getProfileImageUrl() {
        return ProfileImageUrl;
    }

    public void setProfileImageUrl(String imageUrl) {
        ProfileImageUrl = imageUrl;
    }

    public Bitmap GetProfileImage(){
        if(!Utility.StringIsBlankOrEmpty(ProfileImageUrl)){
            return ImageUtility.StringToBitMap(ProfileImageUrl);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.default_profile);
        return bitmap;
        //Bitmap bmp = ((BitmapDrawable) Resources
                //.getDrawable(R.drawable.default_profile)).getBitmap();
        //return bmp;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPassword() {
        return Password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken(){
        return this.token;
    }

    public void set_FireBaseProfileImage(String _FireBaseProfileImage) {
        this._FireBaseProfileImage = _FireBaseProfileImage;
    }

    public String get_FireBaseProfileImage() {
        return _FireBaseProfileImage;
    }

    public void setActivatedDate(Date date){
        ActivatedDate = date;
    }

    public Date getActivatedDate(){
        return  ActivatedDate;
    }

    public void setRegisteredDate(Date registeredDate) {
        RegisteredDate = registeredDate;
    }

    public Date getRegisteredDate() {
        return RegisteredDate;
    }

    public void Init_Free_Status(){
        Status = com.tripmee.findmee.Models.UserStatus.ACTIVATED;
    }

    public void Init_Status(){
        //this function will only be called, if we change the business model from freemium to pay
        //try {
            //Date date1 = Utility.StringToDate("Thu Nov 01 16:27:48 GMT+06:30 2018");
            //Date date2 = Utility.StringToDate("Wed Oct 31 16:27:48 GMT+06:30 2018");
            //RegisteredDate = date2;
            //ActivatedDate = date1;
        if (RegisteredDate != null && ActivatedDate == null) {

            Date CurrentDate = Calendar.getInstance().getTime();
            long diff1 = CurrentDate.getTime() - RegisteredDate.getTime();
            long hours = TimeUnit.HOURS.convert(diff1, TimeUnit.MILLISECONDS);
            long diff2;
            long Days = 366;
            if(ActivatedDate != null) {
                diff2 = CurrentDate.getTime() - ActivatedDate.getTime();
                Days = TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS);
            }

            if (hours < GlobalConstants.TWENTYFOUR_HOURS) {
                Status = com.tripmee.findmee.Models.UserStatus.FREETRIAL;
            } else if (Days <= GlobalConstants.ONE_YEAR_MEMBERSHIP) {
                Status = com.tripmee.findmee.Models.UserStatus.ACTIVATED;
            } else {
                Status = com.tripmee.findmee.Models.UserStatus.RESTRICTED;
            }

        }
        if (ActivatedDate != null && RegisteredDate != null) {

            Date CurrentDate = Calendar.getInstance().getTime();
            long diff1 = CurrentDate.getTime() - RegisteredDate.getTime();
            long hours = TimeUnit.HOURS.convert(diff1, TimeUnit.MILLISECONDS);

            long diff2 = CurrentDate.getTime() - ActivatedDate.getTime();
            long Days = TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS);

            if (Days <= GlobalConstants.ONE_YEAR_MEMBERSHIP) {
                Status = com.tripmee.findmee.Models.UserStatus.ACTIVATED;
            } else if (hours < GlobalConstants.TWENTYFOUR_HOURS) {
                Status = com.tripmee.findmee.Models.UserStatus.FREETRIAL;
            } else {
                    Status = com.tripmee.findmee.Models.UserStatus.RESTRICTED;
           }

        }
        //}catch (java.text.ParseException ex){

        //}
    }

}
