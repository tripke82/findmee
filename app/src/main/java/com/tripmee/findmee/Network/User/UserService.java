package com.tripmee.findmee.Network.User;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService{

    @POST("api/user/authenticate/facebook/{userId}")
    @Headers({"Content-Type: application/json"})
    Call<User> authenticateFacebook(@Path("userId") String userId, @Header(GlobalConstants.FACEBOOK_TOKEN) String authHeader);

    @POST("api/user/authenticate/{userId}")
    @Headers({"Content-Type: application/json"})
    Call<User> authenticate(@Path("userId") String userId,  @Header("Authorization") String authHeader);

    @GET("api/user/")
    @Headers({"Content-Type: application/json"})
    Call<List<User>> getUsers(@Header("Authorization") String token);

    @GET("api/user/{userId}")
    @Headers({"Content-Type: application/json"})
    Call<User> getUser(@Path("userId") String userId, @Header("Authorization") String token);

    @GET("api/user/mobile/{mobile}")
    @Headers({"Content-Type: application/json"})
    Call<User> getUserByMobile(@Path("mobile") String mobile, @Header("Authorization") String token);

    @POST("api/user/")
    @Headers({"Content-Type: application/json"})
    Call<User> createUser(@Body User user);

    //@GET("api/Profiles/GetProfile?id={id}")
    //Call<User> getUser(@Path("id") String id, @Header("Authorization") String authHeader);

    @PUT("api/user/")
    @Headers({"Content-Type: application/json"})
    Call<User> updateUser(@Body User user, @Header("Authorization") String token);

    @DELETE("api/user/{userId}")
    @Headers({"Content-Type: application/json"})
    Call<User> deleteUser(@Path("userId") String userId, @Header("Authorization") String token);
}
