
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>
#include <iostream>

class ManipulationDegree{
    public:
        std::vector<cv::Point_<double> > SetManipulationDegree(std::vector<cv::Point_<double> > destPoints, int eye, int chin);
        //void ManipulationDegree::moveEyeCoordinate(cv::Point_<double> src, int eyeDegree);
        //void ManipulationDegree::moveChinCoordinate(cv::Point_<double> src, int chinDegree);
        ManipulationDegree (){};
};


