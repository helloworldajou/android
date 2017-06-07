package com.helloworld.gpulib;

import android.opengl.Matrix;
import android.graphics.Point;
import android.opengl.GLES20;

import com.helloworld.cumera.utils.FaceHelper;
import com.helloworld.cumera.utils.UserData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by helloworld on 2017. 6. 4..
 */

public class CUPUImageWarpingFilter extends GPUImageFilter {

    public static final String WARPING_VERTEX_SHADER =
                    "uniform mat4 u_ROT1Matrix; \n"
                    + "uniform mat4 u_ROT2Matrix;"
                    + "attribute vec4 a_position;   \n"
                    + "attribute vec2 a_texCoord;   \n"
                    + "varying vec2 v_texCoord;     \n"
                    + "void main()                  \n"
                    + "{                            \n"
                    + "   gl_Position = u_ROT1Matrix * u_ROT2Matrix * a_position; \n"
                    + "   v_texCoord = a_texCoord;  \n"
                    + "}                            \n";


    public static final String WARPING_FRAGMENT_SHADER =
            "precision mediump float;                            \n"
                    + "varying vec2 v_texCoord;                            \n"
                    + "uniform sampler2D s_texture;                        \n"
                    + "void main()                                         \n"
                    + "{                                                   \n"
                    + "  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
                    + "}                                                   \n";


    // 삼각형 번호
    private short[] eIndicesData = {
            0,2,4, 0,1,4, 1,3,4, 2,3,4
    };

