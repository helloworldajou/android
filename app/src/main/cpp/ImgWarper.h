#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <vector>
#include "time.h"
#include "BilinearInterpolation.h"

using namespace cv;
using namespace std;

class ImgWarper{
private:
    cv::Mat warped_image;
    int alpha;
    int grid_size;
    int height, width, channels;
    int grid_rows, grid_cols;
    bool initialized = false;
    std::vector<vector<cv::Point_<double> > > grid;
    BilinearInterpolation bilinear_interpolator;

public:
    void show_warped_img();
    cv::Mat warp(cv::Mat _img, vector<cv::Point_<double> > source_points, vector<cv::Point_<double> > dest_points);

    ImgWarper (){};
    ImgWarper(cv::Mat sample_image, int _height, int _width){
        alpha = 1;
        grid_size = 15;
        height = _height;
        width =_width;
        grid_rows = (height / grid_size) + 1;
        grid_cols = (width / grid_size) + 1;
        channels = 3;
        //bilinear_interpolator = BilinearInterpolation(sample_image, width, height, channels);

        for (int i=0;  i < width; i += grid_size){
            for(int j=0; j < height; j += grid_size){
                vector<cv::Point_<double> > grid_square;
                grid_square.push_back(cv::Point_<double>(i, j));
                grid_square.push_back(cv::Point_<double>(i+grid_size, j));
                grid_square.push_back(cv::Point_<double>(i+grid_size, j+grid_size));
                grid_square.push_back(cv::Point_<double>(i, j+grid_size));
                grid.push_back(grid_square);
                //cv::circle(sample_image, cv::Point_<double>(i, j), 3, -1);
            }
        }
    }
};