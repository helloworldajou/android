package com.tzutalin.dlibtest.Communication;

import android.util.Log;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by alsrn on 2017-05-22.
 */

public class Communication {

    private CommunicationService gitHubService;
    private Retrofit retrofit;

    public Communication()
    {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-52-78-198-113.ap-northeast-2.compute.amazonaws.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gitHubService = retrofit.create(CommunicationService.class);
    }

    public void postDatas(Data data)
    {
        Call<Data> call = gitHubService.postRepos(data);

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if (response.isSuccessful()) {
                    String str = "response code: " + response.code() + "\n eyes: " + response.body().eyes + "\n chin: " +  response.body().chin;
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });
    }

    public int[] getDatas()
    {
        Call<Data> call = gitHubService.getRepos();
        final int res[] = new int[2];

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if (response.isSuccessful() && response.body() != null) {
                    res[0] = (int) Float.parseFloat(response.body().chin);
                    res[1] = (int) Float.parseFloat(response.body().eyes);
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });

        return res;
    }

    public void uploadFile(String filePath) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        FileUploadService service = new Retrofit.Builder().baseUrl("http://ec2-52-78-198-113.ap-northeast-2.compute.amazonaws.com").client(client).build().create(FileUploadService.class);

        File file = new File(filePath);         //Log.d(filePath, file.toString());

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        //MultipartBody.Part.create(reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body, name);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
//                    Toast.makeText(getApplicationContext(), "response code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
