//
// Created by 기천 on 2017. 5. 22..
//
#include "ManipulationDegree.h"


cv::Point_<double> moveEyeCoordinate(cv::Point_<double> src, int eyeDegree, int index){
    /*if (index == 36 || index == 42){
        //src.x -= eyeDegree*0.1;
    }*/
    if(index == 37 || index == 43)
        src.y += eyeDegree*0.1;
    else if(index == 38 || index == 44)
        src.y += eyeDegree*0.1;
    /*else if(index == 39 || index == 45){
        //src.x += eyeDegree*0.1;
    }*/
    else if(index == 40 || index == 46)
        src.y -= eyeDegree*0.1;
    else if(index == 41 || index == 47)
        src.y -= eyeDegree*0.1;

    return src;
}

cv::Point_<double> moveChinCoordinate(cv::Point_<double> src, int chinDegree, int index){

    src.y += chinDegree*0.1;
    return src;
}


std::vector<cv::Point_<double> > ManipulationDegree::SetManipulationDegree(std::vector<cv::Point_<double> > destCoordinate, int eyeDegree, int chinDegree){

    for(int i=0; i<destCoordinate.size(); i++){
        if((i>=36) && (i<=47) && (eyeDegree != 0))
            destCoordinate[i] = moveEyeCoordinate(destCoordinate[i], eyeDegree, i);

        if((i>=5) && (i<=11) && (chinDegree != 0))
            destCoordinate[i] = moveChinCoordinate(destCoordinate[i], chinDegree, i);
    }
    return destCoordinate;
}

