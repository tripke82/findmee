package com.tripmee.findmee.Models.SOS;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.annotations.SerializedName;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SOSRequestList {

    @SerializedName("sosrequest_list")
    private ArrayList<SOSRequest> sosRequests;

    private ArrayList<SOSImage> sosImages;

    private ArrayList<User> Users;


    public ArrayList<SOSRequest> getSOSRequestList() {
        return sosRequests;
    }

    public ArrayList<User> getUsers() {
        return Users;
    }

    public void setSOSRequestList(ArrayList<SOSRequest> sosRequestList) {
        this.sosRequests = sosRequestList;
    }

    public void SortByDate(){
        Collections.sort(sosRequests, new Comparator<SOSRequest>() {
            public int compare(SOSRequest o1, SOSRequest o2) {
                return o2.getDateTime().compareTo(o1.getDateTime());
            }
        });
    }

    public void Add(SOSRequest sosRequest, Bitmap bitmap){
        //This function updates the list after an SOS request is added by the user
        //Please note that after the sos request is posted, instead of pulling the data & image
        //from the database, only data is pulled from database & image is updated on client side to increase speed

        sosRequests.add(sosRequest);
        SOSImage sosImage = new SOSImage(sosRequest.getId(),sosRequest.getImageUrl());

        if(bitmap != null){
            sosImage.set_BitmapImage(bitmap);
        }else {
            sosImage.LoadImage();
        }
        sosImages.add(sosImage);
    }

    public void LoadImages(){
        sosImages = new ArrayList<SOSImage>();

        for (SOSRequest sos:sosRequests) {
            SOSImage sosImage = new SOSImage(sos.getId(),sos.getImageUrl());
            sosImage.LoadImage();
            sosImages.add(sosImage);
        }
    }

    public void LoadUsers(){

        Users = new ArrayList<User>();

        for (SOSRequest sos:sosRequests) {
            DatabaseReference ref = UserGlobalHandler.get_instance().GetDataBaseRefByUserID(sos.getUserID());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user =  UserGlobalHandler.get_instance().GetUserByDataSnapShot(dataSnapshot);
                    Users.add(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public Bitmap GetSOSImage(SOSRequest sosRequest){
        for (SOSImage sosImage:sosImages) {
            if (sosImage.getID() == sosRequest.getId() && sosImage.get_ImageUrl().equals(sosRequest.getImageUrl()) ){
                return sosImage.get_BitmapImage();
            }
        }
        return null;
    }

    public Bitmap GetProfileImage(SOSRequest sosRequest){
        for (User user:Users) {

            if(user == null ){
                return null;
            }

            if (Utility.IsStringEqual(user.getUserID(), sosRequest.getUserID())){
                return user.GetProfileImage();
            }
        }
        return null;
    }

    public void Delete(Integer ID){

        //Log.e("Before Deleted", "ID " + String.valueOf(ID));
        for(int i=0; i<sosRequests.size(); i++){
            SOSRequest sos = sosRequests.get(i);
            if(ID == sos.getId()){
                //Log.e("Deleted", "ID" + String.valueOf(sos.getId()));
                //Log.e("Deleted", "sos" + sos.getMessage());
                sosRequests.remove(i);
            }
        }

        for(int i=0; i<sosImages.size(); i++){
            SOSImage sosImage = sosImages.get(i);
            if(ID == sosImage.getID()){
                //Log.e("Deleted", "sos" + sosImage.get_ImageUrl());
                sosImages.remove(i);
            }
        }
    }
}
