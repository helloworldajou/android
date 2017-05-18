package com.tzutalin.dlibtest.Communication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tzutalin.dlibtest.R;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Manipulation_setting extends AppCompatActivity {

    private SeekBar jawSetting = (SeekBar) findViewById(R.id.jawSeekBar);
    private SeekBar eyeSetting = (SeekBar) findViewById(R.id.eyeSeekBar);

    private int jawValue;
    private int eyeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulation_setting);

        getDatas();

        eyeSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eyeValue = eyeSetting.getProgress();
                Toast.makeText(Manipulation_setting.this, eyeSetting.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        jawSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                jawValue = jawSetting.getProgress();
                Toast.makeText(Manipulation_setting.this, jawSetting.getProgress(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public String settingToJson()
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            jsonObject.put("눈", eyeValue);
            jsonObject.put("턱", jawValue);
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }

        return "[" + jsonObject + "]";
    }

    public void getDatas()
    {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("#####", retrofit.baseUrl().toString());

        CommunicationService gitHubService = retrofit.create(CommunicationService.class);

        Call<Data> call = gitHubService.getRepos("meansoup");

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {

                Log.d("#####", response.code()+": " + response.headers().toString());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("###1#", response.body().eyeSetting + ": " +response.body().jawSetting);
                    jawValue = response.body().jawSetting;
                    eyeValue = response.body().eyeSetting ;
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t){
                Log.e("Not Response", t.getLocalizedMessage());
            }
        });
    }
}
