package com.tripmee.findmee.Handlers;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class EmergencyHandler {

    public ArrayList<EmergencyContact> EmergencyContacts;
    public ArrayList<EmergencyRequest> EmergencyRequests;
    private String Temp = "";
    private String _LastEmergencyMsgSentByCurrentUser;

    public EmergencyHandler(){
        EmergencyContacts = new ArrayList<EmergencyContact>();
        EmergencyRequests = new ArrayList<EmergencyRequest>();
    }
    public boolean IsEmergencyContactExist(){

        if (EmergencyContacts != null) {
            if(EmergencyContacts.size() > 0) {
                return true;
            }
        }
        return false;
    }
    public int GetEmergencyContactsCount(){

        if (EmergencyContacts != null) {
            return EmergencyContacts.size();
        }
        return 0;
    }
    private void RemoveContactByID(String ID){
        for (EmergencyContact c:EmergencyContacts){
            if (c.getUserID().equals(ID)){
                EmergencyContacts.remove(c);
            }
        }
    }

    public void AddEmergencyRequest(EmergencyRequest request){
        boolean AlreadyExist = false;
        for (EmergencyRequest r:EmergencyRequests) { //check if it exist
            if(r.getEmergencyRequestID().equals(request.getEmergencyRequestID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            EmergencyRequests.add(request);
        }
    }
    public void AddEmergencyContact(EmergencyContact contact){
        boolean AlreadyExist = false;
        for (EmergencyContact c:EmergencyContacts) { //check if it exist
            if(c.getUserID().equals(contact.getUserID())){
                AlreadyExist = true;
            }
        }
        if(!AlreadyExist) {
            EmergencyContacts.add(contact);
        }
    }
    public void RemoveEmergencyRequest(EmergencyRequest request){
        boolean AlreadyExist = false;
        for (EmergencyRequest r:EmergencyRequests) { //check if it exist
            if(r.getEmergencyRequestID().equals(request.getEmergencyRequestID())){
                //AlreadyExist = true;
            }
        }
        if(AlreadyExist) {
            EmergencyRequests.remove(request);
        }
    }
    public EmergencyRequest GetRequestByID(String ID){

        Log.e("Requests", String.valueOf(EmergencyRequests.size()));
        for (EmergencyRequest r:EmergencyRequests){


            if (r.getEmergencyRequestID().equals(ID)){
                return r;
            }
        }

        Log.e("Request", "Not found");
        return null;
    }

    public EmergencyRequest GetLatestRequest(){
        Collections.sort(EmergencyRequests, new Comparator<EmergencyRequest>() {
            public int compare(EmergencyRequest o1, EmergencyRequest o2) {
                return o2.getmDateTime().compareTo(o1.getmDateTime());
                //return (o1.getmDateTime() > o2.getmDateTime() ? -1 : 1);     //descending
                //  return (d1.getTime() > d2.getTime() ? 1 : -1);     //ascending
            }
        });

        if (EmergencyRequests.size()> 0){
            EmergencyRequest e = EmergencyRequests.get(0);
            return e;
        }

        return null;
    }


    public void RequestEmergency(User CurrentUser) {



        if (CurrentUser != null && IsEmergencyContactExist()) {

            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            for (EmergencyContact emergencyContact: EmergencyContacts){

                if (emergencyContact != null) {

                    String eContactID = emergencyContact.getUserID();
                    String CurrentUserID = CurrentUser.getUserID();

                    DatabaseReference Ref = Database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(eContactID);
                    //String currentTime = Calendar.getInstance().getTime().toString();
                    String currentTime = Utility.FormatDateToString(Calendar.getInstance().getTime(), GlobalConstants.DATE_FORMAT);
                    String RequestID = Ref.push().getKey();
                    emergencyContact.setEmergencyRequestID(RequestID);

                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.USER_ID).setValue(CurrentUserID);
                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.NAME).setValue(CurrentUser.getUserName());
                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.LATITUDE).setValue(CurrentUser.getLatitude());
                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.LONGITUDE).setValue(CurrentUser.getLongitude());
                    Ref.child(GlobalConstants.REQUESTS).child(RequestID).child(GlobalConstants.DATE_TIME).setValue(currentTime);

                    set_LastEmergencyMsgSentByCurrentUser(CurrentUser, currentTime);


                    //SendEmergencySMS(CurrentUser, emergencyContact);


                }
            }

        } else {
            //do something to handle contact doesn't exist
            //Toast.makeText(this,GlobalConstants.ERROR_OCCURED,Toast.LENGTH_SHORT).show ();
        }

    }

    private void SendEmergencySMS(User CurrentUser, EmergencyContact emergencyContact){

        String SMSMessage = Utility.GetEmergencySMSText(emergencyContact.getUserName());
        //Phone.SendSMS(SMSMessage, emergencyContact.getMobile());
        Phone.SendSMS(SMSMessage, CurrentUser.getMobile());
    }



    public void SetEmergencyRequestStatus(String requestID, User CurrentUser){
        if (requestID != null && CurrentUser!=null) {

            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference Ref = Database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(CurrentUser.getUserID());

            Ref.child(GlobalConstants.REQUESTS).child(requestID).child(GlobalConstants.STATUS_CHECKED).setValue(GlobalConstants.TRUE);

            for(EmergencyRequest e:EmergencyRequests){
                if (e.getEmergencyRequestID().equals(requestID)){
                    e.setCheckStatus(GlobalConstants.TRUE);
                }
            }
        }
    }

    public void SetRequestShowStatus(String requestID, User CurrentUser){

        if (requestID != null && CurrentUser!=null) {

            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference Ref = Database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(CurrentUser.getUserID());

            Ref.child(GlobalConstants.REQUESTS).child(requestID).child(GlobalConstants.STATUS_SHOWED).setValue(GlobalConstants.TRUE);

            for(EmergencyRequest e:EmergencyRequests){
                if (e.getEmergencyRequestID().equals(requestID)){
                    e.setShowStatus(GlobalConstants.TRUE);
                }
            }
        }
    }

    public EmergencyRequest AddRequestFromSnapshot(DataSnapshot childdata){

        String sRequestID = childdata.getKey();
        String sUserID = childdata.child(GlobalConstants.USER_ID).getValue().toString();
        String sMobile = childdata.child(GlobalConstants.MOBILE).getValue().toString();
        String sName = childdata.child(GlobalConstants.NAME).getValue().toString();
        String sDateTime = childdata.child(GlobalConstants.DATE_TIME).getValue().toString();
        String lat = childdata.child(GlobalConstants.LATITUDE).getValue().toString();
        String lon = childdata.child(GlobalConstants.LONGITUDE).getValue().toString();
        String image="";
        EmergencyRequest e = new EmergencyRequest(sRequestID,sUserID,sDateTime,sMobile);
        e.setUserName(sName);

        if(childdata.hasChild(GlobalConstants.IMAGE_URL)){
            image = childdata.child(GlobalConstants.IMAGE_URL).getValue().toString();
            e.setEmergencyImageUrl(image);
        }

        if (lat != null && lon != null){
            e.setLatitude(Double.parseDouble(lat));
            e.setLongitude(Double.parseDouble(lon));
        }

        if(childdata.hasChild(GlobalConstants.STATUS_CHECKED)){
            String sChecked = childdata.child(GlobalConstants.STATUS_CHECKED).getValue().toString();
            e.setCheckStatus(sChecked);
        }

        if(childdata.hasChild(GlobalConstants.STATUS_SHOWED)){
            String sShowed = childdata.child(GlobalConstants.STATUS_SHOWED).getValue().toString();

            e.setShowStatus(sShowed);
        }
        return e;
    }

    public int GetNotificationCount(){
        //Get notification count by type
        int count = 0;

        if (EmergencyRequests!=null) {
            for (EmergencyRequest e : EmergencyRequests) {
                if (!e.IsChecked()) {
                    count += 1;
                }
            }
        }
        return count;
    }

    public void SendEmergencyImage (Bitmap bitmap){

        String image = ImageUtility.GetResizedBitmapToString(bitmap);
        if(image != null){
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            for (EmergencyContact emergencyContact: EmergencyContacts) {
                DatabaseReference Ref = Database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(emergencyContact.getUserID());
                String sID = emergencyContact.getEmergencyRequestID();
                if(sID!= null) {

                    Ref.child(GlobalConstants.REQUESTS).child(sID).child(GlobalConstants.IMAGE_URL).setValue(image);

                }
            }
        }

    }

    public void UpdateEmergencyContactDetails(EmergencyContact user){

        for (EmergencyContact contact:EmergencyContacts) {
            //if(contact.getMobile().equals(user.getMobile())){
            if(contact.getUserID().equals(user.getUserID())){
                contact.setUserName(user.getUserName());
                contact.setEmail(user.getEmail());
                contact.setMobile(user.getMobile());
                contact.set_FireBaseProfileImage(user.get_FireBaseProfileImage());
            }
        }
    }

    public void ListenEmergencyContactUpdates(User CurrentUser){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(CurrentUser.getUserID())
                                       .child(GlobalConstants.EMERGENCY_CONTACT);
        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {

                if(snapshot.hasChild(GlobalConstants.MOBILE)) {
                    String sId = snapshot.getKey(); //problem with getting key
                    String sPhone = Utility.ObjectToString(snapshot.child(GlobalConstants.MOBILE).getValue());

                    if (!Utility.StringIsBlankOrEmpty(sId) && !Utility.StringIsBlankOrEmpty(sPhone)) {
                        EmergencyContact contact = new EmergencyContact(sId, sPhone);

                        if (!EmergencyContacts.contains(contact)) {
                            AddEmergencyContact(contact);
                            GetEmergencyFullDetails();
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                String sId = snapshot.getKey();
                if(!Utility.StringIsBlankOrEmpty(sId)){
                RemoveContactByID(sId);}
            }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String text){ }
            @Override
            public void onChildMoved(DataSnapshot snapshot, String text){ }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                    ref.removeEventListener(this);
                    return;
                }
                //Log.e("Database Error: ", databaseError.getMessage());
                throw databaseError.toException();
            }
        });
    }

    public void Refresh(User CurrentUser){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(CurrentUser.getUserID());

       ref.addListenerForSingleValueEvent(new ValueEventListener() {
        //ref.addValueEventListener(new ValueEventListener() {
            @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if (dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).exists()){
                     EmergencyContacts.clear();
                     for (DataSnapshot userData: dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).getChildren()) {
                         String sId = userData.getKey();
                         String sPhone = userData.child(GlobalConstants.MOBILE).getValue().toString();
                         //EmergencyContact = new User(sId, sPhone);
                         EmergencyContact contact = new EmergencyContact(sId, sPhone);

                         AddEmergencyContact(contact);
                      }
                      GetEmergencyFullDetails();
                  }
                  ref.removeEventListener(this);
             }
             @Override
             public void onCancelled(DatabaseError databaseError) {

                if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                    ref.removeEventListener(this);
                     return;
                }
                    //Log.e("Database Error: ", databaseError.getMessage());
                throw databaseError.toException();
             }
        });
    }
    public void GetEmergencyFullDetails(){

        if (EmergencyContacts!= null){

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            int count =0;
            for (EmergencyContact contact:EmergencyContacts) {

                final DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(contact.getUserID());
                //String id = "yEpiChO3XSNGR5xfZV39NvNaiOw2";
                //DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(id);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String sID=dataSnapshot.getKey();
                        String Mobile = "";
                        String Name = "";
                        String ImageUrl = "";
                        String Email = "";

                        //if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            //ref.removeEventListener(this);
                        //}

                        if (dataSnapshot.child(GlobalConstants.EMAIL).getValue() != null){
                            Email = dataSnapshot.child(GlobalConstants.EMAIL).getValue().toString();

                        }
                        if (dataSnapshot.child(GlobalConstants.MOBILE).getValue() != null){
                            Mobile = dataSnapshot.child(GlobalConstants.MOBILE).getValue().toString();

                        }
                        if (dataSnapshot.child(GlobalConstants.NAME).getValue() != null){
                            Name = dataSnapshot.child(GlobalConstants.NAME).getValue().toString();
                        }
                        EmergencyContact user = new EmergencyContact(sID,Mobile);
                        user.setUserName(Name);
                        user.setEmail(Email);
                        if (dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue() != null){
                            ImageUrl= dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue().toString();
                            //String sImage = ImageUrl.replaceAll(" ", "%20");
                            user.set_FireBaseProfileImage(ImageUrl);
                        }
                        UpdateEmergencyContactDetails(user);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        //Log.e("Database Error: ", databaseError.getMessage());
                        if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                            ref.removeEventListener(this);
                            return;
                        }
                        throw databaseError.toException();
                    }
                });
            }
        }
    }

    public EmergencyContact GetEmergencyContactByID(String UserID){

        for (EmergencyContact contact:EmergencyContacts) {

            if(contact.getUserID().equals(UserID)){
                return contact;
            }
        }
        return null;
    }

    public void DeleteAllEmergencyContacts(String CurrentUserID){

        for (EmergencyContact contact: EmergencyContacts) {
            System.out.println(contact.getUserName() + " is deleted");
            DeleteEmergencyContact(CurrentUserID, contact);
        }
        //EmergencyContacts = new ArrayList<EmergencyContact>();
    }

    public void DeleteEmergencyContact(final String CurrentUserID, final EmergencyContact contact){

        final String OtherContactID = contact.getUserID();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(GlobalConstants.USERS).child(CurrentUserID).
                child(GlobalConstants.EMERGENCY_CONTACT).child(OtherContactID);
        ref.removeValue();

        ref = database.getReference(GlobalConstants.USERS).child(OtherContactID)
                .child(GlobalConstants.EMERGENCY_CONTACT).child(CurrentUserID);
        ref.removeValue();
    }

    public void DeleteEmergencyContactBackup22Nov18(String CurrentUserID, final EmergencyContact contact){

        String OtherContactID = contact.getUserID();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(GlobalConstants.USERS).child(CurrentUserID)
                .child(GlobalConstants.EMERGENCY_CONTACT);
        final Query queryRef = ref.child(OtherContactID);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                snapshot.getRef().setValue(null);
                //EmergencyContacts.remove(contact);
                queryRef.removeEventListener(this);
            }
            @Override
            public void onChildRemoved(DataSnapshot snapshot) { }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String text){ }
            @Override
            public void onChildMoved(DataSnapshot snapshot, String text){ }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                    queryRef.removeEventListener(this);
                    return;
                }
                throw databaseError.toException();
            }
        });

        DatabaseReference ref2 = database.getReference(GlobalConstants.USERS).child(OtherContactID)
                .child(GlobalConstants.EMERGENCY_CONTACT);
        final Query queryRef2 = ref2.child(CurrentUserID); //orderbychild returns all the children which causes problems
        queryRef2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                snapshot.getRef().setValue(null);
                queryRef2.removeEventListener(this);
            }
            @Override
            public void onChildRemoved(DataSnapshot snapshot){ }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String text){ }
            @Override
            public void onChildMoved(DataSnapshot snapshot, String text){ }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                    queryRef2.removeEventListener(this);
                    return;
                }
                throw databaseError.toException();
            }
        });
    }

    public void set_LastEmergencyMsgSentByCurrentUser(User user, String Time) {
        String msg = "Your last findmee request is sent on " + Time + ". Your location: Latitude:"
                + user.getLatitude() + " Longitude:" + user.getLongitude();
        this._LastEmergencyMsgSentByCurrentUser = msg;
    }

    public String get_LastEmergencyMsgSentByCurrentUser(){
        return _LastEmergencyMsgSentByCurrentUser;
    }
}
