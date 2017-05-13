#include <opencv2/core/core.hpp>
#include <iostream>
#include <vector>
#include <algorithm>
#include <math.h>
#include <float.h>
#include "BilinearInterpolation.h"

using namespace cv;
using namespace std;

int a = 0;

cv::Mat BilinearInterpolation::generate(cv::Mat origin_image, std::vector<vector<cv::Point_<double> > > source_grid, vector<cv::Point_<double>*> dest_grid){
    for(int i=0; i<dest_grid.size(); i++){
        draw_image(origin_image, dest_grid[i], source_grid[i]);
    }
    return warped_image;
}

void BilinearInterpolation::draw_image(cv::Mat origin_image, cv::Point_<double>* source_points, vector<cv::Point_<double> > filling_points){
    double x0 = double(std::max(filling_points[0].x, 0.0));
    double y0 = double(std::max(filling_points[0].y, 0.0));
    double x1 = double(std::min(filling_points[2].x, double(width - 1)));
    double y1 = double(std::min(filling_points[2].y, double(height - 1)));
    double xl, xr, yl ,yr;
    double top_x, top_y, bottom_x, bottom_y;
    double src_x, src_y, src_x1, src_y1;
    for(int i = x0; i < x1; i++){
        xl = (i - x0) / (x1-x0);
        xr = 1 - xl;
        top_x = xr * source_points[0].x + xl * source_points[1].x;
        top_y = xr * source_points[0].y + xl * source_points[1].y;
        bottom_x = xr * source_points[3].x + xl * source_points[2].x;
        bottom_y = xr * source_points[3].y + xl * source_points[2].y;
        for(int j = y0; j < y1; j++){
            yl = (j - y0) / (y1 - y0);
            yr = 1 - yl;
            src_x = top_x * yr + bottom_x * yl;
            src_y = top_y * yr + bottom_y * yl;
            
            if (src_x < 0 || src_x > width-1 || src_y < 0 || src_y > height-1){
                warped_image.at<Vec3b>(j, i) = Vec3b(255, 255, 255);
                continue;
            }
            if (isnan(src_x) || isnan(src_y)){
                continue;
            }

            src_x1 = int(floor(src_x));
            src_y1 = int(floor(src_y));
            warped_image.at<Vec3b>(j, i) = origin_image.at<Vec3b>(src_y1, src_x1);
        }
    }
}