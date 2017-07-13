package org.opencv.samples.tutorial1;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.x;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    private SeekBar bar;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    int th = 180;//預設閥值

    int[] arr_X = new int[1000];
    int[] arr_Y = new int[1000];
    List<MatOfPoint> contours;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tutorial1_surface_view);

        bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//设置滑动监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                th = progress;
//                stringBuffer.append("正在拖动"+progress+"\n");
//                textView.setText(stringBuffer);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                stringBuffer=new StringBuffer();
//                stringBuffer.append("开始拖动+\n");
//                textView.setText(stringBuffer);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                stringBuffer.append("停止拖动+\n");
//                textView.setText(stringBuffer);
                Log.d("Threshold=", String.valueOf(th));//印出當前閥值
            }
        });

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat Orignal = inputFrame.rgba();

        int m = Orignal.width();
        int n = Orignal.height();
        Log.d("MATDATAH", String.valueOf(Orignal.height()));
        Log.d("MATDATAW", String.valueOf(Orignal.width()));
        Mat Gray = inputFrame.gray();
        Imgproc.GaussianBlur(Gray, Gray, new Size(3, 3), 0, 0);
        Core.flip(Gray, Gray, 1);//翻轉方向
        Core.flip(Orignal, Orignal, 1);//翻轉方向

         /*結構元素*/
        int erosion_size = 15;
        int dilation_size = 3;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));

        Imgproc.threshold(Gray, Gray, th, 255, Imgproc.THRESH_BINARY);//取閥值二值化
        Imgproc.erode(Gray, Gray, element);
        Imgproc.dilate(Gray, Gray, element1);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(Gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        hierarchy.release();
//        Imgproc.drawContours(Orignal, contours, -1, new Scalar(255, 0, 0), 5);//, 2, 8, hierarchy, 0, new Point());

        //For each contour found
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            //Convert contours(i) from MatOfPoint to MatOfPoint2f
//            if (i != 0) {
            if (Imgproc.contourArea(contours.get(i)) > 500 && Imgproc.contourArea(contours.get(i)) < 3000) {

                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());

                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                // Get bounding rect of contour
                Rect rect = Imgproc.boundingRect(points);

                // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                if (i != 0) {

                    Imgproc.rectangle(Orignal, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 3);//框選燈外框
                    Imgproc.circle(Orignal, new Point(rect.x + (rect.width / 2), rect.y + (rect.height) / 2), 3, new Scalar(0, 0, 255), 20);//燈中心點
//                    Imgproc.putText(Orignal, "TX" + String.valueOf(i) + "" + String.valueOf((rect.x + (rect.width / 2))) + "," + String.valueOf((rect.y + (rect.height) / 2))
//                            , new Point(rect.x + (rect.width / 2 - 100), rect.y + (rect.height) / 2 + 50), 2, 1, new Scalar(255, 0, 0, 255), 2);
                    Imgproc.circle(Orignal, new Point(m / 2, n / 2), 3, new Scalar(255, 0, 255), 20);//當前位置(螢幕中心)
                }


                arr_X[i] = (rect.x + (rect.width / 2));//x[0],x[1]
                arr_Y[i] = (rect.y + (rect.height) / 2);//y[0],y[1]

//                Log.d("ARR_X0", String.valueOf(arr_X[0]));
//                Log.d("ARR_X1", String.valueOf(arr_X[1]));
//                Log.d("ARR_X2", String.valueOf(arr_X[2]));

