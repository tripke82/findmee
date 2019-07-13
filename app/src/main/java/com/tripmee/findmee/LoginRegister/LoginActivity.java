package com.tripmee.findmee.LoginRegister;

import android.Manifest.permission;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.MainActivity;
import com.tripmee.findmee.Models.UserStatus;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Permissions;
import com.tripmee.findmee.Utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private UserGlobalHandler UserHandler;
    private CallbackManager mCallbackManager;
    private String _MobileNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
        if (!Permissions.IsPermissionGranted(this)) {
            requestPermissions(Permissions.PERMISSIONS, Permissions.PERMISSION_ALL);
            //requestPermissions(Permissions.PERMISSIONS2, Permissions.PERMISSION_ALL);
        }
        mEmailView = (EditText) findViewById(R.id.editText);
        mPasswordView = (EditText) findViewById(R.id.editPassword);

        _MobileNumber = GetPhoneNumber();

        //printKeyHash();

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        Redirect();

        Button mEmailSignInButton = (Button) findViewById(R.id.btnLogin);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mLinkToRegisterButton = (Button) findViewById(R.id.btnLinkToRegister);
        mLinkToRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, com.tripmee.findmee.LoginRegister.RegisterActivity.class));
            }
        });

        InitFacebookLogin();

        Button btnReset = (Button) findViewById(R.id.btn_reset_password);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, com.tripmee.findmee.LoginRegister.ResetPasswordActivity.class));
            }
        });

        mProgressView = (ProgressBar)findViewById(R.id.progressBar);

    }

    private void Redirect(){
        if (auth.getCurrentUser() != null) {

            UserHandler = UserGlobalHandler.get_instance();
            UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() { //when data is loaded
                public void CallBack() {
                    //do nothing
                }
            };

            if (UserHandler.CurrentUser.Status == UserStatus.RESTRICTED){

                Intent intent = new Intent(LoginActivity.this, com.tripmee.findmee.LoginRegister.ActivationActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        // if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            //mPasswordView.setError(getString(R.string.error_invalid_password));
            //focusView = mPasswordView;
            //cancel = true;
        //}
        // Check for a valid password, if the user entered one.
        if (!Utility.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Utility.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            SignInWithUserNamePassword(email, password);
        }

    }

    private void SignInWithUserNamePassword(String email, final String password) {

        mProgressView.setVisibility(View.VISIBLE);
        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        mProgressView.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                        } else {

                            UserHandler = UserGlobalHandler.get_instance(null, password);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                });
    }

    private void InitFacebookLogin(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton mFacebookLogin = (LoginButton) findViewById(R.id.btnFacebookLogin);

        mFacebookLogin.setReadPermissions("email", "public_profile");

        mFacebookLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Log.d(TAG, "facebook:onSuccess:" + loginResult);
                //GetProfileDetails(loginResult);

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                //Log.d(TAG, "facebook:onCancel");
                // ...
                Utility.displayMessage(LoginActivity.this, getString(R.string.facebook_login_canceled));
            }

            @Override
            public void onError(FacebookException error) {
                //Log.d(TAG, "facebook:onError", error);
                // ...
                Utility.displayMessage(LoginActivity.this, getString(R.string.facebook_login_error)+ error.getMessage());
            }
        });
    }
    private void handleFacebookAccessToken(final AccessToken token) {
        //Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]


        mProgressView.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //mProgressView.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            UserHandler = UserGlobalHandler.get_instance(token, null);
                            String id = token.getUserId();

                            Log.d("fbuser", id);

                            //UserHandler.InitCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("Facebook", "Loggedin");
                            startActivity(intent);
                            finish();


                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mProgressView.setVisibility(View.GONE);
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void GetProfileDetails(LoginResult loginResult) {

        AccessToken accessToken = loginResult.getAccessToken();

        /* make the API call */
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (object == null) {
                            //cUtils.Toast("Please try again!", getApplicationContext());
                        } else {
                            try {
                                String str_datestr = object.getString("birthday");
                                String strd = str_datestr.replaceAll("\\\\/", "/");
                                String str_date = strd.replaceAll("/", "-");
                                String email = object.getString("email");
                                //facebook_id = object.getString("id");
                                //first_name = object.getString("first_name");
                                //last_name = object.getString("last_name");
                                String user_location = object.getJSONObject("location").toString();
                                String gender = object.getString("gender");
                                //verified = object.getBoolean("verified");
                                //id = object.getString("id");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        //Bundle parameters = new Bundle();
        //parameters.putString("fields", "id,first_name,last_name,email,gender,verified,age_range,hometown, birthday,location");
        //request.setParameters(parameters);
        //request.executeAsync();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    private String GetPhoneNumber() {

        String sNumber = "";
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Permissions.PERMISSIONS,Permissions.PERMISSION_ALL);
            ActivityCompat.requestPermissions(this, Permissions.PERMISSIONS,Permissions.PERMISSION_ALL);
            //requestPermissions(this, PERMISSIONS,PERMISSION_ALL);
        }
        if (ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }else {
            sNumber = tManager.getLine1Number();
            if (sNumber.isEmpty()) {
                Toast.makeText(this, GlobalConstants.SIM_NOT_FOUND, Toast.LENGTH_SHORT).show();
                return "";
            }
        }
        return sNumber;
    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.paranoidcompany.sam.findmee", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            //mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    //show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                //@Override
                //public void onAnimationEnd(Animator animation) {
                    //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                //}
            //});

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
