# cumera
## team - helloworld

### Usage

1.	git clone git@github.com:helloworldajou/android.git 을 통해 git clone을 통해 다운로드 받는다.

2.	https://github.com/opencv/opencv/releases 에서 opencv-3.2.0-android-sdk 를 다운로드 받는다.

3.	Android studio에서 File > new > import module, source directory에서 OpenCV-android-sdk/sdk/java 폴더를 선택한다.

4.	File > Project Structure > app (왼쪽 modules 부분) > Dependencies > ‘+’ > openCVLibrary320 를 선택한다.

5.	Project의 Android > External Build Files > CMakeList.txt 에서 set(pathOPENCV ~) 와 set(pathPROJECT ~) 부분에 다룬로드 받은 openCV 폴더 위치가 맞는 지와 해당 프로젝트 위치가 맞는지 확인한다.

6.	App > java > com.helloworld > cumera > utils > communication 에서 URL 부분을 사용하는 서버의 주소에 맞도록 수정한다. 이 부분은 wewbserver 부분의 README를 참고하도록 한다.

### Features

* 얼굴인식

* 얼굴 랜드마크 인식 

* 얼굴 이미지 조작

* 서버와의 통신 (사람 구별)

### Using openSource

1. dlib-android (https://github.com/tzutalin/dlib-android-app)

2. opencv320 (https://github.com/opencv/opencv/releases)

3. retrofit & okhttp (https://github.com/square/retrofit)

### License

Apache 2.0 License