//                Log.d("ARR_Y0", String.valueOf(arr_Y[0]));
//                Log.d("ARR_Y1", String.valueOf(arr_Y[1]));
//                Log.d("ARR_Y2", String.valueOf(arr_Y[2]));
//
//                //D2 兩盞燈像素距離
//                int D2 = (int) Math.sqrt(
//                        Math.abs(arr_X[1] - arr_X[2]) * Math.abs(arr_X[1] - arr_X[2]) +
//                                Math.abs(arr_Y[1] - arr_Y[2]) * Math.abs(arr_Y[1] - arr_Y[2])
//                );
//                double nn = (double) 120 / D2;
//                int CX;//當前位置(螢幕中心)
//                int CY;//當前位置(螢幕中心)
//
//                CX = Math.abs(arr_X[1] - (m / 2));
//                CY = (arr_Y[1] - (n / 2));
//
//
//                if (arr_Y[1] >= arr_Y[2]) {
//                    //TX1 - TX2
//                    double CCCXXX = (nn * CX) + 100;
//                    double CCCYYY = (nn * CY) + 100;
//                    Imgproc.putText(Orignal, String.valueOf((int) CCCXXX) + "," + String.valueOf((int) CCCYYY), new Point(280, 200), 2, 1, new Scalar(255, 255, 0), 2);
//                } else {
//                    //TX2 - TX1
//                    double CCCXXX = 220 - (nn * CX);
//                    double CCCYYY = (nn * CY) + 100;
//                    Imgproc.putText(Orignal, String.valueOf((int) CCCXXX) + "," + String.valueOf((int) CCCYYY), new Point(280, 200), 2, 1, new Scalar(255, 255, 0), 2);
//                }

                //Log.d("D2=", String.valueOf((double) 120 / D2));

                //測試atan()
//                    double x = 45;
//                    x = Math.toRadians(x);
//                    double angle = 45;//x=1 y=1
//                    double arctan = java.lang.Math.atan(angle);
//                    Log.d("SSSS=", String.valueOf((double) arctan * 180 / Math.PI));
//                    Log.d("CCXX=", String.valueOf(CCCXXX));
//                    Log.d("CCYY=", String.valueOf(CCCYYY));

                //計算角度
                    /*double ag = Math.atan((arr_Y[1] - arr_Y[0]) / (arr_X[1] - arr_X[0])) * 180 / Math.PI;
                    int AX;
                    int AY;
                    AX = (int) (((CCCXXX) * Math.cos(x)) + ((CCCYYY) * Math.sin(x)));
                    AY = (int) (-((CCCXXX) * Math.sin(x)) + ((CCCYYY) * Math.cos(x)));
                    Log.d("angle=", String.valueOf((double) Math.sin(x)));
                    Imgproc.putText(Orignal, String.valueOf((int) AX) + "," + String.valueOf((int) AY), new Point(550, 600), 3, 1, new Scalar(0, 255, 0), 2);*/

//                }
            }//for迴圈結尾
//            Log.d("ARR_X0", String.valueOf(arr_X[0]));
            Log.d("ARR_X1", String.valueOf(arr_X[1]));
            Log.d("ARR_X2", String.valueOf(arr_X[2]));
            if (arr_X[1] < arr_X[2]) {
                //TX1 - TX2
                Imgproc.putText(Orignal, "TX1", new Point(arr_X[1] - 50, arr_Y[1] + 50), 2, 1, new Scalar(255, 0, 0), 2);//紅色
                Imgproc.putText(Orignal, "(" + String.valueOf(arr_X[1]) + "," + String.valueOf(arr_Y[1])
                        + ")", new Point(arr_X[1] - 75, arr_Y[1] + 75), 2, 1, new Scalar(255, 0, 0), 2);//紅色

                Imgproc.putText(Orignal, "TX2", new Point(arr_X[2] - 50, arr_Y[2] + 50), 2, 1, new Scalar(255, 0, 0), 2);//紅色
                Imgproc.putText(Orignal, "(" + String.valueOf(arr_X[2]) + "," + String.valueOf(arr_Y[2])
                        + ")", new Point(arr_X[2] - 75, arr_Y[2] + 75), 2, 1, new Scalar(255, 0, 0), 2);
            } else {
                Imgproc.putText(Orignal, "TX1", new Point(arr_X[2] - 50, arr_Y[1] + 50), 2, 1, new Scalar(255, 255, 0), 2);//黃色
                Imgproc.putText(Orignal, "(" + String.valueOf(arr_X[2]) + "," + String.valueOf(arr_Y[2])
                        + ")", new Point(arr_X[2] - 75, arr_Y[1] + 75), 2, 1, new Scalar(255, 255, 0), 2);//黃色

                Imgproc.putText(Orignal, "TX2", new Point(arr_X[1] - 50, arr_Y[1] + 50), 2, 1, new Scalar(255, 255, 0), 2);
                Imgproc.putText(Orignal, "(" + String.valueOf(arr_X[1]) + "," + String.valueOf(arr_Y[1])
                        + ")", new Point(arr_X[1] - 75, arr_Y[1] + 75), 2, 1, new Scalar(255, 255, 0), 2);
            }
        }
        return Orignal;
    }
}