    // 실제 옮길 버텍스 위치 설정
    private float[] eVerticesData = {
            -1.0f, 1.0f, 0.0f,
            // 원래 위치(왼쪽 위)
            0.0f, 0.0f,

            -1.0f, -1.0f, 0.0f,
            0.0f, 1.0f,

            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f,

            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private final short[] mIndicesData = {

            // 왼쪽 눈 - 시계방향으로
            0,17,36, 17,18,36, 18,36,37, 18,19,37, 19,20,37,
            20,37,38, 20,21,38, 21,38,39, 21,27,39, 27,28,39,
            28,29,39, 29,39,40, 29,31,40, 31,40,41, 2,31,41,
            1,2,41, 1,36,41, 0,1,36,

            // 왼쪽 눈 안
            36,37,41, 37,40,41, 37,38,40, 38,39,40,


            // 오른쪽 눈 - 시계 방향으로
            22,27,42, 22,42,43, 22,23,43, 23,43,44, 23,24,44,
            24,25,44, 25,44,45, 25,26,45, 16,26,45, 15,16,45,
            15,45,46, 14,15,46, 14,35,46, 35,46,47, 29,35,47,
            29,42,47, 28,29,42, 27,28,42,

            // 오른쪽 눈 안
            42,43,47, 43,44,47, 44,46,47, 44,45,46,

            // 턱
            2,3,31, 3,31,48, 3,4,48, 4,5,48, 5,48,49,
            5,6,49, 6,49,50, 6,7,50, 7,50,51, 7,8,51,

            8,51,52, 8,9,52, 9,10,52, 10,52,53, 10,11,53,
            11,53,54, 11,12,54, 13,14,54, 14,35,54, 12,13,54,

            // 코
            29,31,35,
            31,48,49, 31,49,50, 31,50,51, 31,51,52, 31,35,52,
            35,52,53, 35,53,54,
            // 68
            68,0,17, 68,69,17,
            // 69
            69,17,18, 69,18,19, 69,19,20, 20,23,69, 23,24,69,
            20,21,22, 20,22,23, 21,22,27, 69,70,24,
            // 70
            70,24,25, 70,25,26, 70,26,16, 70,16,72,
            // 72
            72,16,15, 72,15,14, 72,14,13, 72,13,12, 72,12,75,
            // 74
            74,9,8, 74,8,7, 74,7,6, 74,6,5, 74,73,5,
            // 73
            73,4,5, 73,4,71,
            // 71
            71,4,3, 71,3,2, 71,2,1, 71,1,0, 71,68,0,
            //75
            9,74,75, 9,10,75, 10,11,75, 11,12,75

    };


    private float[] mVerticesData = new float[77*5];
    ArrayList<Point> landmark = null;

    private static final int BYTE_PER_FLOAT = 4;
    private static final int BYTE_PER_SHORT = 2;
    private static final int COORDS_XYZ = 3;
    private static final int COORDS_ST = 2;


    private FloatBuffer mVertices;
    private ShortBuffer mIndices;


    private FloatBuffer eVertices;
    private ShortBuffer eIndices;


    private int mPositionLoc;
    private int mTexCoordLoc;
    private int mSamplerLoc;
    private int mROT1MatrixLoc;
    private int mROT2MatrixLoc;


    private int mWidth;
    private int mHeight;

    private final float[] mRotation_z_Matrix = new float[16];
    private final float[] mRotation_x_Matrix = new float[16];
    UserData userData = UserData.getInstance();


    public CUPUImageWarpingFilter(){
        super(WARPING_VERTEX_SHADER, WARPING_FRAGMENT_SHADER);
        mWidth = 1440;
        mHeight = 1920;
    }

    @Override
    public void onInit() {
        super.onInit();
        mPositionLoc = GLES20.glGetAttribLocation(getProgram(), "a_position");
        mTexCoordLoc = GLES20.glGetAttribLocation(getProgram(), "a_texCoord" );
        mSamplerLoc = GLES20.glGetUniformLocation ( getProgram(), "s_texture" );
        mROT1MatrixLoc = GLES20.glGetUniformLocation(getProgram(), "u_ROT1Matrix");
        mROT2MatrixLoc = GLES20.glGetUniformLocation(getProgram(), "u_ROT2Matrix");
    }


    @Override
    public void onInitialized() {
        super.onInitialized();
    }


    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer){

        mVerticesData = new float[92*5];
        if(FaceHelper.isDetected){
            landmark = FaceHelper.landmark;
            normalizeTextureCoordinate();

            // making buffer
            mVertices = ByteBuffer.allocateDirect(mVerticesData.length * BYTE_PER_FLOAT)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mVertices.put(mVerticesData).position(0);
            mIndices = ByteBuffer.allocateDirect(mIndicesData.length * BYTE_PER_SHORT)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            mIndices.put(mIndicesData).position(0);



            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glViewport(0, 0, mWidth, mHeight);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(mGLProgId);
            runPendingOnDrawTasks();

            // Rotate Matrix
            Matrix.setRotateM(mRotation_z_Matrix, 0, 90, 0, 0, 1.0f);
            Matrix.setRotateM(mRotation_x_Matrix, 0, 180, 1.0f, 0, 0);


            GLES20.glUniformMatrix4fv(mROT1MatrixLoc, 1, false, mRotation_z_Matrix, 0);
            GLES20.glUniformMatrix4fv(mROT2MatrixLoc, 1, false, mRotation_x_Matrix, 0);


            mVertices.position(0);
            GLES20.glVertexAttribPointer ( mPositionLoc, COORDS_XYZ, GLES20.GL_FLOAT, false, 5 * 4, mVertices );
            mVertices.position(3);
            GLES20.glVertexAttribPointer ( mTexCoordLoc, COORDS_ST, GLES20.GL_FLOAT, false, 5 * 4, mVertices );

            GLES20.glEnableVertexAttribArray(mPositionLoc);
            GLES20.glEnableVertexAttribArray(mTexCoordLoc);


            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            GLES20.glUniform1i(mSamplerLoc, 0);
            GLES20.glDrawElements ( GLES20.GL_TRIANGLES, mIndicesData.length, GLES20.GL_UNSIGNED_SHORT, mIndices );

        }else{

        }

    }


    // normalize the texture coordinates
    public void normalizeTextureCoordinate(){

        int width = mWidth / 6;        // 240
        int height = mHeight / 6;      // 320

        // add edge
        landmark.add(new Point(0, 0));
        landmark.add(new Point(width/2, 0));
        landmark.add(new Point(width, 0));
        landmark.add(new Point(0, height/2));
        landmark.add(new Point(width, height/2));
        landmark.add(new Point(0, height));
        landmark.add(new Point(width/2, height));
        landmark.add(new Point(width, height));


        System.out.println("landmark : "+landmark.size());
        for(int i =0; i<landmark.size(); i++){
            float transformedX = height-landmark.get(i).y;
            float transformedY = width-landmark.get(i).x;

            float normalizedX = transformedX / height;
            float normalizedY = transformedY / width;

            float toWarpX = normalizedX * 2 - 1 ;
            float toWarpY = normalizedY * (-2) + 1;


            int eyeDegree = Integer.parseInt(userData.getChin());
            int chinDegree = Integer.parseInt(userData.getEyes());


            if(i>=36 && i<=47){
                warpingEye(eyeDegree, i, normalizedX, normalizedY, toWarpX, toWarpY);
            }else if(i>=0 && i<=16){
                warpingChin(chinDegree, i, normalizedX, normalizedY, toWarpX, toWarpY);
            }
            else{
                mVerticesData[5*i] = toWarpX;
                mVerticesData[5*i+1] = toWarpY;
                mVerticesData[5*i+2] = 0.0f;
                mVerticesData[5*i+3] = normalizedX;
                mVerticesData[5*i+4] = normalizedY;
            }
        }
    }


