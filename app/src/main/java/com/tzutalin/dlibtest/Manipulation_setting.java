package com.tzutalin.dlibtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Manipulation_setting extends AppCompatActivity {

    private SeekBar jawSetting = (SeekBar) findViewById(R.id.jawSeekBar);
    private SeekBar eyeSetting = (SeekBar) findViewById(R.id.eyeSeekBar);

    private int jawValue;
    private int eyeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulation_setting);

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
}
