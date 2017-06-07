package com.helloworld.cumera.utils;

import android.util.Log;

import org.json.JSONObject;

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


public class Communication {
    private CommunicationService gitHubService;
    private Retrofit retrofit;
    final private String[] send;
    private final String URL = "http://192.168.10.12:8000";// "http://ec2-52-78-198-113.ap-northeast-2.compute.amazonaws.com";
    private UserData userData;

    public Communication()
    {
        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gitHubService = retrofit.create(CommunicationService.class);
        send = new String[3];

        userData = UserData.getInstance();
    }

    public void postDatas(String username, Value value)
    {
        Call<Data> call = gitHubService.postRepos(username, value);

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if (response.isSuccessful()) {
                    String str = "response code: " + response.code();
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });
    }

    public int[] getDatas(String username)
    {
        Call<Data> call = gitHubService.getRepos(username);
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

    public void joinImageEnd()
    {
        Call<String> call = gitHubService.emptyGet(userData.getUsername());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {}

            @Override
            public void onFailure(Call<String> call, Throwable t) {}
        });
    }

    public String[] uploadFile(String filePath) {

        final boolean[] received = {false};

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        FileUploadService service = new Retrofit.Builder().baseUrl(URL).client(client).build().create(FileUploadService.class);

        File file = new File(filePath);         //Log.d(filePath, file.toString());

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        //MultipartBody.Part.create(reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        retrofit2.Call<ResponseBody> req = service.postImage(body, name);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String get = response.body().string();

                        JSONObject obj = new JSONObject(get);
                        send[0] = obj.getString("username");
                        send[1] = obj.getInt("eyes") +"";
                        send[2] = obj.getInt("chin") +"";

                        received[0] = true;

                    }catch(Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                received[0] = true;
            }
        });

        while(received[0] == false)
            ;

        return send;
    }

    public int uploadJoinFile(String filePath) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        FileUploadService service = new Retrofit.Builder().baseUrl(URL).client(client).build().create(FileUploadService.class);

        File file = new File(filePath);         //Log.d(filePath, file.toString());
        final int[] code = {0};

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        //MultipartBody.Part.create(reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        retrofit2.Call<ResponseBody> req = service.postJoinImage(userData.getUsername(),body, name);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    code[0] = response.code();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

        return code[0];
    }

}
