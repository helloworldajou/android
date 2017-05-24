#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "ImgWarper.h"
#include "ManipulationDegree.h"

extern "C" {

using namespace cv;
using namespace std;

bool isWarperInitialized = false;
ImgWarper warper;
ManipulationDegree mDegree= ManipulationDegree();


JNIEXPORT void JNICALL
Java_com_tzutalin_dlibtest_OnGetImageListener_warp(JNIEnv *env, jobject self, jlong inputImg,
                                                   jlong outputImg, jobject javaArray, jint eyeDegree, jint chinDegree) {

    // arraylist 선언
    jclass arrayListPoint = env->FindClass("java/util/ArrayList");

    // arraylist에 필요한 메소드 id 생성
    jmethodID arraySize = env->GetMethodID(arrayListPoint, "size", "()I");
    jmethodID arrayGet = env->GetMethodID(arrayListPoint, "get", "(I)Ljava/lang/Object;");

    // arraylist의 0번 오브젝트 get, class 얻어오기
    jobject objPoint = env -> CallObjectMethod(javaArray, arrayGet, 0);
    jclass classPoint = env -> GetObjectClass(objPoint);

    // 클래스로 부터 필드 아이디 얻어오기
    jfieldID xID = env -> GetFieldID(classPoint, "x", "I");
    jfieldID yID = env -> GetFieldID(classPoint, "y", "I");


    // int형의 메소드 불러오기
    int numOfPoint = env -> CallIntMethod(javaArray, arraySize);
    double x, y;
    vector<cv::Point_<double> > source_points;
    vector<cv::Point_<double> > dest_points;


    for(int i=0; i<numOfPoint; i++){

        // i번째 오브젝트를 얻어와서, Point 클래스로 얻어옴
        jobject pnt = env -> CallObjectMethod(javaArray, arrayGet, i);

        x = env -> GetIntField(pnt, xID);
        y = env -> GetIntField(pnt, yID);

        x= x*2.0;
        y= y*2.0;

        source_points.push_back(Point_<double>(x, y));
    }

    dest_points = source_points;
    dest_points = mDegree.SetManipulationDegree(dest_points, eyeDegree, chinDegree);

    Mat &matInput = *(Mat *) inputImg;
    Mat &matResult = *(Mat *) outputImg;
    matResult = matInput.clone();

    if (!isWarperInitialized) {
        warper = ImgWarper(matInput, matInput.size().width, matInput.size().height);
        isWarperInitialized = true;
    }

    matResult = warper.warp(matResult, source_points, dest_points);
}
}
