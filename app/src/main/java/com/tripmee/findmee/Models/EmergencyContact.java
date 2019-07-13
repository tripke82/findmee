package com.tripmee.findmee.Models;

public class EmergencyContact extends User {

    private String EmergencyRequestID;
    public EmergencyContact(String id, String number){
        super(id,number);
    }

    public void setEmergencyRequestID(String emergencyRequestID) {
        EmergencyRequestID = emergencyRequestID;
    }

    public String getEmergencyRequestID() {
        return EmergencyRequestID;
    }
}
