package com.tripmee.findmee.EmergencyRequests;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mMap;
    private ImageView imageView;
    private GoogleMap mGoogleMap;
    private UserGlobalHandler mUserHandler;
    private EmergencyRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mUserHandler = UserGlobalHandler.get_instance();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //String UserID = extras.getString("UserID");
            String RequestID = extras.getString("RequestID");
            mRequest = mUserHandler.GetRequestByID(RequestID);
        }

        if(mRequest == null){ // the findmee request could be null if user logged out and another user logged in
            // is that happens go to main activity
            ViewMainActivity();
        }else
        {
            if (!mRequest.IsChecked()){

                mUserHandler.SetEmergencyRequestStatus(  mRequest.getEmergencyRequestID());
            }
        }

        imageView = (ImageView) findViewById(R.id.imageViewEmergency);

        Button btnCall  = (Button) findViewById(R.id.btnCallEmergencyContact);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call
                String m = mRequest.getMobile();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + m));

                if (ActivityCompat.checkSelfPermission(LocationActivity.this,
                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });

        mMap = (MapView) findViewById(R.id.mapView);
        mMap.onCreate(savedInstanceState);

        mMap.getMapAsync(this);


        Button btnViewImage = (Button) findViewById(R.id.btnViewImage);
        btnViewImage.setVisibility(View.GONE);
        if(mRequest!=null){
            if (mRequest.HasImage()){
                btnViewImage.setVisibility(View.VISIBLE);
            }
        }

        btnViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ViewImageFragment(mRequest.getEmergencyRequestID());
                ViewImageActivity();
            }
        });
    }

    private void ViewMainActivity(){
        Intent intent = new Intent(LocationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void ViewImageActivity(){
        Intent intent = new Intent(LocationActivity.this, ViewImageActivity.class);
        intent.putExtra(GlobalConstants.EMERGENCY_REQUEST_ID, mRequest.getEmergencyRequestID());
        startActivity(intent);
        finish();
    }
    private void ViewImageFragment(String requestID){
        Intent i = new Intent(LocationActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(GlobalConstants.SELECTED_FRAGMENT, GlobalConstants.VIEWIMAGE_FRAGMENT);
        i.putExtra( GlobalConstants.EMERGENCY_REQUEST_ID , requestID);
        startActivity(i);
        finish();
    }

    private void ViewImageFragment2(String requestID){
        ViewImageFragment fragment = new ViewImageFragment();
        Bundle arguments = new Bundle();
        arguments.putString( GlobalConstants.EMERGENCY_REQUEST_ID , requestID);
        fragment.setArguments(arguments);
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(R.id.content, fragment , FRAGMENT_TAG);
        //ft.commit();

        try {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.locationlinearLayout, fragment);
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
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.locationlinearLayout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }

    public void showImage() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);

        if (mRequest.getEmergencyImageUrl() != null){
            if (!mRequest.getEmergencyImageUrl().isEmpty()){
                Bitmap imageBitmap = null;
                imageBitmap = ImageUtility.StringToBitMap(mRequest.getEmergencyImageUrl());
                imageView.setImageBitmap(imageBitmap);
            }
        }

        //imageView.setImageURI(imageUri);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }

    private void drawMarker(Location location) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            if (ActivityCompat.checkSelfPermission
                    (this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
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

        if(mRequest == null){
            return;
        }

        Location location = new Location("");//provider name is unnecessary
        location.setLatitude(mRequest.getLatitude());//your coords of course
        location.setLongitude(mRequest.getLongitude());
        drawMarker(location);
    }
    @Override
    public void onResume() {
        mMap.onResume();
        super.onResume();
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

}
