package com.example.cheng.test_orientation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.orientation;

public class MainActivity extends Activity {
    OrientationEventListener mOrientationListener;
    TextView textviewOrientation;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textviewOrientation = (TextView)findViewById(R.id.textviewOrientation);
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                textviewOrientation.setText("Orientation: " + String.valueOf(orientation));
//                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
//                    return;  //手机平放时，检测不到有效的角度
//                }
////只检测是否有四个角度的改变
//                if (orientation > 350 || orientation < 10) { //0度
//                    orientation = 0;
//
//                } else if (orientation > 80 && orientation < 100) { //90度
//                    orientation = 90;
//                } else if (orientation > 170 && orientation < 190) { //180度
//                    orientation = 180;
//                } else if (orientation > 260 && orientation < 280) { //270度
//                    orientation = 270;
//                } else {
//                    return;
//                }
//
//                Log.d("onOrientationChanged:", String.valueOf(orientation));
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            Log.v("onOrientationChanged", "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.v("onOrientationChanged", "Cannot detect orientation");
            mOrientationListener.disable();
        }
    }
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        mOrientationListener.disable();
    }
}