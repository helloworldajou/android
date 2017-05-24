/*
 * Copyright 2016-present Tzutalin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tzutalin.dlibtest;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.core.CvException;
import org.opencv.core.Mat;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;
import com.tzutalin.dlibtest.Communication.Communication;

import junit.framework.Assert;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Class that takes in preview frames and converts the image to Bitmaps to process with dlib lib.
 */
public class OnGetImageListener implements OnImageAvailableListener {
    private static final boolean SAVE_PREVIEW_BITMAP = false;

    //324, 648, 972, 1296, 224, 448, 672, 976, 1344
    private static final int INPUT_SIZE = 976;
    private static final String TAG = "OnGetImageListener";

    private int mScreenRotation = 90;

    private int mPreviewWdith = 0;
    private int mPreviewHeight = 0;
    private byte[][] mYUVBytes;
    private int[] mRGBBytes = null;
    private Bitmap mRGBframeBitmap = null;
    private Bitmap mCroppedBitmap = null;
    private Bitmap mResizedBitmap = null;
    private Bitmap mInversedBitmap = null;

    private boolean mIsComputing = false;
    private Handler mInferenceHandler;

    private Context mContext;
    private FaceDet mFaceDet;
    private TrasparentTitleView mTransparentTitleView;
    private FloatingCameraWindow mWindow;
    private Paint mFaceLandmardkPaint;
    private Image image = null;
    private float resizeRatio = 4.5f;

    private Communication communication = new Communication();

    private int mframeNum = 0;
    ArrayList<Point> landmarks;
    List<VisionDetRet> results;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("manipulation-lib");
        System.loadLibrary("native-lib");
    }

    public native void warp(long inputImg, long outputImg, ArrayList<Point> _soucr_points, int width, int height);


    public void initialize(
            final Context context,
            final AssetManager assetManager,
            final TrasparentTitleView scoreView,
            final Handler handler) {
        this.mContext = context;
        this.mTransparentTitleView = scoreView;
        this.mInferenceHandler = handler;
        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        mWindow = new FloatingCameraWindow(mContext);

        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);
        mTransparentTitleView.setText("CUMERA");
    }

    public void deInitialize() {
        synchronized (OnGetImageListener.this) {
            if (mFaceDet != null) {
                mFaceDet.release();
            }

            if (mWindow != null) {
                mWindow.release();
            }
        }
    }

    private void drawResizedBitmap(final Bitmap src, final Bitmap dst) {

        Display getOrient = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        Point point = new Point();
        getOrient.getSize(point);
        int screen_width = point.x;
        int screen_height = point.y;
        Log.d(TAG, String.format("screen size (%d,%d)", screen_width, screen_height));
        if (screen_width < screen_height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
            mScreenRotation = -90;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
            mScreenRotation = 0;
        }

        Assert.assertEquals(dst.getWidth(), dst.getHeight());
        final float minDim = Math.min(src.getWidth(), src.getHeight());

        final Matrix matrix = new Matrix();

        // We only want the center square out of the original rectangle.
        final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        matrix.preTranslate(translateX, translateY);

        final float scaleFactor = dst.getHeight() / minDim;
        matrix.postScale(scaleFactor, scaleFactor);

        // Rotate around the center if necessary.
        if (mScreenRotation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(mScreenRotation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }

    public Bitmap imageSideInversion(Bitmap src){
        Matrix sideInversion = new Matrix();
        sideInversion.setScale(-1, 1);
        Bitmap inversedImage = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), sideInversion, false);
        return inversedImage;
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        image = null;
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            // No mutex needed as this method is not reentrant.
            if (mIsComputing) {
                image.close();
                return;
            }
            mIsComputing = true;

            Trace.beginSection("imageAvailable");

            final Plane[] planes = image.getPlanes();

            // Initialize the storage bitmaps once when the resolution is known.
            if (mPreviewWdith != image.getWidth() || mPreviewHeight != image.getHeight()) {
                mPreviewWdith = image.getWidth();
                mPreviewHeight = image.getHeight();

                //Log.d(TAG, String.format("Initializing at size %dx%d", mPreviewWdith, mPreviewHeight));
                mRGBBytes = new int[mPreviewWdith * mPreviewHeight];
                mRGBframeBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Config.ARGB_8888);
                mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

                mYUVBytes = new byte[planes.length][];
                for (int i = 0; i < planes.length; ++i) {
                    mYUVBytes[i] = new byte[planes[i].getBuffer().capacity()];
                }
            }

            for (int i = 0; i < planes.length; ++i) {
                planes[i].getBuffer().get(mYUVBytes[i]);
            }

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            ImageUtils.convertYUV420ToARGB8888(
                    mYUVBytes[0],
                    mYUVBytes[1],
                    mYUVBytes[2],
                    mRGBBytes,
                    mPreviewWdith,
                    mPreviewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            //Log.e(TAG, "Exception!", e);
            Trace.endSection();
            return;
        }

        mRGBframeBitmap.setPixels(mRGBBytes, 0, mPreviewWdith, 0, 0, mPreviewWdith, mPreviewHeight);
        drawResizedBitmap(mRGBframeBitmap, mCroppedBitmap);

        mInversedBitmap = imageSideInversion(mCroppedBitmap);
        mResizedBitmap = Bitmap.createScaledBitmap(mInversedBitmap, (int)(INPUT_SIZE/4.5), (int)(INPUT_SIZE/4.5), true);

        final int[] forSendingOnce = {0};

        mInferenceHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if(mframeNum % 2 == 0){

                            long time1 = System.currentTimeMillis();
                            synchronized (OnGetImageListener.this) {
                                results = mFaceDet.detect(mResizedBitmap);
                            }
                            long time2 = System.currentTimeMillis();
                            mTransparentTitleView.setText("FPS: " + String.valueOf(1.0 / ((time2 - time1) / 1000f)));
                        }

                        if (results.size() != 0) {

                            if(forSendingOnce[0] == 0) {
                                communication.uploadFile(saveBitmapToJpeg(mContext.getApplicationContext(), mInversedBitmap));
                                forSendingOnce[0] = 1;
                            }

                            for (final VisionDetRet ret : results) {
                                landmarks = ret.getFaceLandmarks();

                                Mat canvas = new Mat(mResizedBitmap.getWidth(), mResizedBitmap.getHeight(), CvType.CV_8UC4);
                                Mat output = new Mat(mResizedBitmap.getWidth(), mResizedBitmap.getHeight(), CvType.CV_8UC4);

                                Utils.bitmapToMat(mResizedBitmap, canvas);
                                warp(canvas.getNativeObjAddr(), output.getNativeObjAddr(), landmarks, mResizedBitmap.getWidth(), mResizedBitmap.getHeight());
                                Utils.matToBitmap(output, mResizedBitmap);

                                /*int count =0;
                                    if(count > 37 && count < 42){
                                Canvas canvas1 = new Canvas(mInversedBitmap);
                                for (Point point : landmarks){
                                        int pointX = (int) (point.x * resizeRatio);
                                        int pointY = (int) (point.y * resizeRatio);
                                        canvas1.drawCircle(pointX, pointY, 4, mFaceLandmardkPaint);
                                    }
                                    count ++;
                                }*/
                            }
                        }

                        mframeNum++;
                        mWindow.setRGBBitmap(mResizedBitmap);
                        mIsComputing = false;
                    }

                });
        Trace.endSection();
    }

    public static String saveBitmapToJpeg(Context context, Bitmap bitmap){

        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로

        String fileName = "temp.jpg";  // 파일이름은 마음대로!

        File tempFile = new File(storage,fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(tempFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }
}



