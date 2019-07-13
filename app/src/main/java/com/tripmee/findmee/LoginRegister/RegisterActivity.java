package com.tripmee.findmee.LoginRegister;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.Models.DownloadImageTask;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText mNameView;
    private EditText mEmailView;
    private EditText mPhoneNumberView;
    private EditText mPasswordView;
    private EditText mRePasswordView;
    private CircleImageView mCircleImageView;
    private View mProgressView;

    private String name;
    private String email;
    private String phone;
    private String password;
    private String password2;
    private int PICK_IMAGE_REQUEST = 111;
    private Uri filePath;;
    private Bitmap mBitmap;

    private boolean _IsFacebookLogin = false;

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION, READ_PHONE_STATE, READ_PHONE_NUMBERS, SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameView = (EditText) findViewById(R.id.editTextRegName);
        mEmailView = (EditText) findViewById(R.id.editTextRegEmail);
        mPasswordView = (EditText) findViewById(R.id.editRegPassword);
        mRePasswordView = (EditText) findViewById(R.id.editRetypePassword);
        mPhoneNumberView = (EditText) findViewById(R.id.editTextRegPhoneNumber);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);

        auth = FirebaseAuth.getInstance();

        String phone = GetPhoneNumber();
        mPhoneNumberView.setText(phone);

        _IsFacebookLogin = IsFacebookLoggedin();
        if(_IsFacebookLogin){
            InitFacebookUser();
        }

        Button RegisterButton = (Button) findViewById(R.id.btnRegister);
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        Button CancelButton = (Button) findViewById(R.id.btnCancel);
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_IsFacebookLogin){
                    auth.getCurrentUser().delete();
                    auth.signOut();
                    LoginManager.getInstance().logOut();
                }
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button mSelectImageButton = (Button) findViewById(R.id.btn_SelectImage);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        mProgressView = (ProgressBar)findViewById(R.id.progressBar);
    }

    private boolean IsFacebookLoggedin(){

        Intent intent = getIntent();
        String s1 = intent.getStringExtra("Facebook");
        if(!Utility.StringIsBlankOrEmpty(s1)) {
            if (s1.equals("Loggedin")) {
               return true;
            }
        }

        return false;
    }
    private void InitFacebookUser(){

        FirebaseUser user = auth.getCurrentUser();

        mNameView.setText(user.getDisplayName());
        mEmailView.setText(user.getEmail());
        mPhoneNumberView.setText(user.getPhoneNumber());


        /*Bitmap imageBitmap = ImageUtility.GetBitmapByUrl(user.getPhotoUrl().toString());

        if(imageBitmap != null){
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }*/

        final String imageurl = Utility.GetLargeProfileImageUrl(user.getPhotoUrl().toString());
        new DownloadImageTask(new DownloadImageTask.Listener() {
            @Override
            public void onImageDownloaded(Bitmap bitmap) {
                //bitmap = Utility.resizeBitmap(bitmap, 600);
                mBitmap = bitmap;
                mCircleImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onImageDownloadError() {
                Log.e("onImageDownloadError", "Failed to download image from "
                        + imageurl);
            }
        }).execute(imageurl);

    }

    private void attemptRegister() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        name = mNameView.getText().toString();
        email = mEmailView.getText().toString();
        phone = mPhoneNumberView.getText().toString();

        phone = mPhoneNumberView.getText().toString().trim();
        phone = Phone.ReplaceCountryCode(phone);
        mPhoneNumberView.setText(phone);

        password = mPasswordView.getText().toString();
        password2 = mRePasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        // if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
        //mPasswordView.setError(getString(R.string.error_invalid_password));
        //focusView = mPasswordView;
        //cancel = true;
        //}
        // Check for a valid password, if the user entered one.


        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (isPasswordContainsInvalidChar(password)) {
            mPasswordView.setError("Character : is not allowed in the password");
            focusView = mPasswordView;
            cancel = true;
        }

        if (!isSamePassword(password, password2)) {
            mPasswordView.setError(getString(R.string.error_in_retype_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (!validCellPhone(phone)) {
            mPhoneNumberView.setError(getString(R.string.error_invalid_mobile));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            //if facebook login then

            if(_IsFacebookLogin){
                RegisterWithFacebook();
            }else {
                RegisterWithFireBase(email, password);
            }
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView
                mCircleImageView.setImageBitmap(mBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void RegisterWithFacebook(){

        mProgressView.setVisibility(View.VISIBLE);
        auth.getCurrentUser().updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            CompleteRegisteration();
                            mProgressView.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.password_update_failed), Toast.LENGTH_SHORT).show();
                            mProgressView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void RegisterWithFireBase(String email, String password) {

        mProgressView.setVisibility(View.VISIBLE);

        //authenticate user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        //showProgress(true);
                        //mProgressView.clearAnimation();


                        mProgressView.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //Toast.makeText(RegisterActivity.this, getString(R.string.reg_failed), Toast.LENGTH_LONG).show();
                        } else {

                            //auth.getCurrentUser().updatePhoneNumber(PhoneNumberUtils.formatNumber(phone)); needs to do later
                            CompleteRegisteration();

                        }
                    }
                });

    }

    private void CompleteRegisteration(){

        UserGlobalHandler UserHandler = UserGlobalHandler.get_instance();
        UserHandler.setCurrentUserName(name);
        UserHandler.setCurrentUserPhone(phone);
        UserHandler.CurrentUser.setPassword(password);
        UserHandler.SaveUser(mBitmap);

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean isPasswordContainsInvalidChar(String password) {
        //TODO: Replace this with your own logic
        return password.contains(":");
    }

    private boolean isSamePassword(String password1, String password2) {
        //TODO: Replace this with your own logic
        if (password1.equals(password2)){
            return true;
        }
        return false;
    }
    public boolean validCellPhone(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
        //if (number.length() >10 & number.length() < 13){
            //return true;
        //}
        //return false;
    }

    private String GetPhoneNumber() {

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS,PERMISSION_ALL);
        }
        if (ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }
        String sNumber = tManager.getLine1Number();
        if (sNumber.isEmpty()){
            Toast.makeText(this, GlobalConstants.SIM_NOT_FOUND,Toast.LENGTH_SHORT).show();
            return "";
        }
        return sNumber;
    }

    private String GetPhoneNumberBackup() {

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS,PERMISSION_ALL);
        }
        if (ActivityCompat.checkSelfPermission(this, permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }
        String sNumber = tManager.getLine1Number();
        if (sNumber.isEmpty()){
            Toast.makeText(this, GlobalConstants.SIM_NOT_FOUND,Toast.LENGTH_SHORT).show();
            return "";
        }
        return sNumber;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); do nothing to prevent going back
    }
}
