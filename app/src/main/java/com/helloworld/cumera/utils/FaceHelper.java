package com.helloworld.cumera.utils;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.util.ArrayList;
import java.util.List;

public class FaceHelper {
    private static FaceDet mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
    private static ArrayList<Point> mLandmark = null;

    public static ArrayList<Point> getLandmarks(Bitmap bitmap){
        long tFaceDetect = System.currentTimeMillis();
        List<VisionDetRet> results = detectFace(bitmap);
        mLandmark = null;
        long tLandmarkDetect = System.currentTimeMillis();

        // detection success
        if (results.size() != 0) {
            for (final VisionDetRet ret : results) {
                mLandmark = ret.getFaceLandmarks();
                //BitmapHelper.saveBitmaptoJpeg(bitmap, "CandyCam", "hello.jpg");
                //System.out.println(mLandmarks);
            }
        } else{
            mLandmark = null;
        }

        //System.out.println(String.format("Face detection: %d", (tLandmarkDetect - tFaceDetect)));
        //System.out.println(String.format("Landmark detection: %d", (System.currentTimeMillis() - tLandmarkDetect)));
        return mLandmark;
    }
    public static ArrayList<Point> getLandmarks() { return mLandmark; }
    public static List<VisionDetRet> detectFace(Bitmap bitmap){
        return mFaceDet.detect(bitmap);
    }
}
