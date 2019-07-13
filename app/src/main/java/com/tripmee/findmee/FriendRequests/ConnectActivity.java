package com.tripmee.findmee.FriendRequests;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private AutoCompleteTextView mNameView;
    private EditText mMobileView;
    private View mProgressView;
    private UserGlobalHandler UserHandler;

    private ArrayAdapter<String> mContactsAdapter;
    private ArrayList<String> mContactNames;
    private ArrayList<String> mContactNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mNameView = (AutoCompleteTextView) findViewById(R.id.editContactName);
        mMobileView = (EditText) findViewById(R.id.editContactNumber);

        SetUpContactList();

        Button mRequestButton = (Button) findViewById(R.id.btnRequestFriend);
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Request_Connection();
            }
        });

        mProgressView = (ProgressBar)findViewById(R.id.progressBar);

    }

    private void Request_Connection(){

        // Reset errors.
        mNameView.setError(null);
        mMobileView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String mobile = mMobileView.getText().toString();

        mProgressView.setVisibility(View.VISIBLE);
        boolean cancel = false;
        View focusView = null;
        UserHandler = UserGlobalHandler.get_instance();


        /*if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else */

        if (!isMobileValid(mobile)) {
            mMobileView.setError(getString(R.string.error_invalid_mobile));
            focusView = mMobileView;
            cancel = true;
        }else if (UserHandler.CurrentUser.getMobile().equals(mobile)){
            mMobileView.setError(getString(R.string.error_invalid_mobile));
            focusView = mMobileView;
            cancel = true;
        }else if (UserHandler.IsAlreadyConnected(mobile)){
            mMobileView.setError(getString(R.string.error_already_connected));
            focusView = mMobileView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            mProgressView.setVisibility(View.GONE);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            mobile = Phone.ReplaceCountryCode(mobile);
            final String Number = mobile;
            mMobileView.setText(Number);

            CheckUserExist(mobile, UserHandler.CurrentUser.getToken());
            //ProcessHandshake_BackUp(Number);
        }

    }

    private boolean isAlreadyConnected(String Number) {

        return false;
    }

    private boolean isMobileValid(String mNumber) {
        //TODO: Replace this with your own logic

        return mNumber.length() > 10;
    }

    private void CheckUserExist(final String mobile, final String token){

        UnitOfWork UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
        UOW._RetrofitService.getUserByMobile(mobile,token, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                final User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) {
                    Utility.displayMessage(getBaseContext(),"User found:" + responseUser.getUserName());
                    ProcessHandShakeRequest(responseUser);
                } else {               //User doesn't exist
                    mProgressView.setVisibility(View.GONE);
                    Toast.makeText(ConnectActivity.this, "This user hasn't joined the network. A message has" +
                            "been sent to download our mobile app.", Toast.LENGTH_LONG).show();
                    //startActivity(new Intent(ConnectActivity.this, MainActivity.class));
                    String Message = "Hi " + mNameView.getText().toString() + ", " + getApplicationContext().getString(R.string.SMS_invitation);
                    Phone.LunchSMSActivity(ConnectActivity.this,Message, mobile);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void ProcessHandShakeRequest(final User contact){
        DatabaseReference ref = UserHandler.UsersDataRef().child(contact.getUserID());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserHandler.InitContactDetails(contact, dataSnapshot);
                if (UserHandler.CheckFriendAlreadyRequested()) {
                    String msg = getString(R.string.friend_already_requested);
                    Toast.makeText(getBaseContext(),msg, Toast.LENGTH_LONG).show();
                    return;
                }

                if (UserHandler.IsOtherConnectionsExceeded()) {
                    String msg = getString(R.string.Other_user_friends_exceeded);
                    Toast.makeText(getBaseContext(),msg, Toast.LENGTH_LONG).show();
                    return;
                }
                UserHandler.RequestFriend();
                Toast.makeText(getBaseContext(), getResources().getText(R.string.friend_request_sent), Toast.LENGTH_LONG).show();
                startActivity(new Intent(ConnectActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SetUpContactList(){

        if(!Permissions.IsPermissionGranted(this)){
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        }

        ContentResolver cr = getBaseContext().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            mContactNames = new ArrayList<>();
            mContactNumbers = new ArrayList<>();
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String Name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        mContactNames.add(Name);
                        mContactNumbers.add(contactNumber);
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }

        mContactsAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, mContactNames);

        mNameView.setAdapter(mContactsAdapter);
        mNameView.setThreshold(1);
        mNameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selected = (String) parent.getItemAtPosition(position);
                int realPosition = 0;

                for (String name:mContactNames) {

                    if(name.equals(selected)){
                        String Number = mContactNumbers.get(realPosition);
                        Number = Phone.ReplaceCountryCode(Number);
                        mMobileView.setText(Number);
                    }
                    realPosition += 1;
                }

            }
        });
    }
}

