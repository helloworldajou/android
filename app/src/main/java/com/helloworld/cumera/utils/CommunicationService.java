package com.helloworld.cumera.utils;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


//module:app gradle 에 추가해야 할 것.
//        compile 'com.squareup.retrofit2:retrofit:2.3.0'
//        compile 'com.squareup.retrofit2:converter-gson:2.3.0'

public interface CommunicationService {
    @GET("/api/correction_degree/{username}")
    Call<Data> getRepos(@Path("username") String username);

    @POST("/api/correction_degree/{username}")
    Call<Data> postRepos(@Path("username") String username, @Body Value value);

    @GET("/api/selfie/{username}/training")
    Call<String> emptyGet(@Path("username") String username);
}
