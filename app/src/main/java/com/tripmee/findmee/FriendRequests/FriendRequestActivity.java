package com.tripmee.findmee.FriendRequests;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.tripmee.findmee.FriendRequests.ConnectActivity;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.MyContact;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Phone;

import java.util.ArrayList;

public class FriendRequestActivity extends AppCompatActivity {

    private ListView ContactslistView;
    private Button btnAdd;
    private SearchView SearchView;
    private Fragment selectedfragment;

    private com.tripmee.findmee.FriendRequests.PhoneContactAdapter _ContactAdapter;

    private UserGlobalHandler _UserHandler;
    private User _CurrentUser;
    private ArrayList<MyContact> _Contacts;

    /*private BottomNavigationView navigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedfragment = new HomeFragment();
                    loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_crimemap:
                    selectedfragment = new CrimeMapFragment();
                    loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_friendrequest:
                    selectedfragment = new FriendRequestListFragment();
                    //loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_notifications:
                    selectedfragment = new NotificationFragment();
                    loadFragment(selectedfragment);
                    return true;
            }
            return false;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        _UserHandler = UserGlobalHandler.get_instance();

        /*navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigationView.setSelectedItemId(R.id.navigation_friendrequest);*/

        ContactslistView = (ListView)findViewById(R.id.contacts_listview);
        btnAdd = (Button)findViewById(R.id.btn_Add);
        SearchView = (SearchView) findViewById(R.id.contacts_searchview);
        _CurrentUser = UserGlobalHandler.get_instance().CurrentUser;

        Init_btnAdd();

        SetUpContactList();
        Init_Search();

    }

    private void SetUpContactList(){

        if(!Permissions.IsPermissionGranted(this)){
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        }

        ContentResolver cr = getBaseContext().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            _Contacts = new ArrayList<MyContact>();

            int index = 1;
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

                        if(!Phone.SameMobileNumber(_CurrentUser.getMobile(), contactNumber)) {
                            MyContact contact = new MyContact(String.valueOf(index), contactNumber);
                            contact.setUserName(Name);
                            boolean IsAdded = _UserHandler.IsAlreadyConnected(contact.getMobile());
                            boolean IsRequested = _UserHandler.IsFriendAlreadyRequested(contact);
                            boolean IsRequestReceived = _UserHandler.IsFriendRequestedByUser(contact);

                            contact.setFriendAdded(IsAdded);
                            contact.setFriendRequested(IsRequested);
                            contact.setFriendRequestReceived(IsRequestReceived);

                            _Contacts.add(contact);
                            index += 1;

                            break;
                        }
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }

        _ContactAdapter = new com.tripmee.findmee.FriendRequests.PhoneContactAdapter(this, _Contacts);
        ContactslistView.setAdapter(_ContactAdapter);
    }

    private void Init_btnAdd(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendRequestActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });
    }

    private void Init_Search(){
        SearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                _ContactAdapter.getFilter().filter(query);
                return true; // handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == "" || newText.isEmpty()) {
                    _ContactAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    /*private void LoadMainActivity(Integer selectedindex){

        Intent intent = new Intent(FriendRequestActivity.this, MainActivity.class);
        intent.putExtra("SelectedIndex", selectedindex);
        startActivity(intent);
        finish();
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        try {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }*/
}
