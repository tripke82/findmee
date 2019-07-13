package com.tripmee.findmee.Network;

import android.util.Base64;

import com.tripmee.findmee.Models.CrimeReport.Comment;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Models.SOS.SOSRequest;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.User.UserService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private static GetSOSDataService SOSservice;
    private static UserService userService;
    private static CrimeReportService crimeReportService;
    private static CommentService commentService;
    private static RetrofitService retrofitService;
    private static final String BASE_URL = "https://safierestapi.azurewebsites.net/";
    private static final String TEST_BASE_URL = "https://192.168.1.18:5001/";
    //private ArrayList<Bitmap> ImageList;

    private RetrofitService() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                //.baseUrl(TEST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        SOSservice = retrofit.create(GetSOSDataService.class);
        userService = retrofit.create(UserService.class);
        crimeReportService = retrofit.create(CrimeReportService.class);
        commentService= retrofit.create(CommentService.class);
    }

    public static RetrofitService getInstance() {

        if (retrofitService == null) {
            retrofitService = new RetrofitService();
        }
        return retrofitService;
    }

    public void createSOSRequest(SOSRequest sos, Callback<SOSRequest> callback) {
        Call<SOSRequest> sosRequestCall = SOSservice.createSOSRequest(sos);
        sosRequestCall.enqueue(callback);
    }

    public void GetSOSRequests(Callback<List<SOSRequest>> callback) {
        Call<List<SOSRequest>> Call = SOSservice.getSOSData();
        Call.enqueue(callback);
    }

    public void deleteSOSRequest(String userID, Callback<SOSRequest> callback) {
        Call<SOSRequest> sosRequestCall = SOSservice.deleteSOSRequest(userID);
        sosRequestCall.enqueue(callback);
    }

    public void authenticateFacebookUser(String userID, String token, Callback<User> callback) {
        Call<User> userCall = userService.authenticateFacebook(userID, token);
        userCall.enqueue(callback);
    }

    public void authenticate(User user, Callback<User> callback) {

        String credentials = user.getUserName() + ":" + user.getPassword();
        // create Base64 encodet string
        final String basic =
                "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<User> userCall = userService.authenticate(user.getUserID(), basic);
        userCall.enqueue(callback);
    }

    public void createUser(User user, Callback<User> callback) {
        Call<User> userCall = userService.createUser(user);
        userCall.enqueue(callback);
    }

    public void updateUser(User user, Callback<User> callback) {
        String BearerToken = "Bearer " + user.getToken();
        Call<User> userCall = userService.updateUser(user, BearerToken);
        userCall.enqueue(callback);
    }

    public void getUser(String userID, String token, Callback<User> callback) {
        String BearerToken = "Bearer " + token;
        Call<User> userCall = userService.getUser(userID, BearerToken);
        userCall.enqueue(callback);
    }

    public void getUserByMobile(String mobile, String token, Callback<User> callback) {
        String BearerToken = "Bearer " + token;
        Call<User> userCall = userService.getUserByMobile(mobile, BearerToken);
        userCall.enqueue(callback);
    }

    public void deleteUser(String id, String token, Callback<User> callback) {
        String BearerToken = "Bearer " + token;
        Call<User> userCall = userService.deleteUser(id, BearerToken);
        userCall.enqueue(callback);
    }

    public void createCrimeReport(CrimeReport report, String token, Callback<CrimeReport> callback) {
        String BearerToken = "Bearer " + token;
        Call<CrimeReport> reportCall = crimeReportService.createReport(report, BearerToken);
        reportCall.enqueue(callback);
    }

    public void updateCrimeReport(String id, CrimeReport report, String token,Callback<CrimeReport> callback) {
        String BearerToken = "Bearer " + token;
        Call<CrimeReport> reportCall = crimeReportService.Update(id,report, BearerToken);
        reportCall.enqueue(callback);
    }

    public void GetCrimeReports(String token, Callback<List<CrimeReport>> callback) {
        String BearerToken = "Bearer " + token;
        Call<List<CrimeReport>> Call = crimeReportService.getCrimeReports(BearerToken);
        Call.enqueue(callback);
    }

    public void deleteCrimeReport(String id, String token, Callback<CrimeReport> reportCallback) {
        String BearerToken = "Bearer " + token;
        Call<CrimeReport> crimeReportCall = crimeReportService.deleteReport(id, BearerToken);
        crimeReportCall.enqueue(reportCallback);
    }

    public void postComment(Comment comment, String token, Callback<Comment> callback) {
        String BearerToken = "Bearer " + token;
        Call<Comment> commentCall = commentService.createComment(comment, BearerToken);
        commentCall.enqueue(callback);
    }

    public void getComments(String id, String token, Callback<List<Comment>>  callback) {
        String BearerToken = "Bearer " + token;
        Call<List<Comment>>  Call = commentService.getComments(id, BearerToken);
        Call.enqueue(callback);
    }

    public void deleteComment(String id, String token, Callback<Comment> callback) {
        String BearerToken = "Bearer " + token;
        Call<Comment> commentCall = commentService.deleteComment(id, BearerToken);
        commentCall.enqueue(callback);
    }

}