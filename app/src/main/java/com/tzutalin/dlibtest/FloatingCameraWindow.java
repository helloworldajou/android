package com.tzutalin.dlibtest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tzutalin.dlibtest.Communication.Communication;
import com.tzutalin.dlibtest.Communication.Manipulation_setting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.content.ContextCompat.startActivity;
import static java.lang.Thread.sleep;

/**
 * Created by Tzutalin on 2016/5/25
 */
public class FloatingCameraWindow {
    private static final String TAG = "FloatingCameraWindow";
    private Context mContext;
    private WindowManager.LayoutParams mWindowParam;
    private WindowManager mWindowManager;
    private FloatCamView mRootView;
    private Handler mUIHandler;

    private int mWindowWidth;
    private int mWindowHeight;

    private int mScreenMaxWidth;
    private int mScreenMaxHeight;

    private float mScaleWidthRatio = 1.0f;
    private float mScaleHeightRatio = 1.0f;

    private static final boolean DEBUG = true;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;


    public FloatingCameraWindow(Context context) {
        mContext = context;
        mUIHandler = new Handler(Looper.getMainLooper());

        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        // Get screen max size
        Point size = new Point();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            display.getSize(size);
            mScreenMaxWidth = size.x;
            mScreenMaxHeight = size.y;
        } else {
            mScreenMaxWidth = display.getWidth();
            mScreenMaxHeight = display.getHeight();
        }
        // Default window size
        mWindowWidth = mScreenMaxWidth;  // / 2;
        mWindowHeight = mScreenMaxHeight;  // / 2;

        mWindowWidth = mWindowWidth > 0 && mWindowWidth < mScreenMaxWidth ? mWindowWidth : mScreenMaxWidth;
        mWindowHeight = mWindowHeight > 0 && mWindowHeight < mScreenMaxHeight ? mWindowHeight : mScreenMaxHeight;
    }

    public FloatingCameraWindow(Context context, int windowWidth, int windowHeight) {
        this(context);

        if (windowWidth < 0 || windowWidth > mScreenMaxWidth || windowHeight < 0 || windowHeight > mScreenMaxHeight) {
            throw new IllegalArgumentException("Window size is illegal");
        }

        mScaleWidthRatio = (float) windowWidth / mWindowHeight;
        mScaleHeightRatio = (float) windowHeight / mWindowHeight;

        if (DEBUG) {
            Log.d(TAG, "mScaleWidthRatio: " + mScaleWidthRatio);
            Log.d(TAG, "mScaleHeightRatio: " + mScaleHeightRatio);
        }

        mWindowWidth = windowWidth;
        mWindowHeight = windowHeight;
    }

    private void init() {
        mUIHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (mWindowManager == null || mRootView == null) {
                    mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    mRootView = new FloatCamView(FloatingCameraWindow.this);
                    mWindowManager.addView(mRootView, initWindowParameter());
                }
            }
        });
    }

    public void release() {
        mUIHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (mWindowManager != null) {
                    mWindowManager.removeViewImmediate(mRootView);
                    mRootView = null;
                }
                mUIHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    private WindowManager.LayoutParams initWindowParameter() {
        mWindowParam = new WindowManager.LayoutParams();

        mWindowParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowParam.format = 1;
        mWindowParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParam.flags = mWindowParam.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mWindowParam.flags = mWindowParam.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowParam.alpha = 1.0f;

        mWindowParam.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mWindowParam.x = 0;
        mWindowParam.y = 0;
        mWindowParam.width = mWindowWidth;
        mWindowParam.height = mWindowHeight;
        return mWindowParam;
    }

    public void setRGBBitmap(final Bitmap rgb) {
        checkInit();
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                mRootView.setRGBImageView(rgb);
            }
        });
    }

    public void setFPS(final float fps) {
        checkInit();
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                checkInit();
                mRootView.setFPS(fps);
            }
        });
    }

    public void setMoreInformation(final String info) {
        checkInit();
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                checkInit();
                mRootView.setMoreInformation(info);
            }
        });
    }

    private void checkInit() {
        if (mRootView == null) {
            init();
        }
    }

    @UiThread
    private final class FloatCamView extends FrameLayout {
        private WeakReference<FloatingCameraWindow> mWeakRef;
        private static final int MOVE_THRESHOLD = 10;
        private int mLastX;
        private int mLastY;
        private int mFirstX;
        private int mFirstY;
        private LayoutInflater mLayoutInflater;
        private ImageView mColorView;
        private TextView mFPSText;
        private TextView mInfoText;
        private boolean mIsMoving = false;
        View floatView;

        public FloatCamView(FloatingCameraWindow window) {
            super(window.mContext);
            mWeakRef = new WeakReference<FloatingCameraWindow>(window);
            // mLayoutInflater = LayoutInflater.from(context);
            mLayoutInflater = (LayoutInflater) window.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            FrameLayout body = (FrameLayout) this;
            body.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            floatView = mLayoutInflater.inflate(R.layout.cam_window_view, body, true);
            mColorView = (ImageView) findViewById(R.id.imageView_c);
            mFPSText = (TextView) findViewById(R.id.fps_textview);
            mInfoText = (TextView) findViewById(R.id.info_textview);
            mFPSText.setVisibility(View.GONE);
            mInfoText.setVisibility(View.GONE);

            int colorMaxWidth = (int) (mWindowWidth* window.mScaleWidthRatio);
            int colorMaxHeight = (int) (mWindowHeight * window.mScaleHeightRatio);

            mColorView.getLayoutParams().width = colorMaxWidth;
            mColorView.getLayoutParams().height = colorMaxHeight;
        }

        //////////
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Toast.makeText(mContext, ""+(int)event.getRawX()+"    "+event.getRawY()  , Toast.LENGTH_SHORT).show();

            //Intent intent = new Intent
            //startActivity(new Intent(this, Manipulation_setting.class));

                /*    backgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            File screenShot = ScreenShot(floatView);
                            if(screenShot!=null) {
                                //갤러리에 추가
                                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                            }
                        }
                    });
            */

            return true;
        }

        //화면 캡쳐하기
        public File ScreenShot(View view){
            view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

            Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환

            // 현재 날짜로 파일을 저장하기
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            // 년월일시분초
            Date currentTime_1 = new Date();
            String dateString = formatter.format(currentTime_1);

            String filename = dateString+".png";
            File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
            FileOutputStream os = null;
            try{
                os = new FileOutputStream(file);
                screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
                os.close();
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }

            view.setDrawingCacheEnabled(false);
            return file;
        }

        public void setRGBImageView(Bitmap rgb) {
            if (rgb != null && !rgb.isRecycled()) {
                mColorView.setImageBitmap(rgb);
            }
        }

        public void setFPS(float fps) {
            if (mFPSText != null) {
                if (mFPSText.getVisibility() == View.GONE) {
                    mFPSText.setVisibility(View.VISIBLE);
                }
                mFPSText.setText(String.format("FPS: %.2f", fps));
            }
        }

        public void setMoreInformation(String info) {
            if (mInfoText != null) {
                if (mInfoText.getVisibility() == View.GONE) {
                    mInfoText.setVisibility(View.VISIBLE);
                }
                mInfoText.setText(info);
            }
        }
    }

}