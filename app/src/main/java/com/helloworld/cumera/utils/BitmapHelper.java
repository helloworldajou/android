package com.helloworld.cumera.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.util.Log;
import android.os.Environment;


public class BitmapHelper {

    public static boolean doDetect = false;
    public static int cutFrame = 0;
    static int imageNumber = 0;

    public static void fileDelete(File cacheDir){

        if(imageNumber <30) return;

        File storage = cacheDir;

        String fileName = "temp" + imageNumber +  ".jpg";

        File tempFile = new File(storage, fileName);

        if(tempFile.exists()) {
            tempFile.delete();
        }
    }

    public static String saveBitmapToJpeg(File cacheDir, Bitmap bitmap){

        File storage = cacheDir; // 이 부분이 임시파일 저장 경로

        String fileName = "temp" + imageNumber++ + ".jpg";  // 파일이름은 마음대로!

        File tempFile = new File(storage,fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(tempFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

    public static Bitmap createBitmapFromByteArray(byte[] data, Size previewSize){
        YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, opt);


        Matrix matrix = new Matrix();
        matrix.postRotate(-90);

        return Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    public static void saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){

        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;



        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            exception.printStackTrace();
        }catch(IOException exception){
            exception.printStackTrace();
        }
    }
}
