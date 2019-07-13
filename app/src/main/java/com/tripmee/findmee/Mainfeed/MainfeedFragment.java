package com.tripmee.findmee.Mainfeed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.SOS.SOSRequest;
import com.tripmee.findmee.Models.SOS.SOSRequestList;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Permissions;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainfeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainfeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainfeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView listView;
    private CircleImageView ProfileImageView;
    private Button btnCreatePost;
    private SearchView searchView;
    private SOSRequestList _SOSRequestList;
    private User _CurrentUser;
    private com.tripmee.findmee.Mainfeed.SOSRequestAdapter _SOSRequestAdapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private UserGlobalHandler UserHandler;

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};

    public MainfeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainfeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainfeedFragment newInstance(String param1, String param2) {
        MainfeedFragment fragment = new MainfeedFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_mainfeed, container, false);
        // Inflate the layout for this fragment


        listView = (ListView)rootView.findViewById(R.id.mainfeed_list_view);
        ProfileImageView = (CircleImageView) rootView.findViewById(R.id.profile_photo);
        btnCreatePost = (Button)rootView.findViewById(R.id.btn_Post);
        searchView = (SearchView) rootView.findViewById(R.id.mainfeed_searchview);
        _CurrentUser = UserGlobalHandler.get_instance().CurrentUser;

        if(_CurrentUser.get_FireBaseProfileImage() != null){
            ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(_CurrentUser.get_FireBaseProfileImage()));
        }

        CreatePost();

        _SOSRequestAdapter = new SOSRequestAdapter(getActivity(), UnitOfWork.get_instance(_CurrentUser).get_SOSRequestList());
        //Integer i = UnitOfWork.get_instance(_CurrentUser).get_SOSRequestList().getSOSRequestList().size();
        //Utility.displayMessage(getContext(), "List Count" + String.valueOf(i));
        listView.setAdapter(_SOSRequestAdapter);
        Init_Search();

        if (!Permissions.IsPermissionGranted(getActivity())) {
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        }

        InitLocationService();

        return rootView;
    }

    private void CreatePost(){
        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
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


    /*
    private void Init_SOSRequests(){
        UnitOfWork UOW = new UnitOfWork(UserGlobalHandler.get_instance().CurrentUser);

        _SOSRequestList = new SOSRequestList();

        UOW._RetrofitService.GetSOSRequests(new Callback<List<SOSRequest>>() {
            @Override
            public void onResponse(Call<List<SOSRequest>> call, Response<List<SOSRequest>> response) {

                _SOSRequestList.setSOSRequestList((ArrayList)response.body());
                _SOSRequestList.SortByDate();
                _SOSRequestList.LoadImages();
                _SOSRequestAdapter = new SOSRequestAdapter(getActivity(), _SOSRequestList);
                //_SOSRequestAdapter = new SOSRequestAdapter(getActivity(), (ArrayList)response.body());
                listView.setAdapter(_SOSRequestAdapter);
            }

            @Override
            public void onFailure(Call<List<SOSRequest>> call, Throwable t) {

            }
        });
    }*/

    private void Init_Search(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                _SOSRequestAdapter.getFilter().filter(query);
                //_SOSRequestAdapter.ClearAll();
                return true; // handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == "" || newText.isEmpty()) {
                    _SOSRequestAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    private void ViewFeedLocationFragment(SOSRequest sosRequest) {
        Mainfeed_LocationFragment fragment = new Mainfeed_LocationFragment();
        Bundle arguments = new Bundle();
        arguments.putString("latitude", sosRequest.getLatitude().toString());
        arguments.putString("longitude", sosRequest.getLongitude().toString());
        //arguments.putString("imageurl", sosRequest.getImageUrl());
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        //}
    }

    //@Override
    //public void onAttach(Context context) {
        //super.onAttach(context);
        //if (context instanceof OnFragmentInteractionListener) {
            //mListener = (OnFragmentInteractionListener) context;
        //} else {
            //throw new RuntimeException(context.toString()
                    //+ " must implement OnFragmentInteractionListener");
        //}
    //}

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
}
