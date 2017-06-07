/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helloworld.cumera.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Handler;

import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.helloworld.cumera.utils.Communication;
import com.helloworld.cumera.utils.BitmapHelper;
import com.helloworld.cumera.FileUtils;
import com.helloworld.cumera.GPUImageFilterTools;
import com.helloworld.cumera.R;
import com.helloworld.cumera.utils.UserData;
import com.helloworld.cumera.utils.Value;
import com.helloworld.gpulib.GPUImage;
import com.helloworld.gpulib.GPUImage.OnPictureSavedListener;
import com.helloworld.gpulib.GPUImageFilter;
import com.helloworld.cumera.GPUImageFilterTools.FilterAdjuster;
import com.helloworld.cumera.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.helloworld.cumera.utils.CameraHelper;
import com.helloworld.cumera.utils.CameraHelper.CameraInfo2;
import com.tarek360.instacapture.InstaCapture;
import com.tarek360.instacapture.listener.ScreenCaptureListener;
import com.tzutalin.dlib.Constants;

import static com.helloworld.cumera.utils.BitmapHelper.doDetect;
import static com.helloworld.cumera.utils.BitmapHelper.fileDelete;

public class CameraActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {

    private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    private GPUImageFilter mFilter;
    private FilterAdjuster mFilterAdjuster;

    private LinearLayout mCamLinearLayout;
    private LinearLayout mSetLinearLayout;
    private LayoutInflater minflater;
    private SeekBar chinSetting;
    private SeekBar eyeSetting;
    private TextView userNameBigTextView;
    private Button joinButton;
    private ImageView faceHintImageView;
    private ImageView loadingView;
    private ImageView switchCamera;
    private RelativeLayout relativeLayout;
    private ImageButton captureButton;


    private Communication communication;
    private UserData userData;

    private boolean joining;
    private int beforeFaceNum;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        captureButton = (ImageButton) findViewById(R.id.button_capture);

        mCamLinearLayout = (LinearLayout) findViewById(R.id.layout_camera);
        minflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        minflater.inflate(R.layout.layout_camera, mCamLinearLayout);
        mSetLinearLayout = (LinearLayout) findViewById(R.id.layout_setting);
        minflater.inflate(R.layout.layout_setting, mSetLinearLayout);



        findViewById(R.id.button_manip_setting).setOnClickListener(this);
        joinButton = (Button) findViewById(R.id.button_join);
        joinButton.setOnClickListener(this);

        faceHintImageView = (ImageView) findViewById(R.id.facehint);
        Drawable faceHintImageSetAlpha = faceHintImageView.getBackground();
        faceHintImageSetAlpha.setAlpha(50);

        userNameBigTextView = (TextView) findViewById(R.id.userNameText);
        userNameBigTextView.setText("Who are you?");

