package com.tripmee.findmee.Models.CrimeReport;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

public class CrimeReport {


    @SerializedName("id")
    @Expose
    private Integer id; // this is the database ID in azure

    @SerializedName("userID")
    @Expose
    private String userID; // this is the database ID in firebase

    @SerializedName("displayName")
    @Expose
    private String displayName; //

    @SerializedName("comments")
    @Expose
    private String comments; //

    @SerializedName("crimeType")
    @Expose
    private String crimeType; // optional

    @SerializedName("imageURL")
    @Expose
    private String imageUrl;

    @SerializedName("VideoURL")
    @Expose
    private String videoUrl;

    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("longitude")
    @Expose
    private double longitude;

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    public CrimeReport(String userID, String displayName, String comments, double Lat, double Lng){

        this.userID= userID;
        this.displayName = displayName;
        this.comments = comments;
        this.latitude = Lat;
        this.longitude = Lng;
    }

    public void setID(Integer ID) {
        this.id = ID;
    }

    public Integer getID() {
        return id;
    }

    public void setUserID(String userID) {
        userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setDisplayName(String name) {
      displayName = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime){
        this.dateTime = dateTime;
    }

    public Bitmap GetImage(){
        if(!Utility.StringIsBlankOrEmpty(imageUrl)){
            return ImageUtility.StringToBitMap(imageUrl);
        }
        return null;
    }
}
