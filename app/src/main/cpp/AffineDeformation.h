#include <vector>
#define NUM_OF_LANDMARK_POINTS 68
using namespace std;
using namespace cv;

class AffineDeformator{
    private:
        vector<Point_<double> > source_points, dest_points;
        int alpha;

    public:
        Point_<double> move_point(Point_<double> point);

        AffineDeformator(vector<Point_<double> > _source_points, vector<Point_<double> > _dest_points, int _alpha){
            source_points = _source_points;
            dest_points = _dest_points;
            alpha = _alpha;
        }
};