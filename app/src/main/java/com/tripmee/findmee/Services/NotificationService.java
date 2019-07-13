package com.tripmee.findmee.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Utilities.NotificationUtility;

public class NotificationService extends Service {

    private UserGlobalHandler UserHandler;
    private Context _Context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UserHandler = UserGlobalHandler.get_instance();
        Log.d("230182", "NotificationService created");

        ListenEmergencyRequests(this);

    }

    @Override
    public void onDestroy() {

        Log.d("230182", "NotificationService destoryed");
        super.onDestroy();

    }

    private void ListenEmergencyRequests(final Context context){


        User user = UserHandler.CurrentUser;

        if (user != null) {
            //final Query ref = UserHandler.EmergencyDataRefByID(user.getUserID()).limitToLast(1);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final Query ref = database.getReference(GlobalConstants.EMERGENCY_REQUEST).child(user.getUserID()).child(GlobalConstants.REQUESTS).limitToLast(1);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (FirebaseAuth.getInstance().getCurrentUser() == null){ //if user is logged out remove the listener
                        ref.removeEventListener(this);
                        return;
                    }

                    int i = (int) dataSnapshot.getChildrenCount();
                    for (DataSnapshot child:dataSnapshot.getChildren()) {
                        if (child.getChildrenCount() >= GlobalConstants.EMERGENCY_REQUEST_MIN_FIELDS){ //only add if data is complete
                            EmergencyRequest e = UserHandler.AddRequestFromDataSnapshot(child);

                            if (e != null) {
                                if (e.IsValidToPushNotification()) {

                                    UserHandler.SetEmergencyRequestShowedStatus(e.getEmergencyRequestID());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationUtility.notificationCallForOreo(e.getEmergencyMessage(), e.getEmergencyRequestID(), context);
                                    }else{
                                        NotificationUtility.notificationCall(e.getEmergencyMessage(), e.getEmergencyRequestID(), context);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                        return;
                    }
                    //Log.e("Database Error: ", databaseError.getMessage());
                    throw databaseError.toException();
                }
            });
        }
    }
}
