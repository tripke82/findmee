package com.tripmee.findmee.Models;

public class HandShakeRequest {

    private String ContactID;
    private String Name; // name is optional
    private String MobileNumber;
    private boolean mIsCheck = false;
    public HandShakeRequest(String id, String number){

        this.ContactID= id;
        this.MobileNumber = number;
    }
    public void setContactID(String ID) {
        ContactID = ID;
    }
    public String getContactID() {
        return ContactID;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }
    public String getMobileNumber() {
        return MobileNumber;
    }

    public void SetCheckStatus(String status){

        if (status == null){
            return;
        }

        if(status.equals("true")){
            mIsCheck = true;
        }
    }
    public boolean IsChecked(){
        return mIsCheck;
    }


}
