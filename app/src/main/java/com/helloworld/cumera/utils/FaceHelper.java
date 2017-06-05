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
        List<VisionDetRet> results = detectFace(bitmap);
        ArrayList<Point> landmarks = null;
        if (results.size() != 0) {
            for (final VisionDetRet ret : results) {
                landmarks = ret.getFaceLandmarks();
                System.out.println(landmarks);
            }
        }
        return landmarks;
    }
    
    public static List<VisionDetRet> detectFace(Bitmap bitmap){
        return mFaceDet.detect(bitmap);
    }
}
