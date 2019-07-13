package com.tripmee.findmee.Handlers;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Models.HandShakeRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

public class HandshakeHandler {
    private ArrayList<HandShakeRequest> MyHandShakeRequests;
    private ArrayList<User> OtherHandShakeRequests;

    public HandshakeHandler(){
        MyHandShakeRequests = new ArrayList<HandShakeRequest>();
        OtherHandShakeRequests = new ArrayList<User>();
    }

    public ArrayList<User> getOtherHandShakeRequests() {
        return OtherHandShakeRequests;
    }

    public void AddMyHandshakeRequests(HandShakeRequest request){
        boolean AlreadyExist = false;
        for (HandShakeRequest r:MyHandShakeRequests) { //check if it exist
            if(r.getContactID().equals(request.getContactID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            MyHandShakeRequests.add(request);
        }
    }

    public void AddOtherHandshakeRequest(User NewContact){
        boolean AlreadyExist = false;
        for (User user:OtherHandShakeRequests) { //check if it exist
            if(user.getUserID().equals(NewContact.getUserID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            OtherHandShakeRequests.add(NewContact);
        }
    }

    public boolean MyHandShakeRequestExist(){

        if (MyHandShakeRequests.size() > 0) {
            return true;
        }
        return false;
    }
    public boolean OtherHandShakeRequestExist(){

        if (OtherHandShakeRequests.size() > 0) {
            return true;
        }
        return false;
    }
    public int OtherHandShakeRequestCount(){
        return OtherHandShakeRequests.size();
    }

    public boolean CheckHandShakeAlreadyRequested(User NewContact){

        if (NewContact != null && MyHandShakeRequests != null) {
            for (HandShakeRequest request : MyHandShakeRequests) {

                if (request.getContactID().equals(NewContact.getUserID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void RequestHandShake(User CurrentUser, User NewContact, int count) {

        Boolean IsEligibleToRequest = false;

        if (CurrentUser == null || NewContact == null){
            return;
        }
        //check how many requests has been made
        //make sure no more than 5
        if (IsEligibleForHandShake(count)){
            IsEligibleToRequest = true;
        }

        //if (CurrentUser != null && NewContact != null) {
        if (IsEligibleToRequest) {
            String CurrentUID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference Ref = Database.getReference(GlobalConstants.HANDSHAKE_REQUEST).child(NewContact.getUserID());
            //Ref.child(CurrentUID).child(GlobalConstants.USER_ID).setValue(CurrentUser.getUserID());
            //Ref.child(CurrentUID).child(GlobalConstants.USER_ID).setValue(CurrentUser.getUserID());
            Ref.child(CurrentUID).child(GlobalConstants.NAME).setValue(CurrentUser.getUserName());
            Ref.child(CurrentUID).child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
            Ref.child(CurrentUID).child(GlobalConstants.ACCEPT_STATUS).setValue(GlobalConstants.FALSE);

            Ref = Database.getReference(GlobalConstants.USERS).child(CurrentUser.getUserID());
            Ref.child(GlobalConstants.REQUESTED_HANDSHAKES).child(NewContact.getUserID()).child(GlobalConstants.MOBILE).setValue(NewContact.getMobile());

            HandShakeRequest request = new HandShakeRequest(NewContact.getUserID(),NewContact.getMobile());
            //AddHandShakeToArray(request);
            if(MyHandShakeRequests == null){
                MyHandShakeRequests = new ArrayList<HandShakeRequest>();
            }
            MyHandShakeRequests.add(request);

        } else {
            //do something to handle kid doesn't exist
            //Toast.makeText(this,GlobalConstants.ERROR_OCCURED, Toast.LENGTH_SHORT).show ();
        }

    }

    public void AcceptHandShake(User CurrentUser,User NewContact){

        if (CurrentUser != null) {

            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS).child(userID);

            usersRef.child(GlobalConstants.EMERGENCY_CONTACT).child(NewContact.getUserID())
                    .child(GlobalConstants.MOBILE).setValue(NewContact.getMobile());

            DatabaseReference usersRef2 = Database.getReference(GlobalConstants.USERS).child(NewContact.getUserID());
              usersRef2.child(GlobalConstants.EMERGENCY_CONTACT).child(CurrentUser.getUserID())
                    .child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
            DeleteHandShakeRequest(CurrentUser,NewContact);
        }
    }

    public int GetHandShakeRequests(DataSnapshot dataSnapshot){
        int count=0;
        for (DataSnapshot userData: dataSnapshot.getChildren()) {

            String sAccepted = Utility.ObjectToString(userData.child(GlobalConstants.ACCEPT_STATUS).getValue());
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
                //String msg = requestedUserMobile + " sent you a hand shake request. Are you sure you want to accept it?";
                User newContact = new User(requestedUid, requestedUserMobile);
                newContact.setUserName(requestedUserName);

                AddOthersHandShakeRequests(newContact);
                count+=1;
            }
        }
        return count;
    }

    public void DeleteAllRequests(User CurrentUser){

        for (HandShakeRequest myrequest:MyHandShakeRequests) {
            //myrequest.getContactID();
           DeleteRequestByContactID(myrequest.getContactID(),CurrentUser.getUserID());
        }
    }

    public void DeleteHandShakeRequest(User CurrentUser,User contact){
        OtherHandShakeRequests.remove(contact);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query queryRef = database.getReference(GlobalConstants.HANDSHAKE_REQUEST).child(CurrentUser.getUserID())
                        .child(contact.getUserID());
        queryRef.getRef().removeValue();

        Query queryRef2 = database.getReference(GlobalConstants.USERS).child(contact.getUserID())
                          .child(GlobalConstants.REQUESTED_HANDSHAKES).child(CurrentUser.getUserID());
        queryRef2.getRef().removeValue();
    }

    public void AddOthersHandShakeRequests(final User newContact){

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
                AddOtherHandshakeRequest(newContact);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }

    public boolean IsEligibleForHandShake(int count){
        boolean IsEligible = false;
        if (count < FriendRequestConstants.MAX_CONNECTIONS){
            IsEligible =true;
        }
        return IsEligible;
    }

    private void DeleteRequestByContactID(String ContactID,String CurrentUserID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Ref = database.getReference(GlobalConstants.HANDSHAKE_REQUEST).child(ContactID).child(CurrentUserID);
        Ref.removeValue();
    }

    private void DeleteRequestByContactIDBacKUp(String ContactID,String CurrentUserID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Query queryRef = database.getReference(GlobalConstants.HANDSHAKE_REQUEST).child(ContactID).child(CurrentUserID);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                snapshot.getRef().setValue(null);
                queryRef.removeEventListener(this);
            }
            @Override
            public void onChildRemoved(DataSnapshot snapshot){ }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String text){ }
            @Override
            public void onChildMoved(DataSnapshot snapshot, String text){ }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }
}
