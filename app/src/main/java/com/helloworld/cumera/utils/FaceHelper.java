package com.helloworld.cumera.utils;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.helloworld.gpulib.LandmarkPoint;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.util.ArrayList;
import java.util.List;

public class FaceHelper {
    private static FaceDet mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());

    public static ArrayList<Point> landmark = new ArrayList<Point>(68);


    public static void setLandmarks(ArrayList<Point> ldmk){
        for(int i=0; i<ldmk.size(); i++) {
            landmark.set(i, new Point((int) (ldmk.get(i).x * 1.5), (int) (ldmk.get(i).y * 1.5)));
        }
    }

    public static ArrayList<Point> getLandmarks(Bitmap bitmap){
        long tFaceDetect = System.currentTimeMillis();
        List<VisionDetRet> results = detectFace(bitmap);

        long tLandmarkDetect = System.currentTimeMillis();

        // detection success
        if (results.size() != 0) {
            for (final VisionDetRet ret : results) {
                landmark = ret.getFaceLandmarks();
                //BitmapHelper.saveBitmaptoJpeg(bitmap, "CandyCam", "hello.jpg");
                //System.out.println(landmarks);
            }
        }

        //System.out.println(String.format("Face detection: %d", (tLandmarkDetect - tFaceDetect)));
        //System.out.println(String.format("Landmark detection: %d", (System.currentTimeMillis() - tLandmarkDetect)));
        return landmark;
    }

    public static List<VisionDetRet> detectFace(Bitmap bitmap){
        return mFaceDet.detect(bitmap);
    }
}