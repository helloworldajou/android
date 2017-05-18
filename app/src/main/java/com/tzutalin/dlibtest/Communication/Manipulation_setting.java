package com.tzutalin.dlibtest.Communication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tzutalin.dlibtest.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Manipulation_setting extends Activity {

    private SeekBar chinSetting;
    private SeekBar eyeSetting;
    private int chinValue;
    private int eyeValue;

    private CommunicationService gitHubService;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulation_setting);

        chinSetting = (SeekBar) findViewById(R.id.chinSeekBar);
        eyeSetting = (SeekBar) findViewById(R.id.eyeSeekBar);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-52-78-198-113.ap-northeast-2.compute.amazonaws.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gitHubService = retrofit.create(CommunicationService.class);

        getDatas();

        eyeSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eyeValue = eyeSetting.getProgress();
                Toast.makeText(Manipulation_setting.this, eyeValue+"", Toast.LENGTH_SHORT).show();
                postDatas(new Data(eyeValue+"", chinValue+""));
            }
        });

        chinSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                chinValue = chinSetting.getProgress();
                Toast.makeText(Manipulation_setting.this, chinValue+"", Toast.LENGTH_SHORT).show();
                postDatas(new Data(eyeValue+"", chinValue+""));
            }
        });
    }

    public void postDatas(Data data)
    {
        Call<Data> call = gitHubService.postRepos(data);

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                 if (response.isSuccessful()) {
                    String str = "response code: " + response.code() + "\n eyes: " + response.body().eyes + "\n chin: " +  response.body().chin;
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });
    }

    public void getDatas()
    {
        Call<Data> call = gitHubService.getRepos();

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chinValue = (int) Float.parseFloat(response.body().chin);
                    eyeValue = (int) Float.parseFloat(response.body().eyes);
                    chinSetting.setProgress(chinValue);
                    eyeSetting.setProgress(eyeValue);
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });
    }
}
