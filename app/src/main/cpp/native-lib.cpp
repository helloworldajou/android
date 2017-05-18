#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "ImgWarper.h"

//ImgWarper warper;

extern "C"{
JNIEXPORT void JNICALL
Java_com_tzutalin_dlibtest_OnGetImageListener_warp(JNIEnv* env, jobject self, jlong _img,
                                                   jobject _source_points) {

    // TODO
    jclass alCls = env->FindClass("java/util/ArrayList");
    jclass ptCls = env->FindClass("android/graphics/Point");

    if (alCls == nullptr || ptCls == nullptr) {
        return;
    }

    jmethodID alGetId  = env->GetMethodID(alCls, "get", "(I)Ljava/lang/Object;");
    jmethodID alSizeId = env->GetMethodID(alCls, "size", "()I");
    jfieldID ptGetXId = env->GetFieldID(ptCls, "x", "I");
    jfieldID ptGetYId = env->GetFieldID(ptCls, "y", "I");

    if (alGetId == nullptr || alSizeId == nullptr || ptGetXId == nullptr || ptGetYId == nullptr) {
        return;
    }

    int pointCount = static_cast<int>(env->CallIntMethod(_source_points, alSizeId));

    if (pointCount < 1) {
        return;
    }

    std::vector<cv::Point_<double>> source_points;
    std::vector<cv::Point_<double>> dest_points;
    source_points.reserve(pointCount);
    double x, y;

    for (int i = 0; i < pointCount; ++i) {
        jobject point = env->CallObjectMethod(_source_points, alGetId, i);
        x = static_cast<double>(env->GetIntField(point, ptGetXId));
        y = static_cast<double>(env->GetIntField(point, ptGetYId));
        source_points.push_back(cv::Point_<double>(x, y));
    }
    //std::copy( source_points.begin(), source_points.end(), dest_points.begin() );
    dest_points = source_points;
    dest_points[37].y +=20;
    dest_points[38].y +=20;

    //if(warper.isInitialized()){
    //    warper = ImgWarper(long(_img));
    //}
    //warper.warp(long(_img), source_points, dest_points);
}
}

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL
Java_com_tzutalin_dlibtest_OnGetImageListener_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}
}