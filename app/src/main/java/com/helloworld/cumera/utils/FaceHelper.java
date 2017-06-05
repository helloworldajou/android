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

    public static ArrayList<Point> getLandmarks(Bitmap bitmap){
        long tFaceDetect = System.currentTimeMillis();
        ArrayList<Point> landmarks = null;
        List<VisionDetRet> results = detectFace(bitmap);

        long tLandmarkDetect = System.currentTimeMillis();
        if (results.size() != 0) {
            for (final VisionDetRet ret : results) {
                landmarks = ret.getFaceLandmarks();
                System.out.println(landmarks);
            }
        }
        System.out.println(String.format("Face detection: %d", (tLandmarkDetect - tFaceDetect)));
        System.out.println(String.format("Landmark detection: %d", (System.currentTimeMillis() - tLandmarkDetect)));
        return landmarks;
    }
    
    public static List<VisionDetRet> detectFace(Bitmap bitmap){
        return mFaceDet.detect(bitmap);
    }
}
