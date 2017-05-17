cmake_minimum_required(VERSION 2.8)
#TBB library
include_directories(${TBB_ROOT_DIR}/include)

# Local libraries
include_directories(${LandmarkDetector_SOURCE_DIR}/include)
	
include_directories(../../lib/local/LandmarkDetector/include)

find_package( OpenCV REQUIRED )
include_directories ("./src/main/cpp" ) # to find header files
add_library (AffineDeformation SHARED ./src/main/cpp/AffineDeformation.cpp)
add_library (BilinearInterpolation SHARED ./src/main/cpp/BilinearInterpolation.cpp)
add_library (ImgWarper SHARED ./src/main/cpp/ImgWarper.cpp)
target_link_libraries( BilinearInterpolation ${OpenCV_LIBS})
target_link_libraries( AffineDeformation ${OpenCV_LIBS})
target_link_libraries( ImgWarper ${OpenCV_LIBS} AffineDeformation BilinearInterpolation)