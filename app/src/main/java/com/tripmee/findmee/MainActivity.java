package com.tripmee.findmee;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.R;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.CrimeReport.CrimeMapFragment;
import com.tripmee.findmee.EmergencyRequests.LocationActivity;
import com.tripmee.findmee.EmergencyRequests.LocationFragment;
import com.tripmee.findmee.EmergencyRequests.NotificationFragment;
import com.tripmee.findmee.EmergencyRequests.ViewImageFragment;
import com.tripmee.findmee.FriendRequests.FriendRequestListFragment;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Home.HomeFragment;
import com.tripmee.findmee.LoginRegister.RegisterActivity;
import com.tripmee.findmee.Mainfeed.MainfeedFragment;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.Services.BackgroundLocationService;
import com.tripmee.findmee.Services.ExternalEventReceiver;
import com.tripmee.findmee.Services.ScreenOnOffBackgroundService;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Utility;

public class MainActivity extends AppCompatActivity {

    private UserGlobalHandler UserHandler;
    private UnitOfWork _UOW;
    private BottomNavigationView navigationView;

    private boolean IsEmergencyBadgeAdded = false;
    private boolean IsFriendRequestBadgeAdded = false;
    private View EmergencyBadge;
    private View FriendRequestBadge;
    private View CrimeReportBadge;
    private Integer CrimeMapMenuIndex = 1; //hardcoded
    private Integer FriendRequestMenuIndex = 2; //hardcoded
    private Integer EmergencyMenuIndex = 3; //hardcoded
    private Integer UPDATE_REQUEST_CODE = 155;
    private Fragment selectedfragment;
    private int selectedItemId;


    private ExternalEventReceiver _EventReceiver;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            RefreshBadgeCount();

            selectedItemId = item.getItemId();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedfragment = new HomeFragment();
                    loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_crimemap:

                    if(CrimeReportBadge != null){
                        CrimeReportBadge.setVisibility(View.INVISIBLE);
                    }

                    selectedfragment = new CrimeMapFragment();
                    loadFragment(selectedfragment);
                    return true;
                /*case R.id.navigation_mainfeed:
                    selectedfragment = new MainfeedFragment();
                    loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_dashboard:
                    selectedfragment = new DashboardFragment();
                    loadFragment(selectedfragment);
                    return true;*/
               case R.id.navigation_friendrequest:
                    selectedfragment = new FriendRequestListFragment();
                    loadFragment(selectedfragment);
                    return true;
                case R.id.navigation_notifications:
                    selectedfragment = new NotificationFragment();
                    loadFragment(selectedfragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserHandler = UserGlobalHandler.get_instance();

        CheckFacebookUserisRegistered();
        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
        LoadEmergencyRequests(); //single query
        ListenEmergencyRequests(); // listen to new findmee request
        //ListenUserStatusChanged(); user status changed is irrelevant as the app is free at the moment
        ListenFriendRequests();

        //boolean Isgranted = IsPermissionGranted();
        boolean Isgranted = Permissions.IsPermissionGranted(this);

        if (Build.VERSION.SDK_INT >= 23) {

            if(!Isgranted) {
                requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
            }

        } else {

            if(!Isgranted){
                //requestPermissions(PERMISSIONS,PERMISSION_ALL);
            }

        }

        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            //((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
        }
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        IsEmergencyRequestSentViaPowerButton();

        _UOW.crimeReportsLoadedCallBack = new ActionCompleteCallBack() {
            @Override
            public void CallBack() {
                PopulateCrimeReportBadges();
            }
        };

        //ListenPowerButtonClicks();

        Intent backgroundService = new Intent(getApplicationContext(), ScreenOnOffBackgroundService.class);
        startService(backgroundService);

        InitSelectedFragment();

