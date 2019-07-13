package com.tripmee.findmee.Models.SOS;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SOSRequest {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("userID")
    @Expose
    private String userID;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("dateTime")
    @Expose
    private String dateTime;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("videoUrl")
    @Expose
    private String videoUrl;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;

    //@SerializedName("isChecked")
    //private boolean IsChecked;

    public SOSRequest(String userID, String imageUrl, double latitude, double longitude) {

        this.userID = userID;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /*public boolean isChecked() {
        return IsChecked;
    }

    public void setChecked(boolean checked) {
        IsChecked = checked;
    }*/
}