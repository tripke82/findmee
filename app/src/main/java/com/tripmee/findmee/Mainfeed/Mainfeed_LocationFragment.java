package com.tripmee.findmee.Mainfeed;

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
import com.tripmee.findmee.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Mainfeed_LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Mainfeed_LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Mainfeed_LocationFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String IMAGEURL = "imageurl";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;
    private MapView mMap;
    private ImageView imageView;
    private GoogleMap mGoogleMap;
    private double _Latitude;
    private double _Longitude;
    private String _ImageUrl;

    private OnFragmentInteractionListener mListener;

    public Mainfeed_LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Mainfeed_LocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Mainfeed_LocationFragment newInstance(String lat, String lon) {
        Mainfeed_LocationFragment fragment = new Mainfeed_LocationFragment();
        Bundle args = new Bundle();
        args.putString(LATITUDE, lat);
        args.putString(LONGITUDE, lon);
        //args.putString(IMAGEURL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String slat = getArguments().getString(LATITUDE);
            String slon = getArguments().getString(LONGITUDE);
            _Latitude = Double.parseDouble(slat);
            _Longitude = Double.parseDouble(slon);
            _ImageUrl = getArguments().getString(IMAGEURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mainfeed__location, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.imageViewEmergency);

        mMap = (MapView) rootView.findViewById(R.id.mainfeedMapView);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(this);

        Button btnViewImage = (Button) rootView.findViewById(R.id.btnViewImage);
        btnViewImage.setVisibility(View.GONE);

        //if(mRequest!=null){
            //if (mRequest.HasImage()){
                //btnViewImage.setVisibility(View.VISIBLE);
            //}
        //}

        btnViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ViewImageFragment(mRequest.getEmergencyRequestID());

            }
        });
        return rootView;
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

            mGoogleMap.setMyLocationEnabled(true);
            UiSettings uiSettings = mGoogleMap.getUiSettings();
            uiSettings.setAllGesturesEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setCompassEnabled(true);
            //uiSettings.setIndoorLevelPickerEnabled(true);
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    //.title(MarkerText)
                    .position(gps));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        mGoogleMap = googleMap;

        Location location = new Location("");//provider name is unnecessary
        location.setLatitude(_Latitude);//your coords of course
        location.setLongitude(_Longitude);
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
            //mListener.onFragmentInteraction(uri);
        //}
    }
    /*
    @Override
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
