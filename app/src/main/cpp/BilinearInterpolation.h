#include <opencv2/core/core.hpp>
#include <vector>

using namespace cv;
using namespace std;

class BilinearInterpolation{
private:
    int width, height;
    cv::Mat warped_image;

public:
    cv::Mat generate(cv::Mat original_image, std::vector<vector<cv::Point_<double> > > source_grid, vector<cv::Point_<double>*> dest_grid);
    void draw_image(cv::Mat original_image, cv::Point_<double>* source_points, vector<cv::Point_<double> > filling_points);

    BilinearInterpolation(){}

    BilinearInterpolation(cv::Mat _origin_image, int _width, int _height, int _channels){
        width = _width;
        height = _height;
        warped_image = _origin_image.clone();
    }
};