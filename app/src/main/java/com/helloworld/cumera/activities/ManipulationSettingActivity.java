package com.helloworld.cumera.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import com.helloworld.cumera.R;
import com.helloworld.cumera.utils.Communication;
import com.helloworld.cumera.utils.Value;
import com.helloworld.cumera.utils.UserData;

public class ManipulationSettingActivity extends Activity {

    private SeekBar chinSetting;
    private SeekBar eyeSetting;
    private int chinValue;
    private int eyeValue;

    private Communication communication;
    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulation_setting);

        communication = new Communication();

        chinSetting = (SeekBar) findViewById(R.id.chinSeekBar);
        eyeSetting = (SeekBar) findViewById(R.id.eyeSeekBar);

        userData = UserData.getInstance();
        int[] get = communication.getDatas(userData.getUsername());
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
                Toast.makeText(ManipulationSettingActivity.this, eyeValue+"", Toast.LENGTH_SHORT).show();
                communication.postDatas(userData.getUsername(), new Value(eyeValue+"", chinValue+""));
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
                Toast.makeText(ManipulationSettingActivity.this, chinValue+"", Toast.LENGTH_SHORT).show();
                communication.postDatas(userData.getUsername(), new Value(eyeValue+"", chinValue+""));
            }
        });
    }
}
