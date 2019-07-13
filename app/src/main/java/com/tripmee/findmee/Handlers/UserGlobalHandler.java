package com.tripmee.findmee.Handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.Models.FriendRequest;
import com.tripmee.findmee.Models.HandShakeRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class UserGlobalHandler {

    public User CurrentUser;
    private com.tripmee.findmee.Handlers.EmergencyHandler emergencyHandler;
    private com.tripmee.findmee.Handlers.HandshakeHandler handshakeHandler;
    private com.tripmee.findmee.Handlers.FriendRequestHandler friendrequestHandler;
    public DataChangedCallBack InitUserCompletedCallBack;
    public DataChangedCallBack FriendRequestsCallBack;
    private Location _CurrentLocation;
    private User NewContact;
    private int NewContact_ConnectionsCount = 0;
    private String Password = "";
    private boolean IsNumberExist = false;
    private boolean IsFireBaseDataLoaded = false;

    private AccessToken _FacebookAccessToken = null;

    private static UserGlobalHandler _instance;

    public static synchronized UserGlobalHandler get_instance() {

        if (_instance == null) {

            _instance = new UserGlobalHandler();
            _instance.Init();
        }
        return _instance;
    }

    public static synchronized UserGlobalHandler get_instance(AccessToken FacebookToken, String password) {
    //if user is logged in by facebook password will be empty and vice versa

        if (_instance == null) {

            _instance = new UserGlobalHandler();
            _instance.set_FacebookAccessToken(FacebookToken);
            _instance.Password = password;
            _instance.Init();
        }
        return _instance;
    }

    public void Init(){

        handshakeHandler = new com.tripmee.findmee.Handlers.HandshakeHandler();
        friendrequestHandler = new com.tripmee.findmee.Handlers.FriendRequestHandler();
        emergencyHandler = new com.tripmee.findmee.Handlers.EmergencyHandler();
        InitCurrentUser();
    }

    public void ClearAll(){

        handshakeHandler = null;
        friendrequestHandler = null;
        emergencyHandler = null;
        CurrentUser = null;
        NewContact = null;
        _CurrentLocation = null;
        NewContact_ConnectionsCount = 0;
        IsFireBaseDataLoaded = false;
        IsNumberExist = false;
        Password = "";
        _FacebookAccessToken = null;
        _instance = null;
    }

    public void set_FacebookAccessToken(AccessToken _FacebookAccessToken) {
        this._FacebookAccessToken = _FacebookAccessToken;
    }

    public void setCurrentLocation(Location currentLocation) {
        _CurrentLocation = currentLocation;
        CurrentUser.setLatitude(currentLocation.getLatitude());
        CurrentUser.setLongitude(currentLocation.getLongitude());
    }

    public Location get_CurrentLocation() {
        return _CurrentLocation;
    }

    public void setCurrentUserName(String name){
        if (CurrentUser != null){
            CurrentUser.setUserName(name);
        }
    }

    public void setCurrentUserPhone(String phone){
        if (CurrentUser != null){
            CurrentUser.setMobile(phone);
        }
    }

    public void SaveUser(Bitmap bitmap) {

        if (CurrentUser == null){
            Init();
        }
        if (CurrentUser != null) {

            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            String currentTime = Calendar.getInstance().getTime().toString();

            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);

            usersRef.child(userID).child(GlobalConstants.NAME).setValue(CurrentUser.getUserName());
            usersRef.child(userID).child(GlobalConstants.EMAIL).setValue(CurrentUser.getEmail());
            usersRef.child(userID).child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());
            usersRef.child(userID).child(GlobalConstants.REGISTERED_DATE).setValue(currentTime);

            SaveProfilePicture(bitmap, "");

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().AddLoggedinUserToDB(CurrentUser);// database sync between firebase & azure
        }
    }

    public void ActivateUser() {

        if (CurrentUser == null){
            Init();
        }
        if (CurrentUser != null) {
            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            CurrentUser.setActivatedDate(Calendar.getInstance().getTime());
            String currentTime = CurrentUser.getActivatedDate().toString();

            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);
            usersRef.child(userID).child(GlobalConstants.ACTIVATED_DATE).setValue(currentTime);
        }
    }

    public void UpdateName(String sName){
        if (CurrentUser != null) {

            CurrentUser.setUserName(sName);
            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);

            usersRef.child(userID).child(GlobalConstants.NAME).setValue(CurrentUser.getUserName());

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().UpdateUser(CurrentUser);
        }
    }

    public void UpdatePhone(String Number){
        if (CurrentUser != null) {

            CurrentUser.setMobile(Number);
            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);

            usersRef.child(userID).child(GlobalConstants.MOBILE).setValue(CurrentUser.getMobile());

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().UpdateUser(CurrentUser);
        }
    }

    public void UpdateEmail(String sEmail){
        if (CurrentUser != null) {

            CurrentUser.setEmail(sEmail);
            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);
            usersRef.child(userID).child(GlobalConstants.EMAIL).setValue(CurrentUser.getEmail());

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().UpdateUser(CurrentUser);
        }
    }

    public void RemoveProfilePhoto(){
        if (CurrentUser != null) {

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().RemoveProfilePic(CurrentUser, CurrentUser.getProfileImageUrl());

            CurrentUser.set_FireBaseProfileImage("");
            CurrentUser.setProfileImageUrl("");
            //Log.e("Update Location latest", Lat.toString());
            String userID = CurrentUser.getUserID();
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = Database.getReference(GlobalConstants.USERS);
            usersRef.child(userID).child(GlobalConstants.IMAGE_URL).setValue(CurrentUser.get_FireBaseProfileImage());

        }
    }

    public void DeleteUser(){
        if (CurrentUser != null) {

            //if my handshake requests exist then delete them
            handshakeHandler.DeleteAllRequests(CurrentUser);
            friendrequestHandler.DeleteAllRequests(CurrentUser);
            //if findmee contacts exist then delete them too
            emergencyHandler.DeleteAllEmergencyContacts(CurrentUser.getUserID());

            //now delete the user
            String userID = CurrentUser.getUserID();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference(GlobalConstants.USERS).child(userID);
            ref.removeValue();

            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().DeleteUser(CurrentUser);
        }
    }

    public void InitContactDetails(User user, DataSnapshot dataSnapshot){

        NewContact = user;
        IsNumberExist = true;

        if (dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).exists()){

            String s = dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).toString();
            //NewContact_ConnectionsCount = Math.toIntExact(user.child(GlobalConstants.EMERGENCY_CONTACT).getChildrenCount());
            NewContact_ConnectionsCount =  0;
            for (DataSnapshot childData: dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).getChildren()) {
                NewContact_ConnectionsCount += 1;
            }
        }
    }

    private void InitCurrentUser(){

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){

            CurrentUser = new User(user.getUid(), user.getPhoneNumber());
            CurrentUser.setEmail(user.getEmail());
            CurrentUser.Init_Free_Status();

            //GetUserFromSQLDatabase();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(user.getUid());

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
            //ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Mobile = "";
                    String Name = "";
                    String ImageUrl;

                    if (dataSnapshot.child(GlobalConstants.EMAIL).getValue() != null){
                        String Email = dataSnapshot.child(GlobalConstants.EMAIL).getValue().toString();
                        CurrentUser.setEmail(Email);
                    }
                    if (dataSnapshot.child(GlobalConstants.MOBILE).getValue() != null){
                        Mobile = dataSnapshot.child(GlobalConstants.MOBILE).getValue().toString();
                        CurrentUser.setMobile(Mobile);
                    }
                    if (dataSnapshot.child(GlobalConstants.NAME).getValue() != null){
                        Name = dataSnapshot.child(GlobalConstants.NAME).getValue().toString();
                        CurrentUser.setUserName(Name);
                    }
                    if (dataSnapshot.child(GlobalConstants.REGISTERED_DATE).getValue() != null){
                        String sDate = dataSnapshot.child(GlobalConstants.REGISTERED_DATE).getValue().toString();

                        try {
                            CurrentUser.setRegisteredDate(Utility.StringToDate(sDate, GlobalConstants.DATE_FORMAT));
                        }catch (java.text.ParseException ex){}
                    }
                    if (dataSnapshot.child(GlobalConstants.ACTIVATED_DATE).getValue() != null){
                        String sDate = dataSnapshot.child(GlobalConstants.ACTIVATED_DATE).getValue().toString();

                        try {
                            CurrentUser.setActivatedDate(Utility.StringToDate(sDate, GlobalConstants.DATE_FORMAT));
                        }catch (java.text.ParseException ex){}
                    }
                    if (dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue() != null){
                        ImageUrl= dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue().toString();
                        //String sImage = ImageUrl.replaceAll(" ", "%20");
                        CurrentUser.set_FireBaseProfileImage(ImageUrl);
                    }
                    if (dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).exists()){

                         String s = dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).toString();
                        for (DataSnapshot userData: dataSnapshot.child(GlobalConstants.EMERGENCY_CONTACT).getChildren()) {

                            String sId = userData.getKey();
                            String sPhone = userData.child(GlobalConstants.MOBILE).getValue().toString();
                            // the bug occurs once contact changed phone number and that is not updated here. Need to fix it.
                            //EmergencyContact = new User(sId, sPhone);
                            EmergencyContact contact = new EmergencyContact(sId, sPhone);

                            emergencyHandler.AddEmergencyContact(contact);
                        }
                        emergencyHandler.GetEmergencyFullDetails();
                    }
                    if (dataSnapshot.child(GlobalConstants.REQUESTED_HANDSHAKES).hasChildren()){
                        //String status = dataSnapshot.child(GlobalConstants.HANDSHAKE_REQUESTED).getValue().toString();
                        for (DataSnapshot userData: dataSnapshot.child(GlobalConstants.REQUESTED_HANDSHAKES).getChildren()) {

                            String sId = userData.getKey(); //other contact
                            String sPhone = userData.child(GlobalConstants.MOBILE).getValue().toString(); //other contact mobile number
                            //EmergencyContact = new User(sId, sPhone)
                            HandShakeRequest myHandshakeRequest = new HandShakeRequest(sId, sPhone);
                            handshakeHandler.AddMyHandshakeRequests(myHandshakeRequest);
                            //MyHandShakeRequests.add(new HandShakeRequest(sId, sPhone));
                            //Log.e("HandShake Exist", sPhone);
                        }

                    }
                    if (dataSnapshot.child(FriendRequestConstants.REQUESTED_FRIENDS).hasChildren()){
                        //String status = dataSnapshot.child(GlobalConstants.HANDSHAKE_REQUESTED).getValue().toString();
                        for (DataSnapshot userData: dataSnapshot.child(FriendRequestConstants.REQUESTED_FRIENDS).getChildren()) {

                            String sId = userData.getKey(); //other contact
                            String sPhone = userData.child(GlobalConstants.MOBILE).getValue().toString(); //other contact mobile number
                            FriendRequest myFriendRequest = new FriendRequest(sId, sPhone);
                            friendrequestHandler.AddMyFriendRequests(myFriendRequest);
                        }

                    }

                    //UnitOfWork.get_instance(CurrentUser).AddLoggedinUserToDB(CurrentUser);// database sync between firebase & azure

                    IsFireBaseDataLoaded = true;
                    if(InitUserCompletedCallBack !=null){
                       InitUserCompletedCallBack.CallBack();
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                    if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                        ref.removeEventListener(this);
                        //Utility.displayMessage(this.UserGlobalHandler,"Function:InitCurrentUser - Listener has been removed");
                        return;
                    }
                    //Log.e("Database Error: ", databaseError.getMessage());
                    throw databaseError.toException();
                }
            });
            //if(UOW.getUserCallBack)

        }
    }

    public User GetUserByDataSnapShot(DataSnapshot dataSnapshot){
        User user = null;

        if(dataSnapshot.child(GlobalConstants.MOBILE).getValue() != null){
            String ID = dataSnapshot.getKey().toString();
            String Mobile = dataSnapshot.child(GlobalConstants.MOBILE).getValue().toString();
            user = new User(ID,Mobile);
        }else{
            return null;
        }

        if (dataSnapshot.child(GlobalConstants.EMAIL).getValue() != null){
             String Email = dataSnapshot.child(GlobalConstants.EMAIL).getValue().toString();
             user.setEmail(Email);
        }
        if (dataSnapshot.child(GlobalConstants.NAME).getValue() != null){
            String Name = dataSnapshot.child(GlobalConstants.NAME).getValue().toString();
            user.setUserName(Name);
        }
        if (dataSnapshot.child(GlobalConstants.REGISTERED_DATE).getValue() != null){
            String sDate = dataSnapshot.child(GlobalConstants.REGISTERED_DATE).getValue().toString();

            try {
                user.setRegisteredDate(Utility.StringToDate(sDate, GlobalConstants.DATE_FORMAT));
            }catch (java.text.ParseException ex){}
        }
        if (dataSnapshot.child(GlobalConstants.ACTIVATED_DATE).getValue() != null){
            String sDate = dataSnapshot.child(GlobalConstants.ACTIVATED_DATE).getValue().toString();

            try {
                user.setActivatedDate(Utility.StringToDate(sDate, GlobalConstants.DATE_FORMAT));
            }catch (java.text.ParseException ex){}
        }
        if (dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue() != null){
            String ImageUrl= dataSnapshot.child(GlobalConstants.IMAGE_URL).getValue().toString();
            //String sImage = ImageUrl.replaceAll(" ", "%20");
            user.set_FireBaseProfileImage(ImageUrl);
        }

        return user;
    }

    public boolean isFireBaseDataLoaded() {
        return IsFireBaseDataLoaded;
    }

    public void SaveProfilePicture (Bitmap bitmap, String PreviousImgPath){
        if (bitmap!= null) {

            //save data in firebase
            String imageEncoded = ImageUtility.GetResizedBitmapToString(bitmap);
            FirebaseDatabase Database = FirebaseDatabase.getInstance();
            DatabaseReference Ref = Database.getReference(GlobalConstants.USERS);
            Ref.child(CurrentUser.getUserID()).child(GlobalConstants.IMAGE_URL).setValue(imageEncoded);

            CurrentUser.set_FireBaseProfileImage(imageEncoded);

            //Update the profile image link in azure database
            //UnitOfWork.get_instance(CurrentUser).GetUserNetworkService().UpdateUserProfilePic(bitmap,Utility.GetProfileImageName(CurrentUser.getUserID()), PreviousImgPath);
        }

    }

    public int GetNotificationCount(boolean IsEmergencyRequest){
        //Get notification count by type
        if (IsEmergencyRequest){
            return emergencyHandler.GetNotificationCount();
        }else{
            return handshakeHandler.OtherHandShakeRequestCount();
        }
    }


    /*
    private void GetUserFromSQLDatabase(){

        final UnitOfWork UOW = UnitOfWork.get_instance(CurrentUser); //get the user from azure db before firebase query is done

        if(_FacebookAccessToken != null){ // if facebook logged in
            UOW.GetUserNetworkService().AuthenticateFacebookUser(CurrentUser.getUserID(), _FacebookAccessToken.getToken());
        }else{

            if(!Utility.StringIsBlankOrEmpty(Password)){
                CurrentUser.setPassword(Password);
            }
            UOW.GetUserNetworkService().Authenticate(CurrentUser);
        }

        UOW.GetUserNetworkService().AuthCallBack = new ActionCompleteCallBack() {
            @Override
            public void CallBack() {
                User response = UOW.GetUserNetworkService().get_responseUser();
                if (response != null) {
                    if (response.getUserID().equals(CurrentUser.getUserID())) {
                        //here response and current user properties, they are all the same except profileimageurl which is only stored
                        //in sql database not firebase and findmee contacts which is only stored in firebase
                        CurrentUser.setID(response.getID());
                        CurrentUser.setUserID(response.getUserID());
                        CurrentUser.setEmail(response.getEmail());
                        CurrentUser.setMobile(response.getMobile());
                        CurrentUser.setUserName(response.getUserName());
                        CurrentUser.setProfileImageUrl(response.getProfileImageUrl());
                        CurrentUser.setLatitude(response.getLatitude());
                        CurrentUser.setLatitude(response.getLongitude());
                        CurrentUser.setToken(response.getToken());
                    }
                }
            }
        };

    }*/

    //Friend Request Handler-----------------------------------------
    public void ProcessFriendRequest(final User contact, final Context context){
        DatabaseReference ref = UsersDataRef().child(contact.getUserID());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                InitContactDetails(contact, dataSnapshot);
                if (CheckFriendAlreadyRequested()) {
                    String msg =  context.getResources().getString(R.string.friend_already_requested);
                    Toast.makeText(context,msg, Toast.LENGTH_LONG).show();
                    return;
                }

                if (IsOtherConnectionsExceeded()) {
                    String msg = context.getString(R.string.Other_user_friends_exceeded);
                    Toast.makeText(context,msg, Toast.LENGTH_LONG).show();
                    return;
                }
                RequestFriend();
                Toast.makeText(context, context.getResources().getText(R.string.friend_request_sent), Toast.LENGTH_LONG).show();
                //startActivity(new Intent(ConnectActivity.this, MainActivity.class));
                //finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void RequestFriend() {
        int count = GetEmergencyContactsCount();
        //handshakeHandler.RequestHandShake(CurrentUser,NewContact,count);
        friendrequestHandler.RequestFriend(CurrentUser,NewContact,count);
    }

    public boolean IsOtherConnectionsExceeded(){
        return NewContact_ConnectionsCount >= FriendRequestConstants.MAX_CONNECTIONS;
    }

    public int GetFriendRequests(DataSnapshot dataSnapshot){

        friendrequestHandler.FriendRequestsCallBack = new DataChangedCallBack() {
            @Override
            public void CallBack() {
                if(FriendRequestsCallBack != null){
                    FriendRequestsCallBack.CallBack();
                }
            }
        };

        //return handshakeHandler.GetHandShakeRequests(dataSnapshot);
        return friendrequestHandler.GetFriendRequests(dataSnapshot);
    }

    public void DeleteFriendRequest(User contact){
        //handshakeHandler.DeleteHandShakeRequest(CurrentUser,contact);
        friendrequestHandler.DeleteFriendRequest(CurrentUser,contact);
        InitCurrentUser();
    }

    public void AcceptFriendRequest(User NewContact){
        //handshakeHandler.AcceptHandShake(CurrentUser,NewContact);
        friendrequestHandler.AcceptFriendRequest(CurrentUser, NewContact);
        InitCurrentUser();
    }

    public boolean CheckFriendAlreadyRequested(){
        //return handshakeHandler.CheckHandShakeAlreadyRequested(NewContact);
        return friendrequestHandler.CheckFriendAlreadyRequested(NewContact);
    }

    public boolean IsMyFriendRequestExist(){
        //return handshakeHandler.MyHandShakeRequestExist();
        return friendrequestHandler.MyFriendRequestExist();
    }

    public boolean FriendRequestReceivedExist(){
        //return handshakeHandler.OtherHandShakeRequestExist();
        return friendrequestHandler.FriendRequestsReceived_Exist();
    }

    public ArrayList<User> getOtherFriendRequests() {
        //return handshakeHandler.getOtherHandShakeRequests();
        return friendrequestHandler.getOtherFriendRequests();
    }

    public Boolean IsAlreadyConnected(String Number) {
        Number = Phone.ReplaceCountryCode(Number);

        if (IsEmergencyContactExist()) {
            for (EmergencyContact contact:emergencyHandler.EmergencyContacts) {

                String mobile = Phone.ReplaceCountryCode(contact.getMobile());
                if(mobile.equals(Number)){
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean IsFriendAlreadyRequested(User user) {
        return friendrequestHandler.CheckFriendAlreadyRequested(user);
    }

    public Boolean IsFriendRequestedByUser(User user) {
        return friendrequestHandler.IsFriendRequestedByUser(user);
    }

    //Emergency Handler
    public boolean IsEmergencyContactExist(){
        return emergencyHandler.IsEmergencyContactExist();
    }

    public void ListenEmergencyContactUpdates(){
        emergencyHandler.ListenEmergencyContactUpdates(CurrentUser);
    }

    public int GetEmergencyContactsCount(){
        return emergencyHandler.GetEmergencyContactsCount();
    }

    public void AddEmergencyRequests(EmergencyRequest request){
        emergencyHandler.AddEmergencyRequest(request);
    }

    public EmergencyRequest AddRequestFromDataSnapshot(DataSnapshot childData){
        EmergencyRequest e = emergencyHandler.AddRequestFromSnapshot(childData);

        if(e==null){
            return null;
        }
        AddEmergencyRequests(e);
        UpdateImageUrl(e);

        return e;
    }

    private void UpdateImageUrl(EmergencyRequest e){
        EmergencyRequest existingRequest = emergencyHandler.GetRequestByID(e.getEmergencyRequestID());

        if(existingRequest != null){
            if (e.getEmergencyImageUrl()!=null)
            {
                existingRequest.setEmergencyImageUrl(e.getEmergencyImageUrl());
            }
        }
    }

    public EmergencyContact GetEmergencyContactByID(String UserID){
        return emergencyHandler.GetEmergencyContactByID(UserID);
    }

    public void DeleteEmergencyContact(EmergencyContact contact){
        emergencyHandler.DeleteEmergencyContact(CurrentUser.getUserID(),contact);
    }

    public void RequestEmergency() {

        if (CurrentUser != null && IsEmergencyContactExist()) {

            //CurrentUser.setLatitude(_CurrentLocation.getLatitude());
            //CurrentUser.setLongitude(_CurrentLocation.getLongitude());
            emergencyHandler.RequestEmergency(CurrentUser);
        } else {
            //do something to handle kid doesn't exist
            //Toast.makeText(getContext,GlobalConstants.ERROR_OCCURED, Toast.LENGTH_SHORT).show ();
            //Utility.displayMessage(getContext, );
        }
    }

    public void SetEmergencyRequestStatus(String requestID){
        emergencyHandler.SetEmergencyRequestStatus(requestID,CurrentUser);
    }

    public void SetEmergencyRequestShowedStatus(String requestID){
        emergencyHandler.SetRequestShowStatus(requestID,CurrentUser);
    }

    public void SendEmergencyImage (Bitmap bitmap){
        emergencyHandler.SendEmergencyImage(bitmap);
    }
    public EmergencyRequest GetRequestByID(String ID){
        return emergencyHandler.GetRequestByID(ID);
    }

    public EmergencyRequest GetLatestRequest(){
        return emergencyHandler.GetLatestRequest();
    }

    public String LastEmergencyMessageSentByCurrentUser(){
        return emergencyHandler.get_LastEmergencyMsgSentByCurrentUser();
    }

    public ArrayList<EmergencyRequest> GetEmergencyRequests(){
        return emergencyHandler.EmergencyRequests;
    }
    public ArrayList<EmergencyContact> GetEmergencyContacts(){
        return emergencyHandler.EmergencyContacts;
    }

    //Database Ref
    public DatabaseReference UsersDataRef(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref =  database.getReference(GlobalConstants.USERS);
        return ref;
    }
    public DatabaseReference GetDataBaseRefByUserID(String UserID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref =  database.getReference(GlobalConstants.USERS).child(UserID);;
        return ref;
    }
    //Database Ref
    public DatabaseReference EmergencyDataRefByID(String ID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(ID);
        return ref;
    }
    //Database Ref
    public Query EmergencyDataQueryByID(String ID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query ref = database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(ID).limitToLast(20);
        return ref;
    }

    public DatabaseReference ResellerssDataRef(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref =  database.getReference(GlobalConstants.RESELLERS);
        return ref;
    }

}
