package com.tripmee.findmee.Network;

import com.tripmee.findmee.Models.CrimeReport.Comment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentService {

    //@GET("api/comment/")
    //Call<List<Comment>> getComments();

    @GET("api/comment/{reportId}")
    @Headers({"Content-Type: application/json"})
    Call<List<Comment>> getComments(@Path("reportId") String reportId, @Header("Authorization") String token);

    @POST("api/comment/")
    @Headers({"Content-Type: application/json"})
    Call<Comment> createComment(@Body Comment comment, @Header("Authorization") String token);

    @DELETE("api/comment/{Id}")
    @Headers({"Content-Type: application/json"})
    Call<Comment> deleteComment(@Path("Id") String Id, @Header("Authorization") String token);

    //@DELETE("api/comment/commentid/{Id}")
    //Call<Comment> deleteByCommentID(@Path("Id") String Id);
}
