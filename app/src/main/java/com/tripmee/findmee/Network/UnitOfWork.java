package com.tripmee.findmee.Network;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.tripmee.findmee.Network.RetrofitService;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.Comment;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Models.CrimeReport.CrimeReportList;
import com.tripmee.findmee.Models.CrimeReport.UserComment;
import com.tripmee.findmee.Models.CrimeReport.UserCommentList;
import com.tripmee.findmee.Models.SOS.SOSRequest;
import com.tripmee.findmee.Models.SOS.SOSRequestList;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.User.UserNetworkService;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnitOfWork {

    private static User _User;
    private static User _responseUser;
    private static SOSRequestList _SOSRequestList;
    private static ArrayList<Comment> _Comments = null;
    public static ArrayList<UserComment> _UserComments = new ArrayList<UserComment>();
    public static UserCommentList _UserCommentList = new UserCommentList();

    private static CrimeReportList _CrimeReportList;
    public static RetrofitService _RetrofitService;
    public ActionCompleteCallBack actionCompletedCallBack;
    public ActionCompleteCallBack crimeReportedCallBack;
    public ActionCompleteCallBack crimeReportsLoadedCallBack;
    public ActionCompleteCallBack deleteActionCallBack;
    public ActionCompleteCallBack foundCommentsCallBack;
    public ActionCompleteCallBack postCommentsCallBack;
    //public ActionCompleteCallBack getUserCallBack;

    private static UnitOfWork _instance;

    private static UserNetworkService _UserNetworkService;

    public static synchronized UnitOfWork get_instance(User user) {

        if (_instance == null) {

            _instance = new UnitOfWork();
            _User = user;
            _RetrofitService = RetrofitService.getInstance();

            _UserNetworkService =  UserNetworkService.get_instance(user, _RetrofitService);
            _instance.LoadCrimeReports();
            //_instance.LoadSOSRequests(); //sos requests will be included in the next versions
        }
        return _instance;
    }

    public void ClearAll(){

        _instance = null;
        _User = null;
        _UserNetworkService.ClearAll();
        _UserNetworkService = null;
    }

    public static UserNetworkService GetUserNetworkService() {
        return _UserNetworkService;
    }

    public void SubmitCrimeReport(final CrimeReport crimeReport, final String Path, final Bitmap bitmap)
    {
        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        if(!Utility.StringIsBlankOrEmpty(Path)){
                            //send the image and get the image uri
                            String fileName = Utility.GetFileNameByPath(Path);

                            if (fileName.isEmpty() || fileName == "") {
                                fileName = crimeReport.getUserID() + crimeReport.getDateTime().toString();
                            }

                            Bitmap finalbitmap = ImageUtility.resizeBitmap(bitmap, GlobalConstants.CRIME_IMAGE_SIZE);
                            InputStream inputStream = ImageUtility.BitmapToInputStream(finalbitmap);
                            String imageUrl = UploadImage(inputStream, inputStream.available(), fileName);
                            crimeReport.setImageUrl(imageUrl);
                            PostCrimeReport(crimeReport, finalbitmap);

                        }else{

                            PostCrimeReport(crimeReport,bitmap);
                        }

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

            Log.e("SubmitCrimeReport:", ex.getMessage());
           // Toast.makeText(get, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void PostCrimeReport(CrimeReport crimeReport, final Bitmap bitmap){

        _RetrofitService.createCrimeReport(crimeReport,_User.getToken(), new Callback<CrimeReport>() {
            @Override
            public void onResponse(Call<CrimeReport> call, Response<CrimeReport> response) {
                CrimeReport report = response.body();
                if (response.isSuccessful() && report != null) {
                    _CrimeReportList.Add(report, bitmap);
                    Log.e("OnSuccess", report.getComments());
                     if(crimeReportedCallBack != null){
                        crimeReportedCallBack.CallBack();
                    }
                } else {
                    //Toast.makeText(getContext(), String.format("Response is %s", String.valueOf(response.code()))
                    //, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CrimeReport> call, Throwable t) { // this is not an error, it just returns nothing
                Log.e("OnFailure", t.getMessage());
            }
        });
    }

    public void UpdateCrimeReport(final CrimeReport crimeReport,final String Path, final Bitmap bitmap, final String PreviousImagePath)
    {
        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        //this is where we need to remove the previous image in collection
                        DeleteCrimeImage(PreviousImagePath, crimeReport.getID());

                        if(!Utility.StringIsBlankOrEmpty(Path)){
                            //send the image and get the image uri
                            String fileName = Utility.GetFileNameByPath(Path);

                            if (fileName.isEmpty() || fileName == "") {
                                fileName = crimeReport.getUserID() + crimeReport.getDateTime().toString();
                            }

                            Bitmap finalbitmap = ImageUtility.resizeBitmap(bitmap, GlobalConstants.CRIME_IMAGE_SIZE);

                            InputStream inputStream = ImageUtility.BitmapToInputStream(finalbitmap);
                            String imageUrl = UploadImage(inputStream, inputStream.available(), fileName);
                            crimeReport.setImageUrl(imageUrl);
                            _CrimeReportList.Update(crimeReport,finalbitmap);
                            UpdateCrimeReport(crimeReport);

                        }else{

                            _CrimeReportList.Update(crimeReport,null);
                            UpdateCrimeReport(crimeReport);
                        }

                        handler.post(new Runnable() {

                            public void run() {
                                //Toast.makeText(getContext(), "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        Log.d("UpdateCrimeReport", exceptionMessage);
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

            Log.e("SubmitCrimeReport:", ex.getMessage());
            // Toast.makeText(get, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateCrimeReport(CrimeReport report){

        _RetrofitService.updateCrimeReport(String.valueOf(report.getID()),report,_User.getToken(), new Callback<CrimeReport>() {
            @Override
            public void onResponse(Call<CrimeReport> call, Response<CrimeReport> response) {
                CrimeReport report = response.body();
                if (response.isSuccessful() && report != null) {

                    if(crimeReportedCallBack != null){
                        crimeReportedCallBack.CallBack();
                    }
                } else {
                    //Toast.makeText(getContext(), String.format("Response is %s", String.valueOf(response.code()))
                    //, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CrimeReport> call, Throwable t) { // this is not an error, it just returns nothing
                Log.e("OnFailure", t.getMessage());
            }
        });
    }

    public void DeleteCrimeReport(final Integer id, final String PreviousImagePath){

        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        DeleteCrimeImage(PreviousImagePath, id);
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

        _RetrofitService.deleteCrimeReport(String.valueOf(id),_User.getToken(), new Callback<CrimeReport>() {
            @Override
            public void onResponse(Call<CrimeReport> call, Response<CrimeReport> response) {
                //Log.e("Delected", "Response" + response.message());
            }

            @Override
            public void onFailure(Call<CrimeReport> call, Throwable t) { // this is not an error, it just returns nothing
                String s = call.toString();
                Log.e("OnFailure", "Failure" + s.toString());
                if(deleteActionCallBack != null){
                    deleteActionCallBack.CallBack();
                }
            }
        });

    }

    public static CrimeReportList get_CrimeReportList() {
        return _CrimeReportList;
    }

    public void LoadCrimeReports(){
       _CrimeReportList = new CrimeReportList();

        _RetrofitService.GetCrimeReports(_User.getToken(), new Callback<List<CrimeReport>>() {
            @Override
            public void onResponse(Call<List<CrimeReport>> call, Response<List<CrimeReport>> response) {

                //Toast.makeText(getActivity(), "Number of reports:" + response.body().size(), Toast.LENGTH_SHORT).show();
                _CrimeReportList.setCrimeReportList((ArrayList)response.body());
                _CrimeReportList.LoadImages();

                if(crimeReportsLoadedCallBack!= null){
                    crimeReportsLoadedCallBack.CallBack();
                }
            }

            @Override
            public void onFailure(Call<List<CrimeReport>> call, Throwable t) {
                Log.e("exception:", t.getMessage());
            }
        });
    }

    public CrimeReport GetReport(String userID, String id){
        ArrayList<CrimeReport> crimeReports = _CrimeReportList.getCrimeReports();

        for (CrimeReport r: crimeReports) {
            if(r.getUserID().equals(userID) && r.getID() == Integer.valueOf(id)){
                return r;
            }
        }
        return null;
    }

    public SOSRequestList get_SOSRequestList(){
        return _SOSRequestList;
    }

    public void SendSOSRequest(final String Path, final String fileName, final Bitmap bitmap, final String OptionalMessage)
    {
        try {

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {

                        //send the image and get the image uri
                        //final String imageName = UploadImageByPath(Path, fileName); changed it with the line below to reduce size
                        InputStream inputStream = ImageUtility.BitmapToInputStream(bitmap);
                        final String imageName = UploadImage(inputStream, inputStream.available(), fileName);
                        SOSRequest sos = InitSOSRequest(_User, imageName);

                        if (!Utility.StringIsBlankOrEmpty(OptionalMessage)){
                            sos.setMessage(OptionalMessage);
                        }

                        PostSOSRequest(sos, bitmap);

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

    public static String UploadImageByPath(String path, String filename){
        InputStream inputStream;
        String imagePath = "";
        try {
            inputStream = ImageUtility.PathToInputStream(path);
            final int imageLength = inputStream.available();

            try {
                imagePath = UploadImage(inputStream, imageLength, filename);
            }catch (Exception ex){
                String message = ex.getMessage();
            }
        }catch(IOException ex){
            String message = ex.getMessage();
        }
        return imagePath;
    }

    public static String UploadImage(InputStream image, int imageLength, String imageName) throws Exception {
        CloudBlobContainer container = getContainer();

        container.createIfNotExists();

        CloudBlockBlob imageBlob = container.getBlockBlobReference(imageName);
        imageBlob.upload(image, imageLength);

        return imageBlob.getStorageUri().getPrimaryUri().toString();
    }

    private void DeleteCrimeImage(String PreviousImagePath, Integer ReportID){


        if(!Utility.StringIsBlankOrEmpty(PreviousImagePath)){ //delete previous image

            try {
                CloudBlobContainer container = getContainer();

                String path = container.getStorageUri().getPrimaryUri().toString() + "/";
                String filename = PreviousImagePath.replace(path, "");

                //container.createIfNotExists();
                //String path = URLDecoder.decode(imagePath, "UTF-8");
                CloudBlockBlob imageBlob = container.getBlockBlobReference(filename);

                boolean b = imageBlob.deleteIfExists();

                if (b) {
                    _CrimeReportList.DeleteImage(PreviousImagePath, ReportID);
                    Log.d("DeleteImage", "Success");
                }
            }catch(Exception ex){
                Log.d("Exception:DeleteImage", ex.getMessage());
            }
        }

    }

    private void LoadSOSRequests(){
        _SOSRequestList = new SOSRequestList();

        _RetrofitService.GetSOSRequests(new Callback<List<SOSRequest>>() {
            @Override
            public void onResponse(Call<List<SOSRequest>> call, Response<List<SOSRequest>> response) {

                _SOSRequestList.setSOSRequestList((ArrayList)response.body());
                _SOSRequestList.SortByDate();
                _SOSRequestList.LoadImages();
                _SOSRequestList.LoadUsers();
            }

            @Override
            public void onFailure(Call<List<SOSRequest>> call, Throwable t) {

            }
        });
    }

    public void DeleteSOSRequest(Integer id){

        _SOSRequestList.Delete(id);
        _RetrofitService.deleteSOSRequest(String.valueOf(id), new Callback<SOSRequest>() {
            @Override
            public void onResponse(Call<SOSRequest> call, Response<SOSRequest> response) {
                //Log.e("Delected", "Response" + response.message());
            }

            @Override
            public void onFailure(Call<SOSRequest> call, Throwable t) { // this is not an error, it just returns nothing
                String s = call.toString();
                Log.e("OnFailure", "Failure" + s.toString());
                if(deleteActionCallBack != null){
                    deleteActionCallBack.CallBack();
                }
            }
        });
    }

    private SOSRequest InitSOSRequest(User user, String imagePath){

        SOSRequest sos = new SOSRequest(user.getUserID(),imagePath,user.getLatitude(),user.getLongitude());

        //String l = Utility.ObjectToString(user.getLatitude());
        String sdate = Utility.FormatDateToString(Calendar.getInstance().getTime(), GlobalConstants.DATE_FORMAT);

        //sos.setDateTime("2019-02-15");
        sos.setDateTime(sdate);
        sos.setUserName(_User.getUserName());
        sos.setMessage(GetEmergencyMessage(sdate));

        return sos;
    }

    private static CloudBlobContainer getContainer() throws Exception {
        // Retrieve storage account from connection-string.

        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(GlobalConstants.STORAGE_CONNECTION_STRING);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("images");

        return container;
    }

    private String GetEmergencyMessage(String sDateTime){
        String msg = _User.getUserName() + " sent an Emergency Request " + sDateTime;
        return msg;
    }

    private void PostSOSRequest(SOSRequest sosRequest, final Bitmap bitmap){

        _RetrofitService.createSOSRequest(sosRequest, new Callback<SOSRequest>() {
            @Override
            public void onResponse(Call<SOSRequest> call, Response<SOSRequest> response) {
                SOSRequest responseSOS = response.body();
                if (response.isSuccessful() && responseSOS != null) {
                    _SOSRequestList.Add(responseSOS,bitmap);
                    _SOSRequestList.SortByDate();


                    if(actionCompletedCallBack != null){
                        actionCompletedCallBack.CallBack();
                    }
                } else {
                    //Toast.makeText(getContext(), String.format("Response is %s", String.valueOf(response.code()))
                    //, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SOSRequest> call, Throwable t) { // this is not an error, it just returns nothing
                Log.e("OnFailure", t.getMessage());
            }
        });
    }

    public static UserCommentList get_UserCommentList() {
        //return _UserComments;
        return _UserCommentList;
    }

    public void PostComment(Comment comment){
        _RetrofitService.postComment(comment, _User.getToken(), new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                Comment comment1 = response.body();
                if (response.isSuccessful() && comment1 != null) {

                    _UserCommentList.Add(UserGlobalHandler.get_instance().CurrentUser, comment1);
                    if(postCommentsCallBack != null){
                        postCommentsCallBack.CallBack();
                    }
                } else {
                    //Toast.makeText(getContext(), String.format("Response is %s", String.valueOf(response.code()))
                    //, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) { // this is not an error, it just returns nothing
                Log.e("OnFailure", t.getMessage());
            }
        });
    }

    public void GetComments(int ReportID){

        _RetrofitService.getComments(String.valueOf(ReportID),_User.getToken(), new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {

                //Toast.makeText(getActivity(), "Number of reports:" + response.body().size(), Toast.LENGTH_SHORT).show();

                ArrayList<Comment> Comments = (ArrayList)response.body();

                _UserCommentList.AddComments(Comments);
                if(foundCommentsCallBack != null){
                    foundCommentsCallBack.CallBack();
                }

            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {

            }
        });

    }

    public void DeleteComment(final String commentID){

        _UserCommentList.Delete(commentID);
        _RetrofitService.deleteComment(commentID,_User.getToken(), new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                //Log.e("Delected", "Response" + response.message());
                //_UserCommentList.Delete(ID);
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) { // this is not an error, it just returns nothing
                //_UserCommentList.Delete(ID);
                String s = call.toString();
                Log.e("OnFailure", "Failure" + s.toString());
            }
        });
    }

}