        Intent intent = new Intent(getApplicationContext(), BackgroundLocationService.class);
        startService(intent);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Double Lat = Double.valueOf(intent.getStringExtra("latutide"));
            Double Lng = Double.valueOf(intent.getStringExtra("longitude"));
            Location l = new Location("");
            l.setLatitude(Lat);
            l.setLongitude(Lng);
            UserHandler.setCurrentLocation(l);
            //String msg = "Background Location Service Lat :" + intent.getStringExtra("latutide") + "Long : " + intent.getStringExtra("longitude");
            //Utility.displayMessage(getBaseContext(),  msg);
        }
    };

    private void ListenPowerButtonClicks(){

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        _EventReceiver = new ExternalEventReceiver (this);
        registerReceiver(_EventReceiver, filter);

    }

    private void IsEmergencyRequestSentViaPowerButton() {

        Intent intent = getIntent();
        String msg = intent.getStringExtra(GlobalConstants.EMERGENCY_REQUEST);

        if(!Utility.StringIsBlankOrEmpty(msg)){
            Utility.displayMessage(getBaseContext(), msg);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(BackgroundLocationService.str_receiver));
        ListenPowerButtonClicks();
        CheckForAppUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(_EventReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // ...
        super.onSaveInstanceState(outState);
    }

    private void CheckFacebookUserisRegistered(){

        if(Utility.IsFacebookLoggedin(getIntent())){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference ref = UserHandler.GetDataBaseRefByUserID(user.getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        //if user doesn't exists
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        intent.putExtra("Facebook", "Loggedin");
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void InitSelectedFragment(){
        Intent intent = getIntent();
        String SelectedFragment = intent.getStringExtra(GlobalConstants.SELECTED_FRAGMENT);

        if(Utility.IsStringEqual(SelectedFragment, GlobalConstants.MAINFEED_FRAGMENT)){

            selectedfragment = new MainfeedFragment();

        }else if(Utility.IsStringEqual(SelectedFragment, GlobalConstants.VIEWIMAGE_FRAGMENT)){

            selectedfragment = new ViewImageFragment();
            Bundle arguments = new Bundle();
            String requestID = intent.getStringExtra(GlobalConstants.EMERGENCY_REQUEST_ID);
            arguments.putString( GlobalConstants.EMERGENCY_REQUEST_ID , requestID);
            selectedfragment.setArguments(arguments);

        }else if(Utility.IsStringEqual(SelectedFragment, GlobalConstants.LOCATION_FRAGMENT)){

            selectedfragment = new LocationFragment();
            Bundle arguments = new Bundle();
            String requestID = intent.getStringExtra(GlobalConstants.EMERGENCY_REQUEST_ID);
            arguments.putString( GlobalConstants.EMERGENCY_REQUEST_ID , requestID);
            selectedfragment.setArguments(arguments);

        }else if(Utility.IsStringEqual(SelectedFragment,GlobalConstants.NOTIFICATION_FRAGMENT))
        {
            selectedfragment = new NotificationFragment();
        }

        if (selectedfragment == null){
            selectedfragment = new HomeFragment();
        }
        loadFragment(selectedfragment);
    }

    private void ListenEmergencyRequests(){


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

                    //dataSnapshot.getChildren()
                    int i = (int) dataSnapshot.getChildrenCount();
                    for (DataSnapshot child:dataSnapshot.getChildren()) {
                        if (child.getChildrenCount() >= GlobalConstants.EMERGENCY_REQUEST_MIN_FIELDS){ //only add if data is complete
                            EmergencyRequest e = UserHandler.AddRequestFromDataSnapshot(child);

                            if (e != null) {
                                //if (!e.IsChecked()) {
                                if (e.IsValidToPushNotification()) {
                                    //notificationCall(e.getEmergencyMessage(), e.getEmergencyRequestID());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        //notificationCall(e.getEmergencyMessage(), e.getEmergencyRequestID());
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        notificationCallForOreo(e.getEmergencyMessage(), e.getEmergencyRequestID());
                                    }else{
                                        notificationCall(e.getEmergencyMessage(), e.getEmergencyRequestID());
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

    private void LoadEmergencyRequests() { // call at main activity onCreate()

        User user = UserHandler.CurrentUser;

        if (user != null) {

            final Query ref = UserHandler.EmergencyDataRefByID(user.getUserID()).child(GlobalConstants.REQUESTS).limitToLast(20);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   for(DataSnapshot childdata : dataSnapshot.getChildren()){

                     if (FirebaseAuth.getInstance().getCurrentUser() == null){ //if user is logged out remove the listener
                         ref.removeEventListener(this);
                         return;
                     }

                      if (childdata.getChildrenCount() >= GlobalConstants.EMERGENCY_REQUEST_MIN_FIELDS){ //only add if data is complete
                          EmergencyRequest e = UserHandler.AddRequestFromDataSnapshot(childdata);
                       }
                   }
                   int count = UserHandler.GetNotificationCount(true);
                   PopulateBadgeCount(count, EmergencyMenuIndex);
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

    private void ListenFriendRequests() { // call at main activity onCreate()

        User user = UserHandler.CurrentUser;

        if (user != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference ref = database.getReference(FriendRequestConstants.FRIEND_REQUEST).child(user.getUserID());

                ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int count = UserHandler.GetFriendRequests(dataSnapshot);
                    PopulateBadgeCount(count, FriendRequestMenuIndex);
                    //BottomNavigationView.
                    loadFragment(selectedfragment);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if event occured after user logged out then exit the function
                        return;
                    }
                    throw databaseError.toException();
                    //Log.e("Database Error: ", databaseError.getMessage());
                }
            });
        }
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
    }

    public void notificationCallForOreo(String message, String RequestID){
        String title = this.getString(R.string.title_emergency_request);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/warningsound");


        // Sets an ID for the notification, so it can be updated.
        int notifyID = 1;
        String CHANNEL_ID = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL_ID;// The id of the channel.
        CharSequence name = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL;// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setSmallIcon(R.drawable.ic_emergency)
                .setColorized(true)
                //.setColor(Color.BLUE)
                .setBadgeIconType(Notification.BADGE_ICON_LARGE)
                .setContentTitle(title)
                .setContentText(message)
                //.setSound(soundUri)
                .setChannelId(CHANNEL_ID).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder() // new code for oreo
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            //delete old channels
            for (NotificationChannel channel: mNotificationManager.getNotificationChannels()) {
                String channelid = channel.getId();
                Log.d("channel", channelid);
                if(!channelid.equals(CHANNEL_ID)){
                    mNotificationManager.deleteNotificationChannel(channelid);
                }
            }

            // Configure the notification channel.
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                //mChannel.setCol
                mChannel.setSound(soundUri, attributes); // This is IMPORTANT // ends new code
                mChannel.setBypassDnd(true);
                mChannel.setShowBadge(true);
                mChannel.setImportance(importance);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        Intent i = new Intent(this, LocationActivity.class);
        i.putExtra("RequestID", RequestID);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;

        // Issue the notification.
        mNotificationManager.notify(notifyID , notification);

    }

    public void notificationCallForOreoBackup(String message, String RequestID){
        String title = this.getString(R.string.title_emergency_request);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/warningsound");

        // Sets an ID for the notification, so it can be updated.
        int notifyID = 1;
        String CHANNEL_ID = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL_ID;// The id of the channel.
        CharSequence name = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL;// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;


        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setSmallIcon(R.drawable.ic_emergency)
                .setColorized(true)
                //.setColor(Color.BLUE)
                .setBadgeIconType(Notification.BADGE_ICON_LARGE)
                .setContentTitle(title)
                .setContentText(message)
                //.setSound(soundUri)
                .setChannelId(CHANNEL_ID).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes attributes = new AudioAttributes.Builder() // new code for oreo
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            // Configure the notification channel.
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //mChannel.setCol
            mChannel.setSound(soundUri, attributes); // This is IMPORTANT // ends new code
            mChannel.setBypassDnd(true);
            mChannel.setShowBadge(true);
            mChannel.setImportance(importance);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotificationManager.createNotificationChannel(mChannel);

        }

        Intent targetIntent = new Intent(getApplicationContext(), LocationActivity.class);

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        targetIntent.putExtra("Notification", "Notification");
        Intent i = new Intent(this, LocationActivity.class);
        //i.putExtra("UserID", ContactID);
        i.putExtra(GlobalConstants.EMERGENCY_REQUEST_ID, RequestID);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;

        // Issue the notification.
        mNotificationManager.notify(notifyID , notification);

    }

    public void notificationCall(String message, String RequestID){

        String title = this.getString(R.string.title_emergency_request);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/warningsound");

        Notification notification = new NotificationCompat.Builder(this, "Channel 1")
                //.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                //.setLargeIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri)
                .build();


        Intent targetIntent = new Intent(getApplicationContext(), LocationActivity.class);

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //targetIntent.putExtra("Notification", "Notification");
        targetIntent.putExtra(GlobalConstants.SELECTED_FRAGMENT, GlobalConstants.NOTIFICATION_FRAGMENT);
        Intent i = new Intent(this, LocationActivity.class);
        //i.putExtra("UserID", ContactID);
        i.putExtra("RequestID", RequestID);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void PopulateBadgeCount(int count, int index){
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) navigationView.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(index);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        if (index == FriendRequestMenuIndex){

            if (!IsFriendRequestBadgeAdded){
                if(count > 0) { // if notifications exist add badget and settext count
                    FriendRequestBadge = LayoutInflater.from(this)
                            .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);
                    itemView.addView(FriendRequestBadge);
                    TextView countTextView = (TextView) FriendRequestBadge.findViewById(R.id.notificationsbadge);
                    countTextView.setText(String.valueOf(count));
                    IsFriendRequestBadgeAdded = true;
                }
            }else{

                if (count > 0) { //update count
                    TextView countTextView = (TextView) FriendRequestBadge.findViewById(R.id.notificationsbadge);
                    countTextView.setText(String.valueOf(count));
                    FriendRequestBadge.setVisibility(View.VISIBLE);
                }else if(count == 0){// hide badge
                    FriendRequestBadge.setVisibility(View.GONE);
                    IsFriendRequestBadgeAdded = false;
                }
            }
        }

        if (index == EmergencyMenuIndex){
            //Boolean BadgeAdded = false;

            if(EmergencyBadge != null){
                //BadgeAdded = true;
                if(EmergencyBadge.getVisibility() == View.GONE){
                    IsEmergencyBadgeAdded = false;
                }
            }
            if (!IsEmergencyBadgeAdded){
                if(count > 0) { // if notifications exist add badget and settext count
                    EmergencyBadge = LayoutInflater.from(this)
                            .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);
                    itemView.addView(EmergencyBadge);
                    TextView countTextView = (TextView) EmergencyBadge.findViewById(R.id.notificationsbadge);
                    countTextView.setText(String.valueOf(count));
                    IsEmergencyBadgeAdded = true;
                    //ShortcutBadger.applyCount(this, count);
                }
            }else{

                if (count > 0) { //update count
                    TextView countTextView = (TextView) EmergencyBadge.findViewById(R.id.notificationsbadge);
                    countTextView.setText(String.valueOf(count));
                }else if(count == 0){// hide badge
                    EmergencyBadge.setVisibility(View.GONE);
                }
            }
        }

    }

    private void RefreshBadgeCount(){
        if(UserHandler != null) {
            int count = UserHandler.GetNotificationCount(true);
            PopulateBadgeCount(count, EmergencyMenuIndex);
        }
    }

    private void PopulateCrimeReportBadges(){


        if(_UOW != null){
            if(_UOW.get_CrimeReportList() != null){
                if(_UOW.get_CrimeReportList().getCrimeReports() != null){

                    int count = _UOW.get_CrimeReportList().getCrimeReports().size();

                    if(count>0) {
                        BottomNavigationMenuView bottomNavigationMenuView =
                                (BottomNavigationMenuView) navigationView.getChildAt(0);
                        View v = bottomNavigationMenuView.getChildAt(CrimeMapMenuIndex);
                        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

                        CrimeReportBadge = LayoutInflater.from(this)
                                .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);
                        itemView.addView(CrimeReportBadge);
                        TextView countTextView = (TextView) CrimeReportBadge.findViewById(R.id.notificationsbadge);
                        countTextView.setText(String.valueOf(count));
                    }

                }
            }
        }
    }

    //Check App version update
    private void CheckForAppUpdates(){
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {

            int i = appUpdateInfo.availableVersionCode();
            //For a flexible update, use AppUpdateType.FLEXIBLE Request the update.
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {


                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.IMMEDIATE, this,UPDATE_REQUEST_CODE);
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    // The current activity making the update request.
                    // Include a request code to later monitor this update request.
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }else if(appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){

                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE,this, UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e(String.valueOf(resultCode), "Update flow failed! Result code: " + String.valueOf(resultCode));
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }
}
