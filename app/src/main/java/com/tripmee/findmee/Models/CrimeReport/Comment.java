package com.tripmee.findmee.Models.CrimeReport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    @Expose
    private Integer id; // this is the database ID in azure

    @SerializedName("reportID")
    @Expose
    private int ReportID;

    @SerializedName("userID")
    @Expose
    private String UserID;

    @SerializedName("commentID")
    @Expose
    private String CommentID;

    @SerializedName("userName")
    @Expose
    private String UserName; // name is optional

    @SerializedName("text")
    @Expose
    private String Text; // comments

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    public Comment(int reportID, String userID, String commentID, String name, String text, String dateTime){

        this.ReportID = reportID;
        this.UserID= userID;
        this.CommentID = commentID;
        this.UserName = name;
        this.Text = text;
        this.dateTime = dateTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setReportID(int reportID) {
        ReportID = reportID;
    }

    public int getReportID() {
        return ReportID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setCommentID(String commentID) {
        CommentID = commentID;
    }

    public String getCommentID() {
        return CommentID;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getText() {
        return Text;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTime() {
        return dateTime;
    }
}