    public void warpingEye(int eyeDegree, int index, float normlizedX, float normlizedY, float texX, float texY){
        float eyeFactor = 0.0001f;

        switch (index) {
            case 36:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY + eyeDegree * eyeFactor * 0.3f;;
                break;

            case 37:
                mVerticesData[5*index] = texX + eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 38:
                mVerticesData[5*index] = texX + eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 39:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY - eyeDegree * eyeFactor * 0.3f;
                break;

            case 40:
                mVerticesData[5*index] = texX - eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 41:
                mVerticesData[5*index] = texX - eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 42:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY + eyeDegree * eyeFactor * 0.3f;
                break;

            case 43:
                mVerticesData[5*index] = texX + eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 44:
                mVerticesData[5*index] = texX + eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 45:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY - eyeDegree * eyeFactor * 0.3f;
                break;

            case 46:
                mVerticesData[5*index] = texX - eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;

            case 47:
                mVerticesData[5*index] = texX - eyeDegree * eyeFactor;
                mVerticesData[5*index+1] = texY;
                break;
        }


        mVerticesData[5*index+2] = 0.0f;
        mVerticesData[5*index+3] = normlizedX;
        mVerticesData[5*index+4] = normlizedY;
    }


    public void warpingChin(int chinDegree, int index, float normlizedX, float normlizedY, float texX, float texY){
        float chinFactor = 0.0003f;


        switch (index) {
            case 0:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 0.5f;
                break;
            case 1:
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 0.5f;
                break;
            case 2:
                //mVerticesData[5*index] = texX  + chinDegree * chinFactor * 0.8f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.2f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.2f;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 0.8f;
                break;
            case 3:
                //mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.3f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.4f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.4f;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 1.3f;
                break;
            case 4:
                //mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.3f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.8f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.8f;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 1.3f;
                break;
            case 5:
                //mVerticesData[5*index] = texX + chinDegree * chinFactor;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor;
                mVerticesData[5*index] = texX + chinDegree * chinFactor;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor;

                break;
            case 6:
                //mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.8f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 0.8f;

                break;
            case 7:
                //mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.6f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.7f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.7f;
                mVerticesData[5*index+1] = texY - chinDegree * chinFactor * 0.6f;

                break;
            case 8:
                //mVerticesData[5*index] = texX;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index+1] = texY;
                break;

            case 9:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 0.6f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.7f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.7f;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.6f;
                break;

            case 10:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 0.8f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 1.4f;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.8f;
                break;

            case 11:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor;
                mVerticesData[5*index] = texX + chinDegree * chinFactor;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor;
                break;

            case 12:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 1.3f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.8f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.8f;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.3f;
                break;

            case 13:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 1.3f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.4f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.4f;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 1.3f;
                break;

            case 14:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 0.8f;
                //mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.2f;
                mVerticesData[5*index] = texX + chinDegree * chinFactor * 0.2f;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.8f;
                break;

            case 15:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 0.5f;
                //mVerticesData[5*index+1] = texY;
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.5f;
                break;

            case 16:
                //mVerticesData[5*index] = texX - chinDegree * chinFactor * 0.5f;
                //mVerticesData[5*index+1] = texY;
                mVerticesData[5*index] = texX;
                mVerticesData[5*index+1] = texY + chinDegree * chinFactor * 0.5f;
                break;
        }

        mVerticesData[5*index+2] = 0.0f;
        mVerticesData[5*index+3] = normlizedX;
        mVerticesData[5*index+4] = normlizedY;
    }

}
