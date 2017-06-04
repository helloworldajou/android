# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#set(pathOPENCV /etc/OpenCV-android-sdk)
#set(pathPROJECT ~/dev/helloworld/android)

set(pathOPENCV C:/Users/alsrn/Desktop/teampro/helloajou/OpenCV-android-sdk)
set(pathPROJECT C:/Users/alsrn/Desktop/teampro/android)
set(pathLIBOPENCV_JAVA ${pathPROJECT}/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

file( GLOB MANIPULATION_SOURCES src/main/cpp/*.cpp)
file( GLOB MANIPULATION_HEADERS src/main/cpp/*.h)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

add_library( # Sets the name of the library.
             gpuimage-library

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/yuv-decoder.c )


add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})
include_directories(${pathOPENCV}/sdk/native/jni/include)


add_library( manipulation-lib SHARED ${MANIPULATION_SOURCES} ${MANIPULATION_HEADERS})
set_target_properties( manipulation-lib PROPERTIES LINKER_LANGUAGE CXX )


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

find_library( android-lib android)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( manipulation-lib

                        lib_opencv
                    )



target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       ${android-lib}
                       manipulation-lib
                       lib_opencv
                    )
