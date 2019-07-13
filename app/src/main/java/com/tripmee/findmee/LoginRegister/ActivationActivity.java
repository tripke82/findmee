package com.tripmee.findmee.LoginRegister;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.gsm.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Utility;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;

public class ActivationActivity extends AppCompatActivity {

    private Button btnRequestCode,btnActivate;
    private EditText textStoreID,textPasscode;
    private ProgressBar progressBar;
    private String mPasscode;
    final static int PERMISSION_ALL =1;
    final static String[] PERMISSIONS = {ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION, READ_PHONE_STATE, SEND_SMS};
    private boolean IsNumberExist = false;

    private UserGlobalHandler UserHandler;
    private String ResellerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        UserHandler = UserGlobalHandler.get_instance();

        btnRequestCode = (Button) findViewById(R.id.btn_Request_Passcode);
        btnActivate = (Button) findViewById(R.id.btnActivateAccount);
        textStoreID = (EditText) findViewById(R.id.editStoreID);
        textPasscode = (EditText) findViewById(R.id.editPasscode);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (ActivityCompat.checkSelfPermission(ActivationActivity.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,PERMISSIONS,PERMISSION_ALL);
        }

        btnRequestCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                String sMobile = textStoreID.getText().toString();

                if (TextUtils.isEmpty(sMobile)) {
                    sMobile = GlobalConstants.DEFAULT_STORE_NUMBER;
                }

                final String Number = sMobile;

                if (ActivityCompat.checkSelfPermission(ActivationActivity.this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                DatabaseReference ref = UserHandler.ResellerssDataRef();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        boolean IsExist = false;
                        for (DataSnapshot data: dataSnapshot.getChildren()) {
                            String sNumber = "";
                            if(data.child(GlobalConstants.CONTACT_NUMBER).getValue()!=null){
                                sNumber = data.child(GlobalConstants.CONTACT_NUMBER).getValue().toString();
                            }
                            if (Number.equals(sNumber)) {
                                IsExist = true;
                                mPasscode = Utility.random(6);
                                ResellerID = data.getKey();
                                Uri uri = Uri.parse("smsto:" + Number);
                                sendSMS(Number, "Safie app activation code:" + mPasscode);
                            }
                        }
                        //boolean IsExist = UserHandler.CheckNumberExists(Number,dataSnapshot);
                        if(IsExist){
                           //mPasscode = Utility.random(6);
                           //Uri uri = Uri.parse("smsto:" + Number);
                           //sendSMS(Number, "Safie app activation code:" + mPasscode);
                         }else{
                         Toast.makeText(ActivationActivity.this, "Reseller phone number is wrong. Please try again.", Toast.LENGTH_LONG).show();
                         }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
            }
        });
        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String EnteredPasscode = textPasscode.getText().toString();

                if (!EnteredPasscode.isEmpty()) {
                    if (mPasscode.equals(EnteredPasscode)){
                        //activate it
                        UserGlobalHandler UserHandler = UserGlobalHandler.get_instance();
                        UserHandler.ActivateUser();
                        SaveSaleTransaction();
                        Toast.makeText(ActivationActivity.this, "CONGRATS! Your account has been activated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ActivationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    // wrong passcode
                    Toast.makeText(ActivationActivity.this, "Incorrect Passcode. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        //PendingIntent pi = PendingIntent.getActivity(this, 0,
                //new Intent(this, ActivationActivity.class), 0);
        //SmsManager sms = SmsManager.getDefault();
        //sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    public void SaveSaleTransaction() {

        String salesDate = UserHandler.CurrentUser.getActivatedDate().toString();
        String SaleID = UserHandler.CurrentUser.getUserID();
        String name = UserHandler.CurrentUser.getUserName();
        String mobile = UserHandler.CurrentUser.getMobile();

        FirebaseDatabase Database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = Database.getReference(GlobalConstants.SALES);

        usersRef.child(ResellerID).child(SaleID).child(GlobalConstants.CUSTOMERNAME).setValue(name);
        usersRef.child(ResellerID).child(SaleID).child(GlobalConstants.CUSTOMERPHONE).setValue(mobile);
        usersRef.child(ResellerID).child(SaleID).child(GlobalConstants.PRICE).setValue(GlobalConstants.DEFAULT_PRICE);
        usersRef.child(ResellerID).child(SaleID).child(GlobalConstants.TRANSACTIONDATE).setValue(salesDate);
    }
}
