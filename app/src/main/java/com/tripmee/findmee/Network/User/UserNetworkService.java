package com.tripmee.findmee.Network.User;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.RetrofitService;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserNetworkService {

    private static User _User;
    private static User _responseUser;
    //private static String Token;
    public static RetrofitService _RetrofitService;
    public ActionCompleteCallBack getUserCallBack;
    public ActionCompleteCallBack AuthCallBack;

    private static UserNetworkService _instance;

    public static synchronized UserNetworkService get_instance(User user, RetrofitService retrofitService) {

        if (_instance == null) {

            _instance = new UserNetworkService();
            _User = user;
            _RetrofitService = RetrofitService.getInstance();
        }
        return _instance;
    }

    public void ClearAll(){

        _instance = null;
        _User = null;
        _RetrofitService = null;
    }

    public void SaveUser(final Bitmap bitmap, final String fileName){

        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        String imageName = "";

                        if(bitmap!=null) {
                            //send the image and get the image uri
                            //bitmap.

                            InputStream inputStream = ImageUtility.BitmapToInputStream(bitmap);
                            imageName = ImageUtility.UploadImage(inputStream, inputStream.available(), fileName);
                        }

                        _User.setProfileImageUrl(imageName);
                        CreateUser(_User);

                        handler.post(new Runnable() {

                            public void run() {
                                //Toast.makeText(getContext(), "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                //Toast.makeText(getContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }});
            th.start();
        }
        catch(Exception ex) {

            //Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void DeleteUser(final User user){

        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        DeleteProfileImage(user.getProfileImageUrl());
                        handler.post(new Runnable() {

                            public void run() {
                                //Toast.makeText(getContext(), "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        Log.d("DeleteReport:Exception", exceptionMessage);
                        handler.post(new Runnable() {
                            public void run() {
                                //Toast.makeText(getContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }});
            th.start();
        }
        catch(Exception ex) {

            Log.e("DeleteReport:Exception:", ex.getMessage());
            // Toast.makeText(get, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        _RetrofitService.deleteUser(String.valueOf(user.getUserID()),user.getToken(), new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //Log.e("Delected", "Response" + response.message());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) { // this is not an error, it just returns nothing
                String s = call.toString();
                Log.e("OnFailure", "Failure" + s.toString());
            }
        });
    }

    public void UpdateUser(User user){

        _RetrofitService.updateUser(user, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //Log.e("Delected", "Response" + response.message());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) { // this is not an error, it just returns nothing
                String s = call.toString();
                Log.e("OnFailure", "Failure" + s.toString());
            }
        });
    }

    public void RemoveProfilePic(final User user, final String PreviousImgPath){
        //this function may cause a problem if CurrentUser in Userhandler fields are changed,
        // it wouldn't be updated as it will still be using initial _User. Need to fix that

        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {
                        DeleteProfileImage(PreviousImgPath);
                        _User.setProfileImageUrl("");
                        UpdateUser(user);

                        handler.post(new Runnable() {

                            public void run() {
                                //Toast.makeText(getContext(), "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                //Toast.makeText(getContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }});
            th.start();
        }
        catch(Exception ex) {

            //Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void UpdateUserProfilePic(final Bitmap bitmap, final String fileName, final String PreviousImgPath){
        //this function may cause a problem if CurrentUser in Userhandler fields are changed,
        // it wouldn't be updated as it will still be using initial _User. Need to fix that

        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        String imageName = "";

                        DeleteProfileImage(PreviousImgPath);
                        if(bitmap!=null) {
                            //send the image and get the image uri
                            //bitmap.

                            InputStream inputStream = ImageUtility.BitmapToInputStream(bitmap);
                            imageName = ImageUtility.UploadImage(inputStream, inputStream.available(), fileName);
                        }

                        _User.setProfileImageUrl(imageName);
                        UpdateUser(_User);

                        handler.post(new Runnable() {

                            public void run() {
                                //Toast.makeText(getContext(), "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                //Toast.makeText(getContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }});
            th.start();
        }
        catch(Exception ex) {

            //Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void AddLoggedinUserToDB(final User user){
        _RetrofitService.getUser(user.getUserID(),user.getToken(), new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) {

                } else { // if user not found in db then add user
                    SaveUser(user.GetProfileImage(), user.getUserID());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

    }

    public void GetUser(User user){
        _RetrofitService.getUser(user.getUserID(), user.getToken(), new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) {

                    //_User.setProfileImageUrl(responseUser.getProfileImageUrl());
                    _responseUser = responseUser;
                    if(getUserCallBack != null){
                        getUserCallBack.CallBack();
                    }

                } else {

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void GetUserByMobile(String mobile, String token){
        _RetrofitService.getUserByMobile(mobile, token, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();

                if (response.isSuccessful() && responseUser != null) {


                } else {

                }
                //GetUserCallBack();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                //GetUserCallBack();
            }
        });
    }

    public void AuthenticateFacebookUser(final String userID, String FacebookToken){

        _RetrofitService.authenticateFacebookUser(userID, FacebookToken,new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) { //if authenticated set jwt

                    _responseUser = responseUser;
                    if(AuthCallBack != null){
                        AuthCallBack.CallBack();
                    }

                } else { // if user not found

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void Authenticate(User user){

        _RetrofitService.authenticate(user,new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) { //if authenticated set jwt

                    _responseUser = responseUser;
                    if(AuthCallBack != null){
                        AuthCallBack.CallBack();
                    }

                } else { // if user not found

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public static User get_responseUser() {
        return _responseUser;
    }

    private void GetUserCallBack(){
        if (getUserCallBack != null){
            getUserCallBack.CallBack();
        }
    }

    private void CreateUser(User user){
        _RetrofitService.createUser(user, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User responseUser = response.body();
                if (response.isSuccessful() && responseUser != null) {


                } else {

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void DeleteProfileImage(String PreviousImagePath){


        if(!Utility.StringIsBlankOrEmpty(PreviousImagePath)){ //delete previous image

            try {
                CloudBlobContainer container = ImageUtility.getContainer();

                String path = container.getStorageUri().getPrimaryUri().toString() + "/";
                String filename = PreviousImagePath.replace(path, "");
                CloudBlockBlob imageBlob = container.getBlockBlobReference(filename);

                boolean b = imageBlob.deleteIfExists();

            }catch(Exception ex){
                Log.d("Exception:ProfileImage", ex.getMessage());
            }
        }

    }
}
