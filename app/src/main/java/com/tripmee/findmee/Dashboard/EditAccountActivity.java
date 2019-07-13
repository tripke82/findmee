package com.tripmee.findmee.Dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.LoginRegister.LoginActivity;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends AppCompatActivity {

    private Button btnChangeEmail,btnChangeName,btnChangePhone, btnChangePassword, btnSendResetPassword, btnRemoveUser,
            btnSelectImage, btnRemoveImage, changeEmail, changeName, changePhone, changePassword, sendEmail, remove, signOut;
    private CircleImageView mCircleImageView;
    private EditText oldEmail, newEmail, newName, newPhone, password, newPassword, passwordDialogEditText;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private UserGlobalHandler UserHandler;

    private Uri filePath;;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.edit_root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) coordinatorLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(EditAccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        UserHandler = UserGlobalHandler.get_instance();
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);

        btnSelectImage = (Button) findViewById(R.id.btnSelectNewImage);
        btnRemoveImage = (Button) findViewById(R.id.btnRemoveImage);
        btnChangeEmail = (Button) findViewById(R.id.change_email_button);
        btnChangeName = (Button) findViewById(R.id.change_name_button);
        btnChangePhone = (Button) findViewById(R.id.change_phone_button);
        btnChangePassword = (Button) findViewById(R.id.change_password_button);
        btnSendResetPassword = (Button) findViewById(R.id.sending_pass_reset_button);
        btnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        changeEmail = (Button) findViewById(R.id.changeEmail);
        changeName = (Button) findViewById(R.id.changeName);
        changePhone = (Button) findViewById(R.id.changePhone);
        changePassword = (Button) findViewById(R.id.changePass);
        sendEmail = (Button) findViewById(R.id.send);
        remove = (Button) findViewById(R.id.remove);
        signOut = (Button) findViewById(R.id.sign_out);

        oldEmail = (EditText) findViewById(R.id.old_email);
        newEmail = (EditText) findViewById(R.id.new_email);
        newName = (EditText) findViewById(R.id.new_name);
        newPhone = (EditText) findViewById(R.id.new_phone);
        password = (EditText) findViewById(R.id.password);
        newPassword = (EditText) findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        newName.setVisibility(View.GONE);
        newPhone.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changeName.setVisibility(View.GONE);
        changePhone.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        Bitmap imageBitmap = null;
        String sImage = UserHandler.CurrentUser.get_FireBaseProfileImage();

        if (!Utility.StringIsBlankOrEmpty(sImage)) {
            imageBitmap = ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage());
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), GlobalConstants.PICK_IMAGE_REQUEST);
            }
        });

        btnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove image
                //mCircleImageView.setImageBitmap(mBitmap);
                mCircleImageView.setImageResource(R.drawable.default_profile);
                UserHandler.RemoveProfilePhoto();
            }
        });


        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                newName.setVisibility(View.GONE);
                newPhone.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changeName.setVisibility(View.GONE);
                changePhone.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //UserHandler.CurrentUser.setEmail(newEmail.getText().toString().trim());
                                        //update changes
                                        UserHandler.UpdateEmail(newEmail.getText().toString().trim());
                                        Toast.makeText(EditAccountActivity.this, getString(R.string.email_updated), Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(EditAccountActivity.this, getString(R.string.email_update_failed), Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                    //UserHandler.SaveUser();
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                newName.setVisibility(View.VISIBLE);
                newPhone.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changeName.setVisibility(View.VISIBLE);
                changePhone.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newName.getText().toString().trim().equals("")) {

                    UserHandler.UpdateName(newName.getText().toString().trim());
                    Toast.makeText(EditAccountActivity.this, getString(R.string.name_updated), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                } else if (newName.getText().toString().trim().equals("")) {
                    newName.setError("Enter Name");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                newName.setVisibility(View.GONE);
                newPhone.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changeName.setVisibility(View.GONE);
                changePhone.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPhone.getText().toString().trim().equals("")) {

                    String Number = newPhone.getText().toString().trim();
                    Number = Phone.ReplaceCountryCode(Number);
                    newPhone.setText(Number);

                    UserHandler.UpdatePhone(Number);
                    Toast.makeText(EditAccountActivity.this, getString(R.string.mobile_updated), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                } else if (newName.getText().toString().trim().equals("")) {
                    newPhone.setError("Enter Phone Number");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                newName.setVisibility(View.GONE);
                newPhone.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changeName.setVisibility(View.GONE);
                changePhone.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError(getString(R.string.password_too_short));
                        progressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(EditAccountActivity.this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(EditAccountActivity.this, getString(R.string.password_update_failed), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError(getString(R.string.enter_password));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnSendResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                newName.setVisibility(View.GONE);
                newPhone.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changeName.setVisibility(View.GONE);
                changePhone.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditAccountActivity.this, getString(R.string.reset_password_email_sent), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(EditAccountActivity.this, getString(R.string.failed_reset_password_email), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                if(Utility.IsFacebookLogin(user)) {
                    DeleteFacebookUser();
                }else{
                    AttemptReauthentication(user);
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting image to ImageView
                mCircleImageView.setImageBitmap(mBitmap);

                String PreviousImagePath = UserHandler.CurrentUser.getProfileImageUrl();
                UserHandler.SaveProfilePicture(mBitmap,PreviousImagePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void AttemptReauthentication(final FirebaseUser user){

        LayoutInflater layoutInflater = LayoutInflater.from(EditAccountActivity.this);
        View popupInputDialogView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Enter Password");
        //builder.setIcon(R.drawable.ic_launcher_background);

        builder.setView(popupInputDialogView);
        builder.setCancelable(true);
        final AlertDialog alertDialog = builder.create();

        alertDialog.show();
        passwordDialogEditText = (EditText) popupInputDialogView.findViewById(R.id.password);
        Button btnContinue = (Button)popupInputDialogView.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordDialogEditText.getText().toString();
                // Check for a valid password, if the user entered one.
                if (!Utility.isPasswordValid(password)) {
                    passwordDialogEditText.setError(getString(R.string.error_invalid_password));
                    passwordDialogEditText.requestFocus();
                }else{
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), password);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        // there was an error
                                        Toast.makeText(EditAccountActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        //Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }else {
                                        DeleteAlertMessage(user);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void DeleteFacebookUser(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = getString(R.string.delete_account_confirmation_question);

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                UnitOfWork.get_instance(UserHandler.CurrentUser).ClearAll();
                UserHandler.DeleteUser();
                DeleteAlertMessage(auth.getCurrentUser());
                auth.signOut();
                LoginManager.getInstance().logOut();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    private void DeleteAlertMessage(final FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = getString(R.string.delete_account_confirmation_question);

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                UnitOfWork.get_instance(UserHandler.CurrentUser).ClearAll();
                UserHandler.DeleteUser();
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditAccountActivity.this, getString(R.string.delete_account_confirmation_message), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(EditAccountActivity.this, LoginActivity.class));
                                    finish();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(EditAccountActivity.this, getString(R.string.failed_to_delete_account), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //sign out method
    public void signOut() {

        UnitOfWork.get_instance(UserHandler.CurrentUser).ClearAll();
        UserHandler.ClearAll();
        UserHandler = null;
        auth.signOut();
        LoginManager.getInstance().logOut();
        //FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(EditAccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
