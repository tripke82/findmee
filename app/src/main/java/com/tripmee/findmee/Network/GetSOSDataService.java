package com.tripmee.findmee.Network;

import com.tripmee.findmee.Models.SOS.SOSRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GetSOSDataService {

    @GET("api/sosrequest/")
    Call<List<SOSRequest>> getSOSData();
    //Call<SOSRequestList> getSOSData();

    @POST("api/sosrequest/")
    @Headers({"Content-Type: application/json"})
    Call<SOSRequest> createSOSRequest(@Body SOSRequest sosRequest);

    @DELETE("api/sosrequest/{id}")
    Call<SOSRequest> deleteSOSRequest(@Path("id") String id);

}
