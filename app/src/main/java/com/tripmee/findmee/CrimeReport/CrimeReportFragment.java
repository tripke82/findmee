package com.tripmee.findmee.CrimeReport;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.GlobalConstants.CrimeReportConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Models.CrimeReport.CrimeReportList;
import com.tripmee.findmee.Models.Geocoding.GeocodingTask;
import com.tripmee.findmee.Models.Geocoding.GetAddressByLocationTask;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.LocationUtility;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Utility;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrimeReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrimeReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrimeReportFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private UserGlobalHandler UserHandler;
    // TODO: Rename and change types of parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String _CrimeType;
    private String _ReportID;
    private CrimeReport _CrimeReport;
    private boolean _IsEdited = false;
    private boolean _IsImageChanged = false;
    private Location _EditedLocation;
    private CrimeReportList _CrimeReportList;
    private String _PreviousImagePath="";
    private UnitOfWork _UOW;

    private TextView mDisplayNameTextView;
    private TextView mLocationMarkerText;
    private EditText mCommentsEditText;
    private TextView mSelectDateTextView;
    private TextView mSelectTimeTextView;
    private TextView textViewPostButton;
    private TextView textViewCancelButton;
    private Switch mSwitchButton;
    private ProgressBar mProgressBar;
    private AutoCompleteTextView mSearchText;
    private ImageView mImageView;
    private ImageButton mbtnClearImage;
    //private MapView mMap;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private com.tripmee.findmee.CrimeReport.PlaceAutoCompleteAdapter mPlaceAutocompleteAdapter;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));


    private Location SelectedLocation;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private Bitmap mBitmap;
    private Uri mFilePath;;

    final Calendar myCalendar = Calendar.getInstance();
    private boolean IsShowYourName;
    private SimpleDateFormat mFormatter = new SimpleDateFormat("MMMM dd yyyy hh:mm aa");
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    private boolean _IsMapMovedprogrammatically = false;

    private OnFragmentInteractionListener mListener;

    public CrimeReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CrimeReportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CrimeReportFragment newInstance(String param1, String param2) {
        CrimeReportFragment fragment = new CrimeReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserHandler = UserGlobalHandler.get_instance();
        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
        _CrimeReportList = _UOW.get_CrimeReportList();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        updateValuesFromBundle(savedInstanceState);

        if (getArguments() != null) {
            _CrimeType = getArguments().getString("Type");

            _ReportID = getArguments().getString(CrimeReportConstants.REPORTID);

            if(!Utility.StringIsBlankOrEmpty(_ReportID)){
                _IsEdited = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_crime_report, container, false);

        mSwitchButton = rootView.findViewById(R.id.switchShowHideName);
        mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IsShowYourName = isChecked;
                if(isChecked){
                    mDisplayNameTextView.setText(UserHandler.CurrentUser.getUserName());
                }else{
                    mDisplayNameTextView.setText("");
                }
            }
        });

        if(!Permissions.IsPermissionGranted(getActivity())){
            requestPermissions(Permissions.PERMISSIONS,Permissions.PERMISSION_ALL);
        }

        mSearchText = (AutoCompleteTextView)rootView.findViewById(R.id.searchView) ;

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //mLocationMarkerText = (TextView) rootView.findViewById(R.id.locationMarkertext);
        mDisplayNameTextView = (TextView) rootView.findViewById(R.id.displayNameTextView);
        mCommentsEditText = (EditText) rootView.findViewById(R.id.commentEditText);
        mbtnClearImage = (ImageButton) rootView.findViewById(R.id.btnClearImage);

        Init_PostCancelButtons(rootView);

        Init_ImageView(rootView);

        if (!Permissions.IsPermissionGranted(getActivity())) {
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        }

        if(_IsEdited){
            Init_EditedReport();
        }

        InitLocationService();

        Init_SelectDateTime(rootView);

        Init_ClearImageButton();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.reportMapView);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    private void Init_EditedReport(){

        User user = UserHandler.CurrentUser;
        _CrimeReport = UnitOfWork.get_instance(user).GetReport(user.getUserID(), _ReportID);

        if(_CrimeReport!=null){
            mCommentsEditText.setText(_CrimeReport.getComments());
            mBitmap = _CrimeReportList.GetImage(_CrimeReport);

            if(mBitmap != null){
                mImageView.setImageBitmap(mBitmap);
            }

            _EditedLocation = new Location("");

            _EditedLocation.setLatitude(_CrimeReport.getLatitude());
            _EditedLocation.setLongitude(_CrimeReport.getLongitude());

        }
    }

    private void Init_ClearImageButton(){

        mbtnClearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitmap = null;

                if(_IsEdited){
                    _PreviousImagePath = _CrimeReport.getImageUrl();
                    _IsImageChanged= true;
                }

                _CrimeReport.setImageUrl("");
                mImageView.setImageResource(R.drawable.ic_imageicon);
            }
        });
    }
    private void Init_SelectDateTime(View rootView){

        mSelectDateTextView = (TextView) rootView.findViewById(R.id.selectDateEditText);
        mSelectTimeTextView = (TextView) rootView.findViewById(R.id.selectTimeEditText);

        myCalendar.setTime(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

        if(_IsEdited){

            SimpleDateFormat format = new SimpleDateFormat(GlobalConstants.CRIME_DATE_FORMAT);
            try {

                Date date = Utility.StringToDate(_CrimeReport.getDateTime(), GlobalConstants.CRIME_DATE_FORMAT);
                Date time = Utility.StringToDate(_CrimeReport.getDateTime(), GlobalConstants.CRIME_DATE_FORMAT);
                mSelectDateTextView.setText(dateFormat.format(date));
                mSelectTimeTextView.setText(timeFormat.format(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            mSelectDateTextView.setText(dateFormat.format(myCalendar.getTime()));
            mSelectTimeTextView.setText(timeFormat.format(myCalendar.getTime()));
        }

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                mSelectDateTextView.setText(sdf.format(myCalendar.getTime()));
            }

        };

        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                mSelectTimeTextView.setText(dateFormat.format(myCalendar.getTime()).toString());
            }
        };

        mSelectDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();


            }
        });

        mSelectTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int min = myCalendar.get(Calendar.MINUTE);
                new TimePickerDialog(getActivity(),timeSetListener,hour,min,true).show();
            }
        });

    }

    private void Init_ImageView(View rootView){

        mImageView = (ImageView)rootView.findViewById(R.id.selectImageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), GlobalConstants.PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mFilePath = data.getData();

            try {
                //getting image from gallery

                if(_IsEdited){
                    _PreviousImagePath = _CrimeReport.getImageUrl();
                    _IsImageChanged = true;
                }

                mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mFilePath);
                mBitmap = ImageUtility.resizeBitmap(mBitmap, GlobalConstants.CRIME_IMAGE_SIZE);
                mImageView.setImageBitmap(mBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void Init_PostCancelButtons(View rootView){

        textViewCancelButton = (TextView) rootView.findViewById(R.id.textViewCancel);
        textViewCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewCrimeMapFragment();
            }
        });

        textViewPostButton = (TextView) rootView.findViewById(R.id.textviewPost);
        textViewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsEditText.setError(null);

                String name = "";
                String comments = mCommentsEditText.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid email address.
                if (TextUtils.isEmpty(comments)) {
                    mCommentsEditText.setError(getString(R.string.error_field_required));
                    focusView = mCommentsEditText;
                    cancel = true;
                }

                if (IsShowYourName){
                    name = UserHandler.CurrentUser.getUserName();
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {

                    textViewPostButton.setClickable(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String userID = UserHandler.CurrentUser.getUserID();

                    CrimeReport crimeReport = new CrimeReport(userID,name, comments, SelectedLocation.getLatitude(), SelectedLocation.getLongitude());
                    String path = "";

                    if (mFilePath != null){
                        path = Utility.getRealPathFromURI(getContext(), mFilePath);
                        //InputStream inputStream = ImageUtility.saveBitmapToFile();
                    }

                    crimeReport.setCrimeType(_CrimeType);
                    String sdate = Utility.FormatDateToString(myCalendar.getTime(), GlobalConstants.DATE_FORMAT);
                    crimeReport.setDateTime(sdate);

                    if(_IsEdited){

                        crimeReport.setID(Integer.parseInt(_ReportID));
                        if(_IsImageChanged){

                            if(mBitmap!= null) {
                                _UOW.UpdateCrimeReport(crimeReport, path, mBitmap, _PreviousImagePath);
                            }else{
                                _UOW.UpdateCrimeReport(crimeReport, "", null, _PreviousImagePath);
                            }
                        }else {
                            _UOW.UpdateCrimeReport(crimeReport, "", null, _PreviousImagePath);

                        }

                        mProgressBar.setVisibility(View.GONE);
                        Utility.displayMessage(getContext(), "Report has been updated");
                        ViewCrimeMapFragment();

                        _UOW.crimeReportedCallBack = new ActionCompleteCallBack() { //when data is loaded
                            public void CallBack() {


                            }
                        };

                    }else {
                        _UOW.SubmitCrimeReport(crimeReport, path, mBitmap);

                        _UOW.crimeReportedCallBack = new ActionCompleteCallBack() { //when data is loaded
                            public void CallBack() {

                                mProgressBar.setVisibility(View.GONE);
                                Utility.displayMessage(getContext(), "It has been reported");
                                ViewCrimeMapFragment();
                            }
                        };
                    }
                }
            }
        });
    }

    private void SetLocation(){

        Location l = new Location("");

        if(_IsEdited) {

            l.setLatitude(_CrimeReport.getLatitude());
            l.setLongitude(_CrimeReport.getLongitude());
        }else{

        }

        moveCamera(l, GlobalConstants.DEFAULT_ZOOM);
    }

    private void InitLocationService(){

        boolean IsCoarseLocationAllowed = ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean IsFineLocationAllowed = ContextCompat.checkSelfPermission(getActivity(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if(!IsCoarseLocationAllowed || !IsFineLocationAllowed){
            requestPermissions(GlobalConstants.PERMISSIONS, GlobalConstants.PERMISSION_ALL);
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

                                Log.d("Location123", "InitLocationService OnSuccess");
                                moveCamera(location, GlobalConstants.DEFAULT_ZOOM);
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
                //int count = 0;
                Location l = locationResult.getLastLocation();
                boolean isLocationChanged = LocationUtility.IsLocationChanged(UserHandler.get_CurrentLocation(),l, 1.0);

                if(isLocationChanged){
                    //AddPlaces();
                    //String sLocation = "Latitude:" + l.getLatitude() + " Longitude:" + l.getLongitude();
                    Log.d("Location123", "InitLocationService LocationCallback");
                    moveCamera(l, GlobalConstants.DEFAULT_ZOOM);
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
            requestPermissions(GlobalConstants.PERMISSIONS, GlobalConstants.PERMISSION_ALL);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        //mMap.onResume();
        //AddPlaces();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mMap.onPause();
        //if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) { //this is causing the crash
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage((getActivity()));
            mGoogleApiClient.disconnect();
        }
        stopLocationUpdates();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        mGoogleApiClient = null;
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mMap.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //mMap.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage((getActivity()));
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        mGoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setOnCameraIdleListener(this);

        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);

        Location location = new Location("");//provider name is unnecessary

        if(UserHandler.get_CurrentLocation() !=null) {

            location.setLatitude(UserHandler.get_CurrentLocation().getLatitude());//your coords of course
            location.setLongitude(UserHandler.get_CurrentLocation().getLongitude());

            //String sLocation = "Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude();


            if(_IsEdited){
                Log.d("Location123", "onMapReady Before init_CurrentAddress edited");
                moveCamera(_EditedLocation, GlobalConstants.DEFAULT_ZOOM);
                init_CurrentAddress(location);
            }else{
                moveCamera(location, GlobalConstants.DEFAULT_ZOOM);
                init_CurrentAddress(location);
            }

        }

        /*mGoogleMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                SelectedLocation = new Location("");
                SelectedLocation.setLatitude(latLng.latitude);
                SelectedLocation.setLongitude(latLng.longitude);

                String markerText = mCommentsEditText.getText().toString();
                LocationUtility.drawMarker(SelectedLocation,mGoogleMap, getActivity(),markerText);
            }
        });*/

        init_Search();
        UpdateSelectedLocation();
    }
    @Override
    public void onCameraIdle() {

        //UpdateSelectedLocation();
        if(_IsMapMovedprogrammatically){
            _IsMapMovedprogrammatically = false; // reset the state of variable
        }
        else {

            //mSearchText.setText("");
            if (SelectedLocation != null) {
                init_CurrentAddress(SelectedLocation);
            }
        }
    }

    private void UpdateSelectedLocation(){
        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change" + "", cameraPosition + "");
                LatLng mCenterLatLong = cameraPosition.target;

                mGoogleMap.clear();

                try {

                    SelectedLocation = new Location("");
                    SelectedLocation.setLatitude(mCenterLatLong.latitude);
                    SelectedLocation.setLongitude(mCenterLatLong.longitude);
                    Location l = new Location("");

                    String editedlocation = "Edited Location:" + String.valueOf(_EditedLocation.getLatitude()) + "|" +
                                            String.valueOf(_EditedLocation.getLongitude());

                    String selectedlocation = ": Selected Location:" + String.valueOf(SelectedLocation.getLatitude()) + "|" +
                            String.valueOf(SelectedLocation.getLongitude());

                    Log.d("Location123", selectedlocation + editedlocation);

                    //startIntentService(mLocation);
                    //mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void init_CurrentAddress(Location l){

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 10);
        }catch(IOException ex){

        }
        if (addresses == null || addresses.isEmpty()) {

            new GetAddressByLocationTask(new GetAddressByLocationTask.Listener() {
                @Override
                public void onCompleted(List<Address> addressList) {

                    if(addressList.size()>0) {
                        Address address = addressList.get(0);
                        String addressline = "";
                        for (int n = 0; n <= address.getMaxAddressLineIndex(); n++) {
                            addressline += address.getAddressLine(n) + ", ";
                        }
                        mSearchText.setText(addressline);
                    }
                }

                @Override
                public void OnError() {
                    Log.e("onError", "GetAddressByLocationTask");
                }
            }).execute(l);
        }

        if(addresses != null) {
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressline = "";
                for (int n = 0; n <= address.getMaxAddressLineIndex(); n++) {
                    addressline += address.getAddressLine(n) + ", ";
                }
                mSearchText.setText(addressline);
            }
        }
    }

    private void init_Search(){

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //mGoogleApiClient.stopAutoManage((getActivity()));
            //mGoogleApiClient.disconnect();
        }else {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getActivity())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), this)
                    .build();
        }

        mPlaceAutocompleteAdapter = new com.tripmee.findmee.CrimeReport.PlaceAutoCompleteAdapter(getContext(), mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mProgressBar.setVisibility(View.VISIBLE);
                hideSoftKeyboard();
                geoLocate();
            }
        });

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    mProgressBar.setVisibility(View.VISIBLE);
                    hideSoftKeyboard();
                    geoLocate();
                }

                return false;
            }
        });
        //hideSoftKeyboard();
    }

    private void geoLocate(){

        final String searchString = mSearchText.getText().toString();

        new GeocodingTask(new GeocodingTask.Listener() {
            @Override
            public void onCompleted(List<Address> addressList) {

                mProgressBar.setVisibility(View.GONE);
                if(addressList.size()>0) {
                    Address address = addressList.get(0);

                    Log.d("", "geoLocate: found a location: " + address.toString());
                    //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), GlobalConstants.DEFAULT_ZOOM,
                            address.getAddressLine(0));
                }
            }

            @Override
            public void OnError() {
                Log.e("onError", "Failed to get Location from "
                        + searchString);
            }
        }).execute(searchString);
    }
    private void geoLocateBackup(){
        String TAG = "geoLocate";
        Log.d(TAG, "geoLocate: geolocating");

        final String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault()); //new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            if(!address.getAddressLine(0).contains(searchString)){
                Toast.makeText(getContext(), "Wrong Address" + address.toString(), Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), GlobalConstants.DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
        else{
            new GeocodingTask(new GeocodingTask.Listener() {
                @Override
                public void onCompleted(List<Address> addressList) {

                    mProgressBar.setVisibility(View.GONE);
                    if(addressList.size()>0) {
                        Address address = addressList.get(0);

                        Log.d("", "geoLocate: found a location: " + address.toString());
                        //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

                        moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), GlobalConstants.DEFAULT_ZOOM,
                                address.getAddressLine(0));
                    }
                }

                @Override
                public void OnError() {
                    Log.e("onError", "Failed to get Location from "
                            + searchString);
                }
            }).execute(searchString);
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title){
        //Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        if (mGoogleMap != null) {

            _IsMapMovedprogrammatically = true;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mGoogleMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void moveCamera(Location location, float zoom) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        }
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

    private void hideSoftKeyboard(){
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void ViewCrimeMapFragment() {
        com.tripmee.findmee.CrimeReport.CrimeMapFragment fragment = new com.tripmee.findmee.CrimeReport.CrimeMapFragment();
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
}
