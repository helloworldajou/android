package com.tzutalin.dlibtest.Communication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tzutalin.dlibtest.R;

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

public class Manipulation_setting extends Activity {

    private SeekBar chinSetting;
    private SeekBar eyeSetting;
    private int chinValue;
    private int eyeValue;

    private Communication communication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulation_setting);

        communication = new Communication();

        chinSetting = (SeekBar) findViewById(R.id.chinSeekBar);
        eyeSetting = (SeekBar) findViewById(R.id.eyeSeekBar);

        int[] get = communication.getDatas("kimsup10");
        eyeSetting.setProgress(get[1]);
        chinSetting.setProgress(get[0]);

        eyeSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eyeValue = eyeSetting.getProgress();
                Toast.makeText(Manipulation_setting.this, eyeValue+"", Toast.LENGTH_SHORT).show();
                communication.postDatas("kimsup10", new Data("kimsup10", eyeValue+"", chinValue+""));
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
                communication.postDatas("kimsup10", new Value(eyeValue+"", chinValue+""));
            }
        });
    }
}