        ((SeekBar) mCamLinearLayout.findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        mCamLinearLayout.findViewById(R.id.button_choose_filter).setOnClickListener(this);
        captureButton.setOnClickListener(this);

        chinSetting = (SeekBar) mSetLinearLayout.findViewById(R.id.chinSeekBar);
        eyeSetting = (SeekBar) mSetLinearLayout.findViewById(R.id.eyeSeekBar);
        chinSetting.setOnSeekBarChangeListener(this);
        eyeSetting.setOnSeekBarChangeListener(this);

        communication = new Communication();
        userData = UserData.getInstance();

        loadingView = (ImageView) findViewById(R.id.loadingView);
        loadingView.bringToFront();

        switchCamera = (ImageView) findViewById(R.id.img_switch_camera);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingView.setVisibility(View.GONE);
            }
        },2000);

        joining = false;
        beforeFaceNum = 0;

        if (!new File(Constants.getFaceShapeModelPath()).exists()) {
            FileUtils.copyFileFromRawToOthers(this.getApplicationContext(), R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
        }

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));

        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader();

        View cameraSwitchView = findViewById(R.id.img_switch_camera);

        cameraSwitchView.setOnClickListener(this);
        if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }

        detectingFace();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    public void detectingFace() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(joining == true)
                    {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    doDetect = true;
                    String[] ret;

                    if(mGPUImage.getCountOfFace() == beforeFaceNum)
                        continue;

                    beforeFaceNum = mGPUImage.getCountOfFace();

                    if(mGPUImage.getCountOfFace() == 0)
                        continue;

                    ret = sendPictureToServer();

                    if(ret == null)
                        continue;

                    if(ret[0] == null)
                        continue;

                    if(ret[0].equals("unknown")) {
                        beforeFaceNum = 0;
                        continue;
                    }

                    if (!ret[0].equals(userData.getUsername())) {

                        userData.setUsername(ret[0]);
                        userData.setEyes(ret[1]);
                        userData.setChin(ret[2]);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    eyeSetting.setProgress(Integer.parseInt(userData.getEyes()));
                                    chinSetting.setProgress(Integer.parseInt(userData.getChin()));
                                    userNameBigTextView.setText(userData.getUsername());
                                }
                            });
                        }

                    doDetect = false;
                }
            }
        }).start();
    }

    public void dialogShow(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new ID");

        final EditText editText = new EditText(getApplicationContext());
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.WHITE);
        editText.setPrivateImeOptions("defaultInputmode=english;");
        builder.setView(editText);

        builder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        userData.setUsername(editText.getText().toString());
                        userData.setChin("0");
                        userData.setEyes("0");

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                joining = true;
                                Bitmap tempBitmap = null;
                                int count = 0;
                                doDetect = true;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        faceHintImageView.setVisibility(View.VISIBLE);
                                        userNameBigTextView.setText(userData.getUsername());
                                    }
                                });

                                while(mGPUImage.getBitmapWithoutFilterApplied() == null)    // mBitmap 쓰고나면 null로 해주기
                                {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                while(count<50) {
                                    if (mGPUImage.getBitmapWithoutFilterApplied() != tempBitmap) {

                                        if(mGPUImage.getCountOfFace() == 0)
                                            continue;

                                        tempBitmap = mGPUImage.getBitmapWithoutFilterApplied();
                                        count++;

                                        sendPictureToServerForJoin(tempBitmap);
                                    }
                                }

                                doDetect = false;
                                communication.joinImageEnd();
                                joining = false;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        faceHintImageView.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(), "training End", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }).start();
                    }
                });

        builder.show();
    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.button_join:
                    dialogShow();
                break;

            case R.id.button_choose_filter:
                GPUImageFilterTools.showDialog(this, new OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                    }
                });
                break;

            case R.id.button_manip_setting:

                if(mCamLinearLayout.getVisibility() == View.VISIBLE)
                {
                    mCamLinearLayout.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);
                    captureButton.setVisibility(View.GONE);
                    mSetLinearLayout.setVisibility(View.VISIBLE);

                    eyeSetting.setProgress(Integer.parseInt(userData.getEyes()));
                    chinSetting.setProgress(Integer.parseInt(userData.getChin()));
                }
                else
                {
                    mCamLinearLayout.setVisibility(View.VISIBLE);
                    joinButton.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.VISIBLE);
                    mSetLinearLayout.setVisibility(View.GONE);
                    communication.postDatas(userData.getUsername(), new Value(userData.getEyes(), userData.getChin()));
                }
                break;
                //sendPictureToServer();

            case R.id.button_capture:

                InstaCapture.getInstance(this).capture(loadingView,switchCamera,faceHintImageView,userNameBigTextView,relativeLayout,joinButton,captureButton).setScreenCapturingListener(new ScreenCaptureListener() {

                    @Override public void onCaptureStarted() {
                        //TODO..
                    }
                    @Override public void onCaptureFailed(Throwable e) {
                        //TODO..
                    }
                    @Override public void onCaptureComplete(Bitmap bitmap) {
                        //TODO..
                        File screenShot = ScreenShot(bitmap);
                        if(screenShot!=null) {
                            //갤러리에 추가
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                        }
                    }
                });

                break;

            case R.id.img_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    public File ScreenShot(Bitmap bitmap){
        //view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

        //Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환
        Bitmap screenBitmap = bitmap;

        // 현재 날짜로 파일을 저장하기
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        // 년월일시분초
        Date currentTime_1 = new Date();
        String dateString = formatter.format(currentTime_1);

        String filename = dateString+".png";
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        //view.setDrawingCacheEnabled(false);
        return file;
    }

    private String[] sendPictureToServer() {

        Bitmap bitmap = mGPUImage.getBitmapWithoutFilterApplied();
        String[] res = communication.uploadFile(BitmapHelper.saveBitmapToJpeg(this.getCacheDir(), bitmap));

        fileDelete(this.getCacheDir());

        return res;
    }

    private void sendPictureToServerForJoin(Bitmap bitmap) {
        communication.uploadJoinFile(BitmapHelper.saveBitmapToJpeg(this.getCacheDir(), bitmap));
        fileDelete(this.getCacheDir());
    }



    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static File getOutputMediaFile(final int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
                                  final boolean fromUser) {
                switch(seekBar.getId())
                {
                    case R.id.seekBar :
                        if (mFilterAdjuster != null) {
                            mFilterAdjuster.adjust(progress);
                        }
                        break;

                    case R.id.eyeSeekBar :
                        userData.setEyes(eyeSetting.getProgress()+"");
                        break;

                    case R.id.chinSeekBar :
                        userData.setChin(chinSetting.getProgress()+"");
                        break;
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    private class CameraLoader {

        private int mCurrentCameraId = 1;
        private Camera mCameraInstance;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    CameraActivity.this, mCurrentCameraId);
            CameraInfo2 cameraInfo = new CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }
}
