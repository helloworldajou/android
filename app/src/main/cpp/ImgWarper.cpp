#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <vector>
#include <string.h>
#include "AffineDeformation.h"
#include "ImgWarper.h"

using namespace cv;
using namespace std;

void ImgWarper::show_warped_img(){
    cv::namedWindow("Warped image", WINDOW_AUTOSIZE);
    imshow("Warped image", warped_image);
}

cv::Mat ImgWarper::warp(cv::Mat _img, vector<cv::Point_<double> > source_points, vector<cv::Point_<double> > dest_points){
    cv::Mat image = cv::Mat(_img);

    AffineDeformator deformator = AffineDeformator(source_points, dest_points, alpha);
    vector<cv::Point_<double>*> transformed_grid;
    cv::Point_<double>** grid_map = new cv::Point_<double>* [grid_rows+1];
    for (int i=0; i < grid_rows+1; i++){
        grid_map[i] = new cv::Point_<double>[grid_cols+1];
        memset(grid_map[i], 0, sizeof(cv::Point_<double>)*grid_cols+1);
    }

    for(int j=0; j<grid_cols; j++){
        for(int i=0; i<grid_rows; i++){
            cv::Point_<double>* grid_square = new cv::Point_<double>[4];
            if (i == 0){
                grid_square[2] = grid_map[i+1][j+1] = deformator.move_point(grid[j * grid_rows + i][2]);
                grid_square[3] = grid_map[i+1][j] = deformator.move_point(grid[j * grid_rows + i][3]);
                if (j == 0){
                    grid_square[0] = grid_map[0][0] = deformator.move_point(grid[j * grid_rows + i][0]);
                    grid_square[1] = grid_map[0][1] = deformator.move_point(grid[j * grid_rows + i][1]);
                } else {
                    grid_square[0] = grid_map[i][j];
                    grid_square[1] = grid_map[i][j+1];
                }
            } else {
                grid_square[0] = grid_map[i][j];
                grid_square[2] = grid_map[i+1][j+1] = deformator.move_point(grid[j * grid_rows + i][2]);
                grid_square[3] = grid_map[i+1][j];
                if (j == 0){
                    grid_square[1] = grid_map[i][j+1] = deformator.move_point(grid[j * grid_rows + i][1]);

                } else {
                    grid_square[1] = grid_map[i][j+1];
                }
            }
            transformed_grid.push_back(grid_square);
        }
    }

    bilinear_interpolator = BilinearInterpolation(image, image.size().width, image.size().height, channels);
    warped_image = bilinear_interpolator.generate(image, grid, transformed_grid);

    for (int i=0; i < grid_rows+1; i++){
        delete[] grid_map[i];
    }
    delete[] grid_map;


    return warped_image;
}