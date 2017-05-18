package com.tzutalin.dlibtest.Communication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by alsrn on 2017-05-17.
 */

//module:app gradle 에 추가해야 할 것.
//        compile 'com.squareup.retrofit2:retrofit:2.3.0'
//        compile 'com.squareup.retrofit2:converter-gson:2.3.0'

public interface CommunicationService {
    @GET("/users/{ID}")
    Call<Data> getRepos(@Path("ID") String id);
}
