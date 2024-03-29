package com.tripmee.findmee.CrimeReport;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CrimeReport.CrimeDetailFragment;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.Dashboard.DashboardFragment;
import com.tripmee.findmee.GlobalConstants.CrimeReportConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.LocationUtility;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Utility;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrimeMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrimeMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrimeMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION, permission.READ_PHONE_STATE};

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private UserGlobalHandler UserHandler;
    private UnitOfWork _UOW;
    private static ArrayList<CrimeReport> _CrimeReports;

    private MapView mMap;
    private GoogleMap mGoogleMap;

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    private FusedLocationProviderApi mFusedLocationProviderApi;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private FloatingActionButton reportButton;
    private CircleImageView ProfileImageView;

    private TextView totalReportsLabel;

    private OnFragmentInteractionListener mListener;

    public CrimeMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CrimeMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CrimeMapFragment newInstance(String param1, String param2) {
        CrimeMapFragment fragment = new CrimeMapFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_crime_map, container, false);

        if (!Permissions.IsPermissionGranted(getActivity())) {
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        }

        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);

        if(_UOW.get_CrimeReportList().getCrimeReports() == null){
            _UOW.LoadCrimeReports();
        }

        Init_ProfileImage(rootView);

        InitLocationService();

        mMap = (MapView)rootView.findViewById(R.id.crimeMapView);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync( this);

        reportButton =(FloatingActionButton)rootView.findViewById(R.id.reportButton);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReportCrimeFragment();
                ViewCrimeTypeFragment();
            }
        });

        totalReportsLabel = (TextView)rootView.findViewById(R.id.textViewReports);


        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMap.onResume();

        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
        //AddPlaces();
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMap.onPause();
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
        mMap.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMap.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        mGoogleMap = googleMap;

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                CrimeReport r = (CrimeReport)marker.getTag();
                ViewCrimeDetailFragment(r);
                return true;
            }
        });


        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);

        Location location = new Location("");//provider name is unnecessary

        if(UserHandler.get_CurrentLocation() !=null) {

            location.setLatitude(UserHandler.get_CurrentLocation().getLatitude());//your coords of course
            location.setLongitude(UserHandler.get_CurrentLocation().getLongitude());

            String sLocation = "Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude();
            //Log.d("OnMapReady", sLocation);
            //moveCamera(location, GlobalConstants.DEFAULT_ZOOM);
            //Toast.makeText(getActivity(), sLocation, Toast.LENGTH_SHORT).show();
            //Toast.makeText(getActivity(), "Move Camera onMapReady", Toast.LENGTH_SHORT).show();

            moveCamera(location, GlobalConstants.DEFAULT_ZOOM);
        }
        AddWarningSigns();
    }

    private void AddWarningSigns(){

        if(_UOW.get_CrimeReportList() == null){
            return;
        }

        if(_UOW.get_CrimeReportList().getCrimeReports() == null){
            //UOW.LoadCrimeReports();
            return;
        }

        totalReportsLabel.setText(_UOW.get_CrimeReportList().getCrimeReports().size() + " incidents reported");

        for (CrimeReport report:_UOW.get_CrimeReportList().getCrimeReports()) {
            Location l = new Location("");
            l.setLatitude(report.getLatitude());
            l.setLongitude(report.getLongitude());

            LatLng latLng = new LatLng(report.getLatitude(), report.getLongitude());
            BitmapDescriptor bitmapDescriptor;
            if(report.getCrimeType().equals(CrimeReportConstants.SEXUAL_ASSAULT)){
                bitmapDescriptor = ImageUtility.bitmapDescriptorFromVector(getContext(),R.drawable.icon_physical_abuse);
            }else if(report.getCrimeType().equals(CrimeReportConstants.VERBAL_ABUSE)){
                bitmapDescriptor = ImageUtility.bitmapDescriptorFromVector(getContext(),R.drawable.icon_verbal_abuse);
            }else{
                bitmapDescriptor = ImageUtility.bitmapDescriptorFromVector(getContext(),R.drawable.icon_other_abuse);
            }

            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                        .icon(bitmapDescriptor)
                                        .title(report.getCrimeType())
                                        .position(latLng)
                                        .snippet(report.getComments()));
            marker.setTag(report);
        }
    }

    private void moveCamera(Location location, float zoom) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        }
    }

    private void InitLocationService(){
        //this function use fusedlocationprovider to get the last known location
        //if last known location is null then it called getLocation() function

        boolean IsCoarseLocationAllowed = ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean IsFineLocationAllowed = ContextCompat.checkSelfPermission(getActivity(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if(!IsCoarseLocationAllowed || !IsFineLocationAllowed){
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        if(UserHandler.get_CurrentLocation() != null) {
            moveCamera(UserHandler.get_CurrentLocation() , GlobalConstants.DEFAULT_ZOOM);
        }else{
            Utility.displayMessage(getContext(), "This app failed to get location");
            SetDefaultLocation();
        }

        createLocationRequest();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        /*location updates are moved to backgroundlocationservice
                        if (location != null) {

                            if (UserHandler != null){
                                //Toast.makeText(getContext(), "location :"+location.getLatitude()+" , "+location.getLongitude(), Toast.LENGTH_SHORT).show();
                                UserHandler.setCurrentLocation(location);
                                moveCamera(location, GlobalConstants.DEFAULT_ZOOM);
                            }
                        }else{

                            //getLocation();
                            if(UserHandler.get_CurrentLocation() != null) {
                                moveCamera(UserHandler.get_CurrentLocation() , GlobalConstants.DEFAULT_ZOOM);
                            }else{
                                Utility.displayMessage(getContext(), "This app failed to get location in crime map fused location");
                            }

                        }*/
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location l = locationResult.getLastLocation();
                boolean isLocationChanged = LocationUtility.IsLocationChanged(UserHandler.get_CurrentLocation(),l, 1.0);

                if(isLocationChanged){
                    //AddPlaces();
                    //String sLocation = "Latitude:" + l.getLatitude() + " Longitude:" + l.getLongitude();
                    moveCamera(l, GlobalConstants.DEFAULT_ZOOM);
                    /*String test = "UserHandler Location. Lat:" + String.valueOf(UserHandler.get_CurrentLocation().getLatitude())
                                  + "| Lng:" + String.valueOf(UserHandler.get_CurrentLocation().getLongitude())
                                  + "Current Location. Lat:" + String.valueOf(l.getLatitude())
                                  + "| Lng:" + String.valueOf(l.getLongitude());*/

                    //Utility.displayMessage(getContext(), test);
                }
                //UserHandler.setCurrentLocation(l);

            };
        };
    }


    @Override
    public void onConnected(Bundle arg0) {

        if (Permissions.IsPermissionGranted(getActivity())) {
            mFusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        UserHandler.setCurrentLocation(location);
        if(UserHandler.get_CurrentLocation() != null) {
            moveCamera(UserHandler.get_CurrentLocation() , GlobalConstants.DEFAULT_ZOOM);
        }
    }
    @Override
    public void onConnectionSuspended(int i){

    }

    private void SetDefaultLocation(){
        //Set default location to shwedagon pagoda
        Double lat = 16.798308;
        Double lng = 96.1474256;
        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(lat);//your coords of course
        targetLocation.setLongitude(lng);
        //UserHandler.setCurrentLocation(targetLocation);
        moveCamera(targetLocation, GlobalConstants.DEFAULT_ZOOM);
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

    private void Init_ProfileImage(View rootView){
        ProfileImageView = (CircleImageView) rootView.findViewById(R.id.profile_photo);

        ProfileImageView.setImageResource(R.drawable.default_profile);

        if(!Utility.StringIsBlankOrEmpty(UserHandler.CurrentUser.get_FireBaseProfileImage())){
            ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
        }

        UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() {
            public void CallBack() {
                if(!Utility.StringIsBlankOrEmpty(UserHandler.CurrentUser.get_FireBaseProfileImage())){
                    ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
                }
            }
        };


        UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() {
            public void CallBack() {
                if(UserHandler.CurrentUser.get_FireBaseProfileImage() != null){
                    ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
                }
            }
        };

        ProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new DashboardFragment());
            }
        });
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

    private void ViewCrimeTypeFragment() {
        com.tripmee.findmee.CrimeReport.CrimeTypeFragment fragment = new com.tripmee.findmee.CrimeReport.CrimeTypeFragment();
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

    private void ViewCrimeDetailFragment(CrimeReport report) {
        CrimeDetailFragment fragment = new CrimeDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString("UserID", report.getUserID());
        arguments.putString("ID", report.getID().toString());
        fragment.setArguments(arguments);

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
