package com.tripmee.findmee.Handlers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Models.FriendRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

public class FriendRequestHandler {

    private ArrayList<FriendRequest> FriendRequestsSent;
    private ArrayList<User> FriendRequestsReceived;
    public DataChangedCallBack FriendRequestsCallBack;

    public FriendRequestHandler(){
        FriendRequestsSent = new ArrayList<FriendRequest>();
        FriendRequestsReceived = new ArrayList<User>();
    }

    public ArrayList<User> getOtherFriendRequests() {
        return FriendRequestsReceived;
    }

    public void AddMyFriendRequests(FriendRequest request){
        boolean AlreadyExist = false;
        for (FriendRequest r:FriendRequestsSent) { //check if it exist
            if(r.getContactID().equals(request.getContactID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            FriendRequestsSent.add(request);
        }
    }

    public void AddFriendRequestsReceived(User NewContact){
        boolean AlreadyExist = false;
        for (User user:FriendRequestsReceived) { //check if it exist
            if(user.getUserID().equals(NewContact.getUserID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            FriendRequestsReceived.add(NewContact);
        }
    }

    public boolean MyFriendRequestExist(){

        if (FriendRequestsSent.size() > 0) {
            return true;
        }
        return false;
    }
    public boolean FriendRequestsReceived_Exist(){

        if (FriendRequestsReceived.size() > 0) {
            return true;
        }
        return false;
    }
    public int FriendRequestsReceived_Count(){
        return FriendRequestsReceived.size();
    }

    public boolean CheckFriendAlreadyRequested(User NewContact){

        if (NewContact != null && FriendRequestsSent != null) {
            for (FriendRequest request : FriendRequestsSent) {

                /*if (request.getContactID().equals(NewContact.getUserID())) {
                    return true;
                }*/

                //comparing with number is better/faster as sometimes you can get phone number but not userid
                if (Phone.SameMobileNumber(request.getMobileNumber(), NewContact.getMobile())){
                    return true;
                }

            }
        }
        return false;
    }

    public boolean IsFriendRequestedByUser(User NewContact){

        if (NewContact != null && FriendRequestsReceived != null) {
            for (User user : FriendRequestsReceived) {

                if (Phone.SameMobileNumber(user.getMobile(), NewContact.getMobile())){
                    return true;
                }

            }
        }
        return false;
    }


    public void RequestFriend(User CurrentUser, User NewContact, int count) {

        Boolean IsEligibleToRequest = false;

        if (CurrentUser == null || NewContact == null){
            return;
        }
        //check how many requests has been made
        //make sure no more than 5
        if (IsEligibleForFriendRequest(count)){
            IsEligibleToRequest = true;
        }

        //if (CurrentUser != null && NewContact != null) {
        if (IsEligibleToRequest) {
            String CurrentUID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference Ref = Database.getReference(FriendRequestConstants.FRIEND_REQUEST).child(NewContact.getUserID());
            //Ref.child(CurrentUID).child(GlobalConstants.USER_ID).setValue(CurrentUser.getUserID());
            //Ref.child(CurrentUID).child(GlobalConstants.USER_ID).setValue(CurrentUser.getUserID());
            Ref.child(CurrentUID).child(GlobalConstants.NAME).setValue(CurrentUser.getUserName());
            Ref.child(CurrentUID).child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
            Ref.child(CurrentUID).child(GlobalConstants.ACCEPT_STATUS).setValue(GlobalConstants.FALSE);

            Ref = Database.getReference(GlobalConstants.USERS).child(CurrentUser.getUserID());
            Ref.child(FriendRequestConstants.REQUESTED_FRIENDS).child(NewContact.getUserID()).child(GlobalConstants.MOBILE).setValue(NewContact.getMobile());

            FriendRequest request = new FriendRequest(NewContact.getUserID(),NewContact.getMobile());
            //AddHandShakeToArray(request);
            if(FriendRequestsSent == null){
                FriendRequestsSent = new ArrayList<FriendRequest>();
            }
            FriendRequestsSent.add(request);

        } else {
            //do something to handle kid doesn't exist
            //Toast.makeText(this,GlobalConstants.ERROR_OCCURED, Toast.LENGTH_SHORT).show ();
        }

    }

    public void AcceptFriendRequest(User CurrentUser,User NewContact){

        if (CurrentUser != null) {

            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS).child(userID);

            usersRef.child(GlobalConstants.EMERGENCY_CONTACT).child(NewContact.getUserID())
                    .child(GlobalConstants.MOBILE).setValue(NewContact.getMobile());

            DatabaseReference usersRef2 = Database.getReference(GlobalConstants.USERS).child(NewContact.getUserID());
            usersRef2.child(GlobalConstants.EMERGENCY_CONTACT).child(CurrentUser.getUserID())
                    .child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
            DeleteFriendRequest(CurrentUser,NewContact);
        }
    }

    public int GetFriendRequests(DataSnapshot dataSnapshot){
        int count=0;
        for (DataSnapshot userData: dataSnapshot.getChildren()) {

            String sAccepted = Utility.ObjectToString(userData.child(FriendRequestConstants.ACCEPT_STATUS).getValue());
            String requestedUid = userData.getKey();

            String requestedUserName = "";
            String requestedUserMobile = "";
            if(sAccepted.equals(GlobalConstants.FALSE)){
                //requestedUid = userData.child(GlobalConstants.USER_ID).getValue().toString();
                if (userData.child(GlobalConstants.MOBILE).getValue() != null) {
                    requestedUserMobile = userData.child(GlobalConstants.MOBILE).getValue().toString();
                }
                if (userData.child(GlobalConstants.NAME).getValue() != null) {
                    requestedUserName = userData.child(GlobalConstants.NAME).getValue().toString();
                }
                //String msg = requestedUserMobile + " sent you a friend request. Are you sure you want to accept it?";
                User newContact = new User(requestedUid, requestedUserMobile);
                newContact.setUserName(requestedUserName);

                //AddOthersHandShakeRequests(newContact);
                AddReceivedFriendRequests(newContact);
                count+=1;
            }
        }
        return count;
    }

    public void DeleteAllRequests(User CurrentUser){

        for (FriendRequest myrequest:FriendRequestsSent) {
            //myrequest.getContactID();
            DeleteRequestByContactID(myrequest.getContactID(),CurrentUser.getUserID());
        }
    }

    public void DeleteFriendRequest(User CurrentUser,User contact){
        FriendRequestsReceived.remove(contact);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query queryRef = database.getReference(FriendRequestConstants.FRIEND_REQUEST).child(CurrentUser.getUserID())
                .child(contact.getUserID());
        queryRef.getRef().removeValue();

        Query queryRef2 = database.getReference(GlobalConstants.USERS).child(contact.getUserID())
                .child(FriendRequestConstants.REQUESTED_FRIENDS).child(CurrentUser.getUserID());
        queryRef2.getRef().removeValue();
    }

    private void AddReceivedFriendRequests(final User newContact){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(newContact.getUserID());

        //get all the other missing details before adding to arraylist
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            //ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Mobile = "";
                String Name = "";
                String ImageUrl;

                if (dataSnapshot.child(GlobalConstants.EMAIL).getValue() != null){
                    String Email = dataSnapshot.child(GlobalConstants.EMAIL).getValue().toString();
                    newContact.setEmail(Email);
                }
                if (dataSnapshot.child(GlobalConstants.MOBILE).getValue() != null){
                    Mobile = dataSnapshot.child(GlobalConstants.MOBILE).getValue().toString();
                    newContact.setMobile(Mobile);
                }
                if (dataSnapshot.child(GlobalConstants.NAME).getValue() != null){
                    Name = dataSnapshot.child(GlobalConstants.NAME).getValue().toString();
                    newContact.setUserName(Name);
                }
                if (dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue() != null){
                    ImageUrl= dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue().toString();
                    //String sImage = ImageUrl.replaceAll(" ", "%20");
                    newContact.set_FireBaseProfileImage(ImageUrl);
                }
                //OtherHandShakeRequests.add(newContact);
                AddFriendRequestsReceived(newContact);

                if(FriendRequestsCallBack !=null){
                    FriendRequestsCallBack.CallBack();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }

    public boolean IsEligibleForFriendRequest(int count){
        boolean IsEligible = false;
        if (count < FriendRequestConstants.MAX_CONNECTIONS){
            IsEligible =true;
        }
        return IsEligible;
    }

    private void DeleteRequestByContactID(String ContactID,String CurrentUserID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Ref = database.getReference(FriendRequestConstants.FRIEND_REQUEST).child(ContactID).child(CurrentUserID);
        Ref.removeValue();
    }
}
