package com.tripmee.findmee.Models;

//Mycontact is designed to handle the phone contacts and friend request status only
public class MyContact extends User {
    private boolean IsFriendAdded = false;
    private boolean IsFriendRequested = false;
    private boolean IsFriendRequestReceived = false;

    public MyContact(String id, String number){
        super(id,number);
    }

    public void setFriendAdded(boolean value){
        IsFriendAdded = value;
    }

    public boolean isFriendAdded() {
        return IsFriendAdded;
    }

    public void setFriendRequested(boolean value){
        IsFriendRequested = value;
    }

    public boolean isFriendRequested(){
        return IsFriendRequested;
    }

    public void setFriendRequestReceived(boolean value) {
        IsFriendRequestReceived = value;
    }

    public boolean isFriendRequestReceived() {
        return IsFriendRequestReceived;
    }

    public boolean AllowedRequest(){

        if(IsFriendAdded || IsFriendRequested || IsFriendRequestReceived){
            return false;
        }
        return true;
    }
}
