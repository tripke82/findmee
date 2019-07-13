package com.tripmee.findmee.Home;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telephony.gsm.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.CrimeReport.CrimeTypeFragment;
import com.tripmee.findmee.Dashboard.DashboardFragment;
import com.tripmee.findmee.FriendRequests.ConnectActivity;
import com.tripmee.findmee.FriendRequests.FriendRequestListFragment;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Camera;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.LocationUtility;
import com.tripmee.findmee.Utilities.Utility;
import com.skyfishjy.library.RippleBackground;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION, permission.READ_PHONE_STATE};

    //private OnFragmentInteractionListener mListener;
    private UserGlobalHandler UserHandler;
    private RippleBackground rippleBackground;
    static final int CAPTURE_IMAGE_REQUEST = 1; //image part
    String mCurrentPhotoPath;
    File photoFile = null;

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    private CircleImageView ProfileImageView;
    private TextView textviewLastEmergencyMessage;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserHandler = UserGlobalHandler.get_instance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        updateValuesFromBundle(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        if (!IsPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        textviewLastEmergencyMessage = (TextView) rootView.findViewById(R.id.lastEMessageTextView);
        textviewLastEmergencyMessage.setText(UserHandler.LastEmergencyMessageSentByCurrentUser());
        InitLocationService();

        FloatingActionButton reportButton =(FloatingActionButton)rootView.findViewById(R.id.reportButton);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReportCrimeFragment();

                ViewCrimeTypeFragment();
            }
        });

        //Init_ProfileImage(rootView);

        rippleBackground =(RippleBackground)rootView.findViewById(R.id.btnRequestEmergencyRipple);
        Button btnRequest=(Button)rootView.findViewById(R.id.btnRequestEmergency);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rippleBackground.startRippleAnimation();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //wait
                    }
                }, 3000);

                RequestEmergency();

                //rippleBackground.stopRippleAnimation();
            }
        });

        if(UserHandler.isFireBaseDataLoaded()){

            Init_ProfileImage(rootView);
            //if no friend go to friend request
            GoToFriendRequestFragment();
        }else{

            UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() {
                @Override
                public void CallBack() {
                    //if no friend go to friend request
                    Init_ProfileImage(rootView);
                    GoToFriendRequestFragment();
                }
            };
        }

        return rootView;
        //return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void GoToFriendRequestFragment(){

        if(!UserHandler.IsEmergencyContactExist()){
            BottomNavigationView navigationView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
            navigationView.getMenu().getItem(2).setChecked(true);
            loadFragment(new FriendRequestListFragment());
        }
    }

    private void RequestEmergency(){

        if (!UserHandler.IsEmergencyContactExist() && !UserHandler.IsMyFriendRequestExist()) {
            Intent intent = new Intent(getActivity(), ConnectActivity.class);
            startActivity(intent);
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                captureImage();
            } else {
                captureImage2();
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Utility.displayMessage(getContext(), "No permission to access phone's location");
            }

            if (UserHandler.get_CurrentLocation()!=null) {

                UserHandler.RequestEmergency();

                String msg = getString(R.string.emergency_message_sent) + ". Your location: Latitude:"
                        + UserHandler.get_CurrentLocation().getLatitude() + " Longitude:" + UserHandler.get_CurrentLocation().getLongitude();
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                String lastEmsg = UserHandler.LastEmergencyMessageSentByCurrentUser();
                textviewLastEmergencyMessage.setText(lastEmsg);

                SendMutiEmergencySMS();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            UserHandler = UserGlobalHandler.get_instance();
            UserHandler.SendEmergencyImage(myBitmap);

            //UnitOfWork UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
            //UOW.SendSOSRequest(photoFile.getAbsolutePath(), photoFile.getName(), myBitmap,null);
            Toast.makeText(getActivity(), getString(R.string.captured_photo_sent), Toast.LENGTH_SHORT).show();
        }
        if(rippleBackground.isRippleAnimationRunning()) {
            rippleBackground.stopRippleAnimation();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {
        //mListener.onFragmentInteraction(uri);
        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
        //AddPlaces();
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void SendMutiEmergencySMS(){

        if (UserHandler == null) {
            return;
        }

        for (EmergencyContact emergencyContact: UserHandler.GetEmergencyContacts()){

            String SMSMessage = Utility.GetEmergencySMSText(emergencyContact.getUserName());

            sendSMS(emergencyContact.getMobile(), SMSMessage);
        }
    }

    private void InitLocationService(){

        boolean IsCoarseLocationAllowed = ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean IsFineLocationAllowed = ContextCompat.checkSelfPermission(getActivity(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if(!IsCoarseLocationAllowed || !IsFineLocationAllowed){
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        createLocationRequest();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            if (UserHandler != null){
                                UserHandler.setCurrentLocation(location);
                            }
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                int count = 0;
                Location l = locationResult.getLastLocation();
                boolean isLocationChanged = LocationUtility.IsLocationChanged(UserHandler.get_CurrentLocation(),l, 1.0);

                if(isLocationChanged){
                    //AddPlaces();
                    String sLocation = "Latitude:" + l.getLatitude() + " Longitude:" + l.getLongitude();
                }
                UserHandler.setCurrentLocation(l);

            };
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {

        boolean IsCoarseLocationAllowed = ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean IsFineLocationAllowed = ContextCompat.checkSelfPermission(getActivity(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if(!IsCoarseLocationAllowed || !IsFineLocationAllowed){
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        if(IsCoarseLocationAllowed && IsFineLocationAllowed) {
            mRequestingLocationUpdates = true;
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }

        // ...
    }


    //@Override
    //public void onAttach(Context context) {
    //super.onAttach(context);
    //if (context instanceof OnFragmentInteractionListener) {
    // mListener = (OnFragmentInteractionListener) context;
    //} else {
    //throw new RuntimeException(context.toString()
    //+ " must implement OnFragmentInteractionListener");
    //}
    //}
    /* Capture Image function for 4.4.4 and lower. Not tested for Android Version 3 and 2 */
    private void captureImage2() {

        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            //photoFile = createImageFile2();
            photoFile = Camera.createImageFile2(getActivity());
            if (photoFile != null) {
                Utility.displayMessage(getContext(), photoFile.getAbsolutePath());
                //Log.i("Image uri", photoFile.getAbsolutePath());
                Uri photoURI = Uri.fromFile(photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
            }
        } catch (Exception e) {
            Utility.displayMessage(getContext(), "Camera is not available." + e.toString());
        }
    }

    private void captureImage() {

        String[] Perms = new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE};
        if (ActivityCompat.checkSelfPermission(getActivity(), permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(Perms, PERMISSION_ALL);
        } else {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) { // need to run on real device
                // Create the File where the photo should go
                try {

                    //photoFile = createImageFile();
                    photoFile = Camera.createImageFile(getActivity());

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        //Uri photoURI = FileProvider.getUriForFile(this,"com.vlemonn.blog.captureimage.fileprovider",photoFile);

                        Uri photoURI = FileProvider.getUriForFile(getActivity(),getContext().getPackageName(),photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    Utility.displayMessage(getContext(), ex.getMessage().toString());
                }


            } else {
                Utility.displayMessage(getContext(), "Camera is not available");
            }
        }

    }

    private void Init_ProfileImage(View rootView){
        ProfileImageView = (CircleImageView) rootView.findViewById(R.id.profile_photo);

        ProfileImageView.setImageResource(R.drawable.default_profile);

        if(!Utility.StringIsBlankOrEmpty(UserHandler.CurrentUser.get_FireBaseProfileImage())){
            ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
        }

        /*
        UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() {
            public void CallBack() {
                if(!Utility.StringIsBlankOrEmpty(UserHandler.CurrentUser.get_FireBaseProfileImage())){
                    ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
                }
            }
        };*/

        ProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new DashboardFragment());
            }
        });
    }

    private boolean IsPermissionGranted(){

        if (ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ){

            if(ContextCompat.checkSelfPermission(getActivity(), READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                //Log.v("All Permission", " Granted");
                return true;
            }
            else { return false;}
        }
        else{
            //Log.v("Permission", "NOT Granted");
            return false;
        }
    }

    private void ViewCrimeTypeFragment() {
        CrimeTypeFragment fragment = new CrimeTypeFragment();
        Bundle arguments = new Bundle();
        //arguments.putString(GlobalConstants.EMERGENCY_REQUEST_ID, requestID);
        //fragment.setArguments(arguments);

        try {
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        try {
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }

    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        getActivity().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getContext(), "SMS sent",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        //Toast.makeText(getContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        //Toast.makeText(getContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        //Toast.makeText(getContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        //Toast.makeText(getContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        getActivity().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Toast.makeText(getContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    /*
    private void AddPlaces() {
        if (mGoogleMap == null) {
            return;
        }

        if (IsPermissionGranted()) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count = likelyPlaces.getCount();
                                if (likelyPlaces.getCount() < GlobalConstants.MARKER_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = GlobalConstants.MARKER_MAX_ENTRIES;
                                }

                                int i = 0;

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                                    i = AddPlace(placeLikelihood,i);
                                    //i++;
                                    if (i > (GlobalConstants.MARKER_MAX_ENTRIES - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();


                            } else {

                            }
                        }
                    });
        } else {
            // The user has not granted permission.


            // Add a default marker, because the user hasn't selected a place.
            //mGoogleMap.addMarker(new MarkerOptions()
            //.title(getString(R.string.default_info_title))
            //.position(mDefaultLocation)
            //.snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            //getLocationPermission();
        }
    }

    private int AddPlace(PlaceLikelihood placeLikelihood, int count){

        // Build a list of likely places to show the user.
        String LikelyPlaceName = (String) placeLikelihood.getPlace().getName();
        String LikelyPlaceAddress = (String) placeLikelihood.getPlace()
                .getAddress();
        String LikelyPlaceAttribution = (String) placeLikelihood.getPlace()
                .getAttributions();
        LatLng latLng = placeLikelihood.getPlace().getLatLng();
        String markerSnippet = LikelyPlaceAddress;

        for (int i:placeLikelihood.getPlace().getPlaceTypes()) {
            try {
                String type = getPlaceTypeForValue(i);
                //Toast.makeText(getActivity(), type, Toast.LENGTH_SHORT).show();
            }catch(Exception ex){
                String s = ex.getMessage();
            }
        }


        if (LikelyPlaceAttribution != null) {
            markerSnippet = markerSnippet + "\n" + LikelyPlaceAttribution;
        }

        if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_DOCTOR)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_clinic))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;

        }else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_HOSPITAL)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_hospital))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_ATM)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_atm))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_BANK)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_bank))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        } else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_POLICE)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_police))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_LOCAL_GOVERNMENT_OFFICE)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_govoffice))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_EMBASSY)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_embassy))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }
        else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_RESTAURANT)||
                placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_CAFE)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_restaurant))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }
        else if(placeLikelihood.getPlace().getPlaceTypes().contains(Place.TYPE_BAR)){
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_bar))
                    .title(LikelyPlaceName)
                    .position(latLng)
                    .snippet(markerSnippet));
            count += 1;
        }
        return count;
    }

    private String getPlaceTypeForValue(int value) throws Exception {
        Field[] fields = Place.class.getDeclaredFields();
        String name;
        for (Field field : fields) {
            name = field.getName().toLowerCase();
            if (name.startsWith("type_") && field.getInt(null) == value) {
                return name.replace("type_", "");
            }
        }
        throw new IllegalArgumentException("place value " + value + " not found.");
    }

    private void InitPlaces(){
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
    }*/
}
