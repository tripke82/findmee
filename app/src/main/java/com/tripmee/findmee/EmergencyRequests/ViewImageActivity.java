package com.tripmee.findmee.EmergencyRequests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tripmee.findmee.EmergencyRequests.LocationActivity;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewImageActivity extends AppCompatActivity {

    private EmergencyRequest mRequest;
    private EmergencyContact mContact;
    private UserGlobalHandler UserHandler;
    private Button btnBack;
    private Button btnSaveImage;
    private ImageView imageView;
    private TextView lblText;
    private TextView lblMobile;
    private CircleImageView mCircleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        UserHandler = UserGlobalHandler.get_instance();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //String UserID = extras.getString("UserID");
            String RequestID = extras.getString(GlobalConstants.EMERGENCY_REQUEST_ID);
            mRequest = UserHandler.GetRequestByID(RequestID);
            mContact = UserHandler.GetEmergencyContactByID(mRequest.getUserID());
            //mUserHandler.SetEmergencyRequestStatus(  mRequest.getEmergencyRequestID());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setTitleTextColor(Color.WHITE);
        //((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        btnBack = (Button)findViewById(R.id.btnBack);
        btnSaveImage = (Button)findViewById(R.id.btnSaveImage);
        imageView = (ImageView)findViewById(R.id.imageViewEmergency);

        lblText = (TextView)findViewById(R.id.emergencyText);
        lblMobile = (TextView)findViewById(R.id.emergencyMobile);
        mCircleImageView = (CircleImageView)findViewById(R.id.profile_image_eitem);

        InitEmergencyContactDetails();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ViewLocationFragment(mRequest.getEmergencyRequestID());
                Intent intent = new Intent(ViewImageActivity.this, LocationActivity.class);
                intent.putExtra(GlobalConstants.EMERGENCY_REQUEST_ID, mRequest.getEmergencyRequestID());
                startActivity(intent);
                finish();
            }
        });

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRequest.getEmergencyImageUrl().isEmpty()){
                    Uri image = Uri.parse(mRequest.getEmergencyImageUrl());

                    Bitmap imageBitmap = null;
                    imageBitmap = ImageUtility.StringToBitMap(mRequest.getEmergencyImageUrl());
                    MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "Emergency" + mRequest.getmDateTime() , mRequest.getEmergencyMessage());
                    Utility.displayMessage(getApplicationContext(), "Saved");
                    //Toast.makeText(, "Saved", Toast.LENGTH_LONG).show();
                }
            }
        });

        InitEmergencyImage();
    }

    private void InitEmergencyContactDetails(){

        lblText.setText(mRequest.getEmergencyMessage());
        lblMobile.setText(mRequest.getMobile());

        Bitmap imageBitmap = null;

        if(mContact == null){
            return;
        }

        String sImage = mContact.get_FireBaseProfileImage();

        if (!Utility.StringIsBlankOrEmpty(sImage)) {
            imageBitmap = ImageUtility.StringToBitMap(mContact.get_FireBaseProfileImage());
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }
    }

    private void InitEmergencyImage(){

        if (mRequest.getEmergencyImageUrl() != null){
            if (!mRequest.getEmergencyImageUrl().isEmpty()){
                Bitmap imageBitmap = null;
                imageBitmap = ImageUtility.StringToBitMap(mRequest.getEmergencyImageUrl());

                int imageWidth = imageBitmap.getWidth();
                int imageHeight = imageBitmap.getHeight();

                int newWidth = GetScreenWidth(); //this method should return the width of device screen.
                float scaleFactor = (float)newWidth/(float)imageWidth;
                int newHeight = (int)(imageHeight * scaleFactor);
                //newHeight = 500;
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true);
                imageView.setImageBitmap(imageBitmap);

            }
        }
    }

    private int GetScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width;
    }
}
