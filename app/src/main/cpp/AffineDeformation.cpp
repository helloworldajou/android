#include <opencv2/core/core.hpp>
#include <future>
#include <vector>
#include <iostream>
#include <math.h>
#include "AffineDeformation.h"

using namespace cv;
using namespace std;

cv::Point_<double> get_weigted_average(vector<cv::Point_<double> > points, double* w){
    double sx = 0;
    double sy = 0;
    double sw = 0;

    for (int i=0; i<NUM_OF_LANDMARK_POINTS; i++){
        sx += points[i].x * w[i];
        sy += points[i].y * w[i];
        sw += w[i];
    }
    return cv::Point_<double>(sx / sw, sy / sw);
}

cv::Mat cvPoint_to_cvMat(cv::Point_<double> p){
    cv::Mat point_mat_1x2 = cv::Mat(1, 2, CV_64F, Scalar(0));//cv::Mat(p);
    point_mat_1x2.at<double>(0, 0) = p.x;
    point_mat_1x2.at<double>(0, 1) = p.y;
    return point_mat_1x2;
}

cv::Point_<double> cvMat_to_cvPoint(cv::Mat m){
    return cv::Point_<double>(m.at<double>(0, 0), m.at<double>(0, 1));
}

cv::Point_<double> AffineDeformator::move_point(cv::Point_<double> point){
    double w[NUM_OF_LANDMARK_POINTS];
    cv::Point_<double> p_relative[NUM_OF_LANDMARK_POINTS];
    cv::Point_<double> q_relative[NUM_OF_LANDMARK_POINTS];
    cv::Point_<double> p_average, q_average;
    cv::Mat mat;

    for (int i=0; i < NUM_OF_LANDMARK_POINTS; i++){
        cv::Point_<double> t = source_points[i] - point;
        w[i] = pow(t.x * t.x + t.y * t.y, -alpha);
    }
    p_average = get_weigted_average(source_points, w);
    q_average = get_weigted_average(dest_points, w);

    for(int i=0; i < NUM_OF_LANDMARK_POINTS; i++){
        p_relative[i] = source_points[i] - p_average;
        q_relative[i] = dest_points[i] - q_average;
    }

    mat = cv::Mat(2, 2, CV_64F, Scalar(0));
    for(int i=0; i<NUM_OF_LANDMARK_POINTS; i++){
        int x = p_relative[i].x;
        int y = p_relative[i].y;

        double to_cvmat[2][2] = {
                {x * x * w[i], x * y * w[i]},
                {y * x * w[i], y * y * w[i]}
        };
        cv::Mat wxtx = cv::Mat(2, 2, CV_64F, to_cvmat);
        mat += wxtx;
    }
    mat = mat.inv();

    for(int i=0; i<NUM_OF_LANDMARK_POINTS; i++){
        cv::Point_<double> distance_vector = point - p_average;
        cv::Mat distance_mat = cvPoint_to_cvMat(distance_vector);
        cv::Mat t = distance_mat * mat;

        cv::Point_<double> p = cvMat_to_cvPoint(t);
        double a = (p.x * p_relative[i].x + p.y * p_relative[i].y) * w[i];
        q_average += q_relative[i]*a;
    }
    return q_average;
}