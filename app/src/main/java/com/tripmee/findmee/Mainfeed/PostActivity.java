package com.tripmee.findmee.Mainfeed;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Camera;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission;

public class PostActivity extends AppCompatActivity {

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION, permission.READ_PHONE_STATE};

    static final int CAPTURE_IMAGE_REQUEST = 1; //image part
    private String mCurrentPhotoPath;
    private File photoFile = null;
    private Bitmap _Bitmap;

    private View mProgressView;
    private CircleImageView imageProfile;
    private ImageView imagePreview;
    private Button btnPost;
    private Button btnCancel;
    private TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        textStatus = findViewById(R.id.editTextPost);
        imagePreview = findViewById(R.id.imageViewPreview);
        imageProfile = findViewById(R.id.profileimage);
        btnPost = findViewById(R.id.btnPost);
        btnCancel = findViewById(R.id.btnCancel);
        btnPost.setEnabled(false);
        mProgressView = (ProgressBar)findViewById(R.id.progressBar);

        imageProfile.setImageBitmap(UserGlobalHandler.get_instance().CurrentUser.GetProfileImage());
        textStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = textStatus.getText().toString();
                if(!Utility.StringIsBlankOrEmpty(text)){
                    btnPost.setEnabled(true);
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            captureImage();
        } else {
            captureImage2();
        }
        if (ActivityCompat.checkSelfPermission(PostActivity.this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(PostActivity.this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utility.displayMessage(PostActivity.this, "No permission to access phone's location");
        }

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressView.setVisibility(View.VISIBLE);
                final UnitOfWork UOW = UnitOfWork.get_instance(UserGlobalHandler.get_instance().CurrentUser);
                UOW.SendSOSRequest(photoFile.getAbsolutePath(), photoFile.getName(), _Bitmap, Utility.ObjectToString(textStatus.getText()));
                Utility.displayMessage(PostActivity.this, "SOS Request is sent");

                UOW.actionCompletedCallBack = new ActionCompleteCallBack() { //when data is loaded
                    public void CallBack() {
                        mProgressView.setVisibility(View.GONE);
                        ViewMainFeedFragment();
                    }
                };
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewMainFeedFragment();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            _Bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            if(_Bitmap != null) {
                _Bitmap = ImageUtility.resizeBitmap(_Bitmap, 600);
                imagePreview.setImageBitmap(_Bitmap);
                btnPost.setEnabled(true);
            }
        }
    }

    private void ViewMainFeedFragment(){
        Intent i = new Intent(PostActivity.this, MainActivity.class);
        i.putExtra(GlobalConstants.SELECTED_FRAGMENT, GlobalConstants.MAINFEED_FRAGMENT);
        startActivity(i);
        finish();
    }

    private void captureImage2() {

        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = Camera.createImageFile2(PostActivity.this);
            if (photoFile != null) {
                Utility.displayMessage(PostActivity.this, photoFile.getAbsolutePath());
                //Log.i("Image uri", photoFile.getAbsolutePath());
                Uri photoURI = Uri.fromFile(photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
            }
        } catch (Exception e) {
            Utility.displayMessage(PostActivity.this, "Camera is not available." + e.toString());
        }
    }

    private void captureImage() {

        String[] Perms = new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE};
        if (ActivityCompat.checkSelfPermission(PostActivity.this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //requestPermissions(Perms, PERMISSION_ALL);
        } else {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // need to run on real device
                // Create the File where the photo should go
                try {

                    photoFile = Camera.createImageFile(PostActivity.this);
                    // Continue only if the File was successfully created
                    if (photoFile != null) {

                        Uri photoURI = FileProvider.getUriForFile(PostActivity.this, getPackageName(), photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    Utility.displayMessage(PostActivity.this, ex.getMessage().toString());
                }

            } else {
                Utility.displayMessage(PostActivity.this,"Camera is not available");
            }
        }
    }
    /*


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createImageFile2() {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                GlobalConstants.IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Utility.displayMessage(PostActivity.this, "Unable to create directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;

    }

   */
}
