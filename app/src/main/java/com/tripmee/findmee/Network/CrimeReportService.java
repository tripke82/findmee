package com.tripmee.findmee.Network;

import com.tripmee.findmee.Models.CrimeReport.CrimeReport;

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

public interface CrimeReportService {

    @GET("api/crimereport/")
    @Headers({"Content-Type: application/json"})
    Call<List<CrimeReport>> getCrimeReports(@Header("Authorization") String token);

    @POST("api/crimereport/")
    @Headers({"Content-Type: application/json"})
    Call<CrimeReport> createReport(@Body CrimeReport report, @Header("Authorization") String token);

    @PUT("api/crimereport/{id}")
    @Headers({"Content-Type: application/json"})
    Call<CrimeReport> Update(@Path("id") String id, @Body CrimeReport report, @Header("Authorization") String token);

    @DELETE("api/crimereport/{id}")
    Call<CrimeReport> deleteReport(@Path("id") String id, @Header("Authorization") String token);
}
