package com.helloworld.cumera.utils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FileUploadService {
    @Multipart
    @POST("/api/selfie/identify")
    Call<ResponseBody> postImage( @Part MultipartBody.Part image, @Part("name") RequestBody name);

    @Multipart
    @POST("/api/selfie/{username}/training")
    Call<ResponseBody> postJoinImage(@Path("username") String username, @Part MultipartBody.Part image, @Part("name") RequestBody name);

}