package com.tzutalin.dlibtest.Communication;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by alsrn on 2017-05-20.
 */

public interface FileUploadService {
    @Multipart
    @POST("/api/selfie/identify")
    Call<Data> postImage(@Part MultipartBody.Part image, @Part("name") RequestBody name);
}