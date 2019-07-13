package com.tripmee.findmee.EmergencyRequests;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Phone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MapView mMap;
    private ImageView imageView;
    private GoogleMap mGoogleMap;
    private UserGlobalHandler mUserHandler;
    private EmergencyRequest mRequest;

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserHandler = UserGlobalHandler.get_instance();
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
            //Bundle arguments = getArguments();
            String ID = getArguments().getString(GlobalConstants.EMERGENCY_REQUEST_ID);
            mRequest = mUserHandler.GetRequestByID(ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.imageViewEmergency);

        Button btnCall  = (Button) rootView.findViewById(R.id.btnCallEmergencyContact);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call
                String m = mRequest.getMobile();
                Phone.Call(m, getActivity());
                /*Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + m));

                if (ActivityCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);*/
            }
        });

        mMap = (MapView) rootView.findViewById(R.id.mapView);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(this);


        Button btnViewImage = (Button) rootView.findViewById(R.id.btnViewImage);
        btnViewImage.setVisibility(View.GONE);
        if(mRequest!=null){
            if (mRequest.HasImage()){
                btnViewImage.setVisibility(View.VISIBLE);
            }
        }

        btnViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ViewImageFragment(mRequest.getEmergencyRequestID());

            }
        });

        return rootView;
    }

    private void ViewImageFragment(String requestID) {
        com.tripmee.findmee.EmergencyRequests.ViewImageFragment fragment = new com.tripmee.findmee.EmergencyRequests.ViewImageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(GlobalConstants.EMERGENCY_REQUEST_ID, requestID);
        fragment.setArguments(arguments);
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(R.id.content, fragment , FRAGMENT_TAG);
        //ft.commit();

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

    private void drawMarker(Location location) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            String MarkerText = mRequest.getMobile();

            if (MarkerText.isEmpty()){
                MarkerText = mRequest.getMobile();
            }

            mGoogleMap.setMyLocationEnabled(true);
            //mGoogleMap.setMapType();
            UiSettings uiSettings = mGoogleMap.getUiSettings();
            uiSettings.setAllGesturesEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setCompassEnabled(true);
            //uiSettings.setIndoorLevelPickerEnabled(true);
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .title(MarkerText));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        mGoogleMap = googleMap;

        Location location = new Location("");//provider name is unnecessary
        location.setLatitude(mRequest.getLatitude());//your coords of course
        location.setLongitude(mRequest.getLongitude());
        drawMarker(location);
    }
    @Override
    public void onResume() {
        mMap.onResume();
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMap.onLowMemory();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
       // }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
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
}